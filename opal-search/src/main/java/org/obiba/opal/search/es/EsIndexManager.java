/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.elasticsearch.action.admin.indices.exists.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.TypeMissingException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.Timestampeds;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.mapping.ValueTableMapping;
import org.obiba.opal.web.magma.ValueTableUpdateListener;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Component
public class EsIndexManager implements IndexManager, ValueTableUpdateListener {

  private static final Logger log = LoggerFactory.getLogger(EsIndexManager.class);

  public static final String DEFAULT_OPAL_INDEX_NAME = "opal";

  public static final String DEFAULT_CLUSTER_NAME = "opal";

  private static final int ES_BATCH_SIZE = 100;

  private final ElasticSearchProvider esProvider;

  private final ElasticSearchConfigurationService esConfig;

  private final IndexManagerConfigurationService indexConfig;

  private final ThreadFactory threadFactory;

  private final Version runtimeVersion;

  private final Set<EsValueTableIndex> indices = Sets.newHashSet();

  @Autowired
  public EsIndexManager(ElasticSearchProvider esProvider, ElasticSearchConfigurationService esConfig,
      IndexManagerConfigurationService indexConfig, ThreadFactory threadFactory, Version version) {

    Preconditions.checkNotNull(esProvider);
    Preconditions.checkNotNull(esConfig);
    Preconditions.checkNotNull(indexConfig);
    Preconditions.checkNotNull(threadFactory);

    this.esProvider = esProvider;
    this.esConfig = esConfig;
    this.indexConfig = indexConfig;
    this.threadFactory = threadFactory;
    this.runtimeVersion = version;
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable table, ValueTableIndex index) {
    return new Indexer(table, (EsValueTableIndex) index);
  }

  @Override
  public boolean isIndexable(ValueTable valueTable) {
    // Currently only based on the state of ElasticSearch
    return esConfig.getConfig().isEnabled() && indexConfig.getConfig().isIndexable(valueTable);
  }

  @Override
  public EsValueTableIndex getIndex(ValueTable vt) {
    Preconditions.checkNotNull(vt);

    for(EsValueTableIndex i : indices) {
      if(i.isForTable(vt)) return i;
    }
    EsValueTableIndex i = new EsValueTableIndex(vt);
    indices.add(i);
    return i;
  }

  private String esIndexName() {
    return esConfig.getConfig().getIndexName();
  }

  private String indexName(ValueTable vt) {
    return tableReference(vt).toLowerCase().replaceAll(" ", "_");
  }

