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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

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
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.mapping.ValueTableMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EsIndexManager implements IndexManager {

  private static final Logger log = LoggerFactory.getLogger(EsIndexManager.class);

  private final String DEFAULT_OPAL_INDEX_NAME = "opal";

  private final int ES_BATCH_SIZE = 100;

  private final ElasticSearchProvider esProvider;

  private final ElasticSearchConfigurationService esConfig;

  private final ThreadFactory threadFactory;

  private final Set<EsValueTableIndex> indices = Sets.newHashSet();

  @Autowired
  public EsIndexManager(ElasticSearchProvider esProvider, ElasticSearchConfigurationService esConfig, ThreadFactory threadFactory) {
    Preconditions.checkNotNull(esProvider);
    Preconditions.checkNotNull(esConfig);
    Preconditions.checkNotNull(threadFactory);

    this.esProvider = esProvider;
    this.esConfig = esConfig;
    this.threadFactory = threadFactory;
  }

  @Override
  public IndexSynchronization createSyncTask(final ValueTable table, final ValueTableIndex index) {
    return new Indexer(table, (EsValueTableIndex) index);
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

  private String esIndexName() {
    return esConfig.getConfig().getIndexName(DEFAULT_OPAL_INDEX_NAME);
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
    IndexMetaData imd = esProvider.getClient().admin().cluster().prepareState().setFilterIndices(esIndexName()).execute().actionGet().getState().getMetaData().index(esIndexName());
    return imd != null ? imd : createIndex();
  }

  private IndexMetaData createIndex() {
    esProvider.getClient().admin().indices().prepareCreate(esIndexName()).setSettings(getIndexSettings()).execute().actionGet();
    return esProvider.getClient().admin().cluster().prepareState().setFilterIndices(esIndexName()).execute().actionGet().getState().getMetaData().index(esIndexName());
  }

  private class Indexer implements IndexSynchronization {

    private final ValueTable valueTable;

    private final EsValueTableIndex index;

    private final int total;

    private int done = 0;

    private Indexer(ValueTable table, EsValueTableIndex index) {
      this.valueTable = table;
      this.index = index;
      this.total = valueTable.getVariableEntities().size();
    }

    @Override
    public void run() {
      log.info("Updating ValueTable index {}", index.valueTableReference);
      try {
        esProvider.getClient().admin().indices().prepareDeleteMapping(esIndexName()).setType(index.name).execute().actionGet();
      } catch(IndexMissingException e) {
        createIndex();
      }
      index();
    }

    private void index() {

      XContentBuilder b = new ValueTableMapping().createMapping(index.name, valueTable);

      esProvider.getClient().admin().indices().preparePutMapping(esIndexName()).setType(index.name).setSource(b).execute().actionGet();

      ConcurrentValueTableReader.Builder.newReader().withThreads(threadFactory).from(valueTable).to(new ConcurrentReaderCallback() {

        BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

        public void onBegin(List<VariableEntity> entitiesToCopy, Variable[] variables) {
        };

        @Override
        public void onValues(VariableEntity entity, Variable[] variables, Value[] values) {
          bulkRequest.add(esProvider.getClient().prepareIndex(esIndexName(), valueTable.getEntityType(), entity.getIdentifier()).setSource("{\"identifier\":\"" + entity.getIdentifier() + "\"}"));
          try {
            XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
            for(int i = 0; i < variables.length; i++) {
              if(values[i].isSequence() && values[i].isNull() == false) {
                for(Value v : values[i].asSequence().getValue()) {
                  xcb.field(variables[i].getName(), v.getValue());
                }
              } else {
                xcb.field(variables[i].getName(), values[i].getValue());
              }
            }
            bulkRequest.add(esProvider.getClient().prepareIndex(esIndexName(), index.name, entity.getIdentifier()).setParent(entity.getIdentifier()).setSource(xcb.endObject()));
            done++;
            if(bulkRequest.numberOfActions() >= ES_BATCH_SIZE) {
              sendAndCheck();
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onComplete() {
          sendAndCheck();
          index.updateTimestamps();
        }

        private void sendAndCheck() {
          if(bulkRequest.numberOfActions() > 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if(bulkResponse.hasFailures()) {
              // process failures by iterating through each bulk response item
              throw new RuntimeException(bulkResponse.buildFailureMessage());
            }
            bulkRequest = esProvider.getClient().prepareBulk();
          }
        }
      }).build().read();

    }

    @Override
    public ValueTableIndex getValueTableIndex() {
      return index;
    }

    @Override
    public ValueTable getValueTable() {
      return valueTable;
    }

    @Override
    public boolean hasStarted() {
      return done > 0;
    }

    @Override
    public boolean isComplete() {
      return total > 0 && done >= total;
    }

    @Override
    public float getProgress() {
      return done / (float) total;
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

    private void updateTimestamps() {
      try {
        EsMapping mapping = readMapping();
        mapping.meta().setString("_updated", DateTimeType.get().valueOf(new Date()).toString());
        esProvider.getClient().admin().indices().preparePutMapping(esIndexName()).setType(name).setSource(mapping.toXContent()).execute().actionGet();
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void delete() {
      esProvider.getClient().admin().indices().prepareDeleteMapping(esIndexName()).setType(name).execute().actionGet();
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
      if(obj instanceof EsValueTableIndex == false) return false;

      return ((EsValueTableIndex) obj).name.equals(this.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

  }
}
