/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;

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
import org.obiba.magma.Attribute;
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
import org.obiba.opal.search.es.mapping.AttributeMapping;
import org.obiba.opal.search.es.mapping.ValueTableMapping;
import org.obiba.opal.search.es.mapping.ValueTableVariablesMapping;
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

  @SuppressWarnings("SpringJavaAutowiringInspection")
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
    runtimeVersion = version;
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable table, ValueTableIndex index) {
    return new Indexer(table, (EsValueTableIndex) index);
  }

  @Override
  public boolean isIndexable(ValueTable valueTable) {
    // Currently only based on the state of ElasticSearch because a index that is NOT_SCHEDULED can still be indexable
    // when launched from the "Index Now" action.

    // is running
    return esConfig.getConfig().isEnabled(); //&& indexConfig.getConfig().isIndexable(valueTable);
  }

  @Override
  public boolean isReadyForIndexing(ValueTable valueTable) {
    return esConfig.getConfig().isEnabled() &&
        indexConfig.getConfig().isReadyForIndexing(valueTable, getIndex(valueTable));
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

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private String indexName(ValueTable vt) {
    return tableReference(vt).replace(' ', '_').replace('.', '-');
  }

  private String tableReference(ValueTable vt) {
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  private Settings getIndexSettings() {
    return ImmutableSettings.settingsBuilder().put("number_of_shards", esConfig.getConfig().getShards())
        .put("number_of_replicas", esConfig.getConfig().getReplicas()).build();
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private IndexMetaData getIndexMetaData() {
    if(esProvider.getClient() == null) return null;

    IndexMetaData imd = esProvider.getClient().admin().cluster().prepareState().setFilterIndices(esIndexName())
        .execute().actionGet().getState().getMetaData().index(esIndexName());
    return imd != null ? imd : createIndex();
  }

  private IndexMetaData createIndex() {
    IndicesAdminClient idxAdmin = esProvider.getClient().admin().indices();
    if(!idxAdmin.exists(new IndicesExistsRequest(esIndexName())).actionGet().exists()) {
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

    private boolean stop = false;

    private Indexer(ValueTable table, EsValueTableIndex index) {
      valueTable = table;
      this.index = index;
      total = valueTable.getVariableEntities().size();
    }

    @Override
    public void run() {
      log.info("Updating ValueTable index {}", index.valueTableReference);
      index.delete();
      createIndex();
      indexDictionary();
      index();
    }

    private void indexDictionary() {

      XContentBuilder b = new ValueTableVariablesMapping()
          .createMapping(runtimeVersion, index.getDictionaryName(), valueTable);
      esProvider.getClient().admin().indices().preparePutMapping(esIndexName()).setType(index.getDictionaryName())
          .setSource(b).execute().actionGet();

      BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

      String fullNamePrefix = valueTable.getDatasource().getName() + "." + valueTable.getName();
      for(Variable variable : valueTable.getVariables()) {
        String fullName = fullNamePrefix + ":" + variable.getName();
        try {
          XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
          xcb.field("datasource", valueTable.getDatasource().getName());
          xcb.field("table", valueTable.getName());
          xcb.field("name", variable.getName());
          xcb.field("fullName", fullName);
          xcb.field("entityType", variable.getEntityType());
          xcb.field("valueType", variable.getValueType().getName());
          xcb.field("occurrenceGroup", variable.getOccurrenceGroup());
          xcb.field("repeatable", variable.isRepeatable());
          xcb.field("mimeType", variable.getMimeType());
          xcb.field("unit", variable.getUnit());

          if(variable.hasAttributes()) {
            for(Attribute attribute : variable.getAttributes()) {
              if(!attribute.getValue().isNull()) {
                xcb.field(AttributeMapping.getFieldName(attribute), attribute.getValue());
              }
            }
          }

          bulkRequest.add(esProvider.getClient().prepareIndex(esIndexName(), index.getDictionaryName(), fullName)
              .setSource(xcb.endObject()));
          if(bulkRequest.numberOfActions() >= ES_BATCH_SIZE) {
            bulkRequest = sendAndCheck(bulkRequest);
          }
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }

      sendAndCheck(bulkRequest);
    }

    private BulkRequestBuilder sendAndCheck(BulkRequestBuilder bulkRequest) {
      if(bulkRequest.numberOfActions() > 0) {
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if(bulkResponse.hasFailures()) {
          // process failures by iterating through each bulk response item
          throw new RuntimeException(bulkResponse.buildFailureMessage());
        }
        return esProvider.getClient().prepareBulk();
      }
      return bulkRequest;
    }

    @SuppressWarnings("OverlyComplexAnonymousInnerClass")
    private void index() {

      XContentBuilder b = new ValueTableMapping().createMapping(runtimeVersion, index.getName(), valueTable);

      esProvider.getClient().admin().indices().preparePutMapping(esIndexName()).setType(index.getName()).setSource(b)
          .execute().actionGet();

      ConcurrentValueTableReader.Builder.newReader().withThreads(threadFactory).ignoreReadErrors().from(valueTable)
          .variables(index.getVariables()).to(new ConcurrentReaderCallback() {

        private BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

        private final Map<Variable, VariableNature> natures = new HashMap<Variable, VariableNature>();

        @Override
        public void onBegin(List<VariableEntity> entitiesToCopy, Variable[] variables) {
          for(Variable variable : variables) {
            natures.put(variable, VariableNature.getNature(variable));
          }
        }

        @Override
        public void onValues(VariableEntity entity, Variable[] variables, Value[] values) {
          if(stop) {
            return;
          }

          bulkRequest.add(
              esProvider.getClient().prepareIndex(esIndexName(), valueTable.getEntityType(), entity.getIdentifier())
                  .setSource("{\"identifier\":\"" + entity.getIdentifier() + "\"}"));
          try {
            XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
            for(int i = 0; i < variables.length; i++) {
              String fieldName = index.getFieldName(variables[i].getName());
              if(values[i].isSequence() && !values[i].isNull()) {
                for(Value v : values[i].asSequence().getValue()) {
                  xcb.field(fieldName, esValue(variables[i], v));
                }
              } else {
                xcb.field(fieldName, esValue(variables[i], values[i]));
              }
            }
            bulkRequest.add(esProvider.getClient().prepareIndex(esIndexName(), index.getName(), entity.getIdentifier())
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
          if(stop) {
            index.delete();
          } else {
            sendAndCheck();
            index.updateTimestamps();
          }
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

    @Override
    public void stop() {
      stop = true;
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
    public String getDictionaryName() {
      return name + "-dictionary";
    }

    @Override
    public String getFieldName(String variable) {
      return name + "-" + variable;
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

        @Nonnull
        @Override
        public Value getLastUpdate() {
          return DateTimeType.get().valueOf(meta.getString("_updated"));
        }

        @Nonnull
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
          return !input.getValueType().equals(BinaryType.get());
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
      if(esProvider.isEnabled()) {
       deleteMapping(getName());
       deleteMapping(getDictionaryName());
      }
    }

    private void deleteMapping(String mapping) {
      try {
        esProvider.getClient().admin().indices().prepareDeleteMapping(esIndexName()).setType(mapping).execute()
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
        IndexMetaData indexMetaData = getIndexMetaData();

        if(indexMetaData != null) {
          MappingMetaData metaData = indexMetaData.mapping(name);
          if(metaData != null) {
            byte[] mappingSource = metaData.source().uncompressed();
            return new EsMapping(name, mappingSource);
          }
        }

        return new EsMapping(name);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null &&
          (obj == this || obj instanceof EsValueTableIndex && ((EsValueTableIndex) obj).name.equals(name));

    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    @Override
    public Calendar now() {
      Calendar c = Calendar.getInstance();
      c.setTime(new Date());
      return c;
    }
  }
}
