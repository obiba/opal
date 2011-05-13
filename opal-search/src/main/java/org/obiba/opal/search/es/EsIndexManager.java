/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexMissingException;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.magma.support.Timestampeds;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.runtime.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.mapping.ValueTableMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EsIndexManager implements IndexManager {

  private static final Logger log = LoggerFactory.getLogger(EsIndexManager.class);

  private final String OPAL_INDEX_NAME = "opal";

  private final int ES_BATCH_SIZE = 100;

  // Grace period before reindexing (in seconds)
  private final int GRACE_PERIOD = 300;

  private final ElasticSearchProvider esProvider;

  private final ElasticSearchConfigurationService esConfig;

  private final ThreadFactory threadFactory;

  private final Set<EsValueTableIndex> indices = Sets.newHashSet();

  private final Sync sync = new Sync();

  @Autowired
  public EsIndexManager(ElasticSearchProvider esProvider, ElasticSearchConfigurationService esConfig, ThreadFactory threadFactory) {
    Preconditions.checkNotNull(esProvider);
    Preconditions.checkNotNull(esConfig);
    Preconditions.checkNotNull(threadFactory);

    this.esProvider = esProvider;
    this.esConfig = esConfig;
    this.threadFactory = threadFactory;
  }

  // Every ten seconds
  @Scheduled(fixedDelay = 10 * 1000)
  public void synchronizeIndices() {
    getSubject().execute(sync);
  }

  private Subject getSubject() {
    // Login as background job user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager().authenticate(new BackgroundJobServiceAuthToken()).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public EsValueTableIndex getIndex(ValueTable vt) {
    Preconditions.checkNotNull(vt);

    for(EsValueTableIndex i : this.indices) {
      if(i.isForTable(vt)) return i;
    }
    EsValueTableIndex i = new EsValueTableIndex(vt);
    this.indices.add(i);
    return i;
  }

  private String indexName(ValueTable vt) {
    return tableReference(vt).toLowerCase().replaceAll(" ", "_");
  }

  private String tableReference(ValueTable vt) {
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  private Settings getIndexSettings() {
    return ImmutableSettings.settingsBuilder().put("number_of_shards", esConfig.getConfig().getShards()).put("number_of_replicas", esConfig.getConfig().getReplicas()).build();
  }

  private IndexMetaData getIndexMetaData() {
    IndexMetaData imd = esProvider.getClient().admin().cluster().prepareState().setFilterIndices(OPAL_INDEX_NAME).execute().actionGet().getState().getMetaData().index(OPAL_INDEX_NAME);
    return imd != null ? imd : createIndex();
  }

  private IndexMetaData createIndex() {
    esProvider.getClient().admin().indices().prepareCreate(OPAL_INDEX_NAME).setSettings(getIndexSettings()).execute().actionGet();
    return esProvider.getClient().admin().cluster().prepareState().setFilterIndices(OPAL_INDEX_NAME).execute().actionGet().getState().getMetaData().index(OPAL_INDEX_NAME);
  }

  private class Sync implements Runnable {

    @Override
    public void run() {
      for(Datasource ds : MagmaEngine.get().getDatasources()) {
        for(ValueTable vt : ds.getValueTables()) {

          // Check that the index is older than the ValueTable
          if(Timestampeds.lastUpdateComparator.compare(getIndex(vt), vt) < 0) {
            // The index needs to be updated
            Value value = vt.getTimestamps().getLastUpdate();
            // Check that the last modification to the ValueTable is older than the gracePeriod
            // If we don't know (null value), reindex
            if(value.isNull() || value.compareTo(gracePeriod()) < 0) {
              new Indexer().update(vt, getIndex(vt));
            }
          }

        }
      }
      // esProvider.getClient().admin().indices().prepareOptimize(OPAL_INDEX_NAME).setWaitForMerge(false).execute().actionGet();
    }

    /**
     * Returns a {@code Value} with the date and time at which things are reindexed.
     * @return
     */
    private Value gracePeriod() {
      // Now
      Calendar gracePeriod = Calendar.getInstance();
      // Move back in time by GRACE_PERIOD seconds
      gracePeriod.add(Calendar.SECOND, -GRACE_PERIOD);
      // Things modified before this value can be reindexed
      return DateTimeType.get().valueOf(gracePeriod);
    }
  }

  private class Indexer {

    void update(ValueTable vt, EsValueTableIndex index) {
      log.info("Updating ValueTable index {}", index.valueTableReference);
      try {
        esProvider.getClient().admin().indices().prepareDeleteMapping(OPAL_INDEX_NAME).setType(index.name).execute().actionGet();
      } catch(IndexMissingException e) {
        createIndex();
      }
      index(vt, index);
    }

    private void index(final ValueTable valueTable, final EsValueTableIndex index) {

      XContentBuilder b = new ValueTableMapping().createMapping(index.name, valueTable);

      esProvider.getClient().admin().indices().preparePutMapping(OPAL_INDEX_NAME).setSource(b).execute().actionGet();

      ConcurrentValueTableReader.Builder.newReader().withThreads(threadFactory).from(valueTable).to(new ConcurrentReaderCallback() {

        BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

        @Override
        public void onValues(VariableEntity entity, Variable[] variables, Value[] values) {
          bulkRequest.add(esProvider.getClient().prepareIndex(OPAL_INDEX_NAME, valueTable.getEntityType(), entity.getIdentifier()).setSource("{\"identifier\":\"" + entity.getIdentifier() + "\"}"));
          try {
            XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
            for(int i = 0; i < variables.length; i++) {
              xcb.field(variables[i].getName(), values[i].getValue());
            }
            bulkRequest.add(esProvider.getClient().prepareIndex(OPAL_INDEX_NAME, index.name, entity.getIdentifier()).setParent(entity.getIdentifier()).setSource(xcb.endObject()));
            if(bulkRequest.numberOfActions() >= ES_BATCH_SIZE) {
              BulkResponse bulkResponse = bulkRequest.execute().actionGet();
              if(bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item
                throw new RuntimeException(bulkResponse.buildFailureMessage());
              }
              bulkRequest = esProvider.getClient().prepareBulk();
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onComplete() {
          index.updateTimestamps();
        }

        @Override
        public void onBegin() {
        }

      }).build().read();

    }
  }

  private class EsValueTableIndex implements ValueTableIndex {

    private final String name;

    private final String valueTableReference;

    private EsValueTableIndex(ValueTable vt) {
      this.name = indexName(vt);
      this.valueTableReference = tableReference(vt);
    }

    @Override
    public Timestamps getTimestamps() {
      return new Timestamps() {

        private final EsMapping.Meta meta;

        {
          meta = readMapping().meta();
        }

        @Override
        public Value getLastUpdate() {
          return DateTimeType.get().valueOf(meta.getString("_updated"));
        }

        @Override
        public Value getCreated() {
          return DateTimeType.get().valueOf(meta.getString("_created"));
        }

      };
    }

    public void updateTimestamps() {
      try {
        EsMapping mapping = readMapping();
        mapping.meta().setString("_updated", DateTimeType.get().valueOf(new Date()).toString());
        esProvider.getClient().admin().indices().preparePutMapping(OPAL_INDEX_NAME).setSource(mapping.toXContent()).execute().actionGet();
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public IndexSynchronization update() {
      return null;
    }

    @Override
    public void delete() {
      esProvider.getClient().admin().indices().prepareDeleteMapping(OPAL_INDEX_NAME).setType(name).execute().actionGet();
    }

    boolean isForTable(ValueTable valueTable) {
      return valueTableReference.equals(tableReference(valueTable));
    }

    private EsMapping readMapping() {
      try {
        MappingMetaData metaData = getIndexMetaData().mapping(name);
        if(metaData != null) {
          byte[] mappingSource = metaData.source().uncompressed();
          return new EsMapping(name, mappingSource);
        }
        return new EsMapping(name);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean equals(Object obj) {
      if(obj == null) return false;
      if(obj == this) return true;

      return ((EsValueTableIndex) obj).name.equals(this.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

  }
}