  private String tableReference(ValueTable vt) {
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  private Settings getIndexSettings() {
    return ImmutableSettings.settingsBuilder().put("number_of_shards", esConfig.getConfig().getShards())
        .put("number_of_replicas", esConfig.getConfig().getReplicas()).build();
  }

  private IndexMetaData getIndexMetaData() {
    IndexMetaData imd = esProvider.getClient().admin().cluster().prepareState().setFilterIndices(esIndexName())
        .execute().actionGet().getState().getMetaData().index(esIndexName());
    return imd != null ? imd : createIndex();
  }

  private IndexMetaData createIndex() {
    IndicesAdminClient idxAdmin = esProvider.getClient().admin().indices();
    if(idxAdmin.exists(new IndicesExistsRequest(esIndexName())).actionGet().exists() == false) {
      log.info("Creating index [{}]", esIndexName());
      idxAdmin.prepareCreate(esIndexName()).setSettings(getIndexSettings()).execute().actionGet();
    }
    return esProvider.getClient().admin().cluster().prepareState().setFilterIndices(esIndexName()).execute().actionGet()
        .getState().getMetaData().index(esIndexName());
  }

  @Override
  public void onDelete(ValueTable vt) {
    // Delete index
    getIndex(vt).delete();
  }

  private class Indexer implements IndexSynchronization {

    private final ValueTable valueTable;

    private final EsValueTableIndex index;

    private final int total;

    private int done = 0;

    private Indexer(ValueTable table, EsValueTableIndex index) {
      valueTable = table;
      this.index = index;
      total = valueTable.getVariableEntities().size();
    }

    @Override
    public void run() {
      log.info("Updating ValueTable index {}", index.valueTableReference);
      IndicesAdminClient idxAdmin = esProvider.getClient().admin().indices();
      if(idxAdmin.exists(new IndicesExistsRequest(esIndexName())).actionGet().exists() == false) {
        createIndex();
      } else {
        try {
          idxAdmin.prepareDeleteMapping(esIndexName()).setType(index.name).execute().actionGet();
        } catch(TypeMissingException e) {
          // ignored
        }
      }
      index();
    }

    private void index() {

      XContentBuilder b = new ValueTableMapping().createMapping(runtimeVersion, index.name, valueTable);

      esProvider.getClient().admin().indices().preparePutMapping(esIndexName()).setType(index.name).setSource(b)
          .execute().actionGet();

      ConcurrentValueTableReader.Builder.newReader().withThreads(threadFactory).from(valueTable)
          .variables(index.getVariables()).to(new ConcurrentReaderCallback() {

        private BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

        private Map<Variable, VariableNature> natures = new HashMap<Variable, VariableNature>();

        @Override
        public void onBegin(List<VariableEntity> entitiesToCopy, Variable[] variables) {
          for(Variable variable : variables) {
            natures.put(variable, VariableNature.getNature(variable));
          }
        }

        @Override
        public void onValues(VariableEntity entity, Variable[] variables, Value[] values) {
          bulkRequest.add(
              esProvider.getClient().prepareIndex(esIndexName(), valueTable.getEntityType(), entity.getIdentifier())
                  .setSource("{\"identifier\":\"" + entity.getIdentifier() + "\"}"));
          try {
            XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
            for(int i = 0; i < variables.length; i++) {
              String fieldName = index.getName() + ":" + variables[i].getName();
              if(values[i].isSequence() && values[i].isNull() == false) {
                for(Value v : values[i].asSequence().getValue()) {
                  xcb.field(fieldName, esValue(variables[i], v));
                }
              } else {
                xcb.field(fieldName, esValue(variables[i], values[i]));
              }
            }
            bulkRequest.add(esProvider.getClient().prepareIndex(esIndexName(), index.name, entity.getIdentifier())
                .setParent(entity.getIdentifier()).setSource(xcb.endObject()));
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

        /**
         * OPAL-1158: missing values are indexed as null for continuous variables
         * @param variable the variable
         * @param value the value
         * @return an object
         */
        private Object esValue(Variable variable, Value value) {
          switch(natures.get(variable)) {
            case CONTINUOUS:
              if(variable.isMissingValue(value)) {
                return null;
              }
          }
          return value.getValue();
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
      name = indexName(vt);
      valueTableReference = tableReference(vt);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getRequestPath() {
      return esIndexName() + "/" + getName();
    }

    @Override
    public boolean requiresUpgrade() {
      EsMapping.Meta meta = readMapping().meta();
      String v = meta.getString("_opalversion");
      if(v == null) return true;
      try {
        Version indexOpalVersion = new Version(v);
        return runtimeVersion.compareTo(indexOpalVersion) > 0;
      } catch(Exception e) {
        return true;
      }
    }

    @Override
    public boolean isUpToDate() {
      return Timestampeds.lastUpdateComparator.compare(this, resolveTable()) >= 0;
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

    @Override
    public Iterable<Variable> getVariables() {
      // Do not index binary values, do not even extract the binary values
      // TODO Could be configurable at table level?
      return Iterables.filter(resolveTable().getVariables(), new Predicate<Variable>() {

        @Override
        public boolean apply(Variable input) {
          return input.getValueType().equals(BinaryType.get()) == false;
        }

      });
    }

    private ValueTable resolveTable() {
      return MagmaEngineTableResolver.valueOf(valueTableReference).resolveTable();
    }

    private void updateTimestamps() {
      try {
        EsMapping mapping = readMapping();
        mapping.meta().setString("_updated", DateTimeType.get().valueOf(new Date()).toString());
        esProvider.getClient().admin().indices().preparePutMapping(esIndexName()).setType(name)
            .setSource(mapping.toXContent()).execute().actionGet();
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void delete() {
      try {
        esProvider.getClient().admin().indices().prepareDeleteMapping(esIndexName()).setType(name).execute()
            .actionGet();
      } catch(TypeMissingException e) {
        // ignored
      }
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
      if(!(obj instanceof EsValueTableIndex)) return false;

      return ((EsValueTableIndex) obj).name.equals(name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

  }
}
