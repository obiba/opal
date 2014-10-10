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
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.indices.TypeMissingException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.Variable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.Timestampeds;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

abstract class EsIndexManager implements IndexManager, ValueTableUpdateListener {

  private static final Logger log = LoggerFactory.getLogger(EsIndexManager.class);

  public static final String DEFAULT_OPAL_INDEX_NAME = "opal";

  public static final String DEFAULT_CLUSTER_NAME = "opal";

  protected static final int ES_BATCH_SIZE = 100;

  @Autowired
  @NotNull
  protected OpalSearchService opalSearchService;

  @Autowired
  @NotNull
  private ElasticSearchConfigurationService esConfig;

  @Autowired
  @NotNull
  private IndexManagerConfigurationService indexConfig;

  @Autowired
  @NotNull
  protected VersionProvider runtimeVersionProvider;

  private final Map<String, ValueTableIndex> indices = Maps.newHashMap();

  @NotNull
  @Override
  public ValueTableIndex getIndex(@NotNull ValueTable vt) {
    Assert.notNull(vt);

    String tableFullName = vt.getTableReference();
    ValueTableIndex index = indices.get(tableFullName);
    if(index == null) {
      index = createIndex(vt);
      indices.put(tableFullName, index);
    }
    return index;
  }

  @Override
  public boolean hasIndex(@NotNull ValueTable valueTable) {
    ClusterStateResponse resp = opalSearchService.getClient().admin().cluster().prepareState().execute().actionGet();
    ImmutableOpenMap<String, MappingMetaData> mappings = resp.getState().metaData().index(getName()).mappings();
    return mappings.containsKey(getIndex(valueTable).getIndexName());

  }

  protected abstract ValueTableIndex createIndex(@NotNull ValueTable vt);

  @Override
  public boolean isEnabled() {
    return indexConfig.getConfig().isEnabled();
  }

  @Override
  public void setEnabled(boolean enabled) {
    indexConfig.setEnabled(enabled);
  }

  @Override
  public boolean isReady() {
    return opalSearchService.isEnabled() && indexConfig.getConfig().isEnabled();
  }

  @Override
  public boolean isIndexable(@NotNull ValueTable valueTable) {
    return indexConfig.getConfig().isReadyForIndexing(valueTable, getIndex(valueTable));
  }

  @Override
  public boolean isIndexUpToDate(@NotNull ValueTable valueTable) {
    return getIndex(valueTable).isUpToDate();
  }

  protected String esIndexName() {
    return esConfig.getConfig().getIndexName();
  }

  protected Settings getIndexSettings() {
    return ImmutableSettings.settingsBuilder() //
        .put("number_of_shards", esConfig.getConfig().getShards()) //
        .put("number_of_replicas", esConfig.getConfig().getReplicas()).build();
  }

  @NotNull
  protected IndexMetaData createIndex() {
    IndicesAdminClient idxAdmin = opalSearchService.getClient().admin().indices();
    if(!idxAdmin.exists(new IndicesExistsRequest(getName())).actionGet().isExists()) {
      log.info("Creating index [{}]", getName());
      idxAdmin.prepareCreate(getName()).setSettings(getIndexSettings()).execute().actionGet();
    }
    return opalSearchService.getClient().admin().cluster().prepareState().setIndices(getName()).execute().actionGet()
        .getState().getMetaData().index(getName());
  }

  @Override
  public void onRename(@NotNull ValueTable vt, String newName) {
    onDelete(vt);
  }

  @Override
  public void onRename(@Nonnull ValueTable vt, Variable v, String newName) {
    onDelete(vt);
  }

  @Override
  public void onDelete(@NotNull ValueTable vt) {
    // Delete index
    getIndex(vt).delete();
  }

  protected abstract class EsIndexer implements IndexSynchronization {

    @NotNull
    protected final ValueTable valueTable;

    @NotNull
    private final EsValueTableIndex index;

    private final int total;

    protected int done = 0;

    protected boolean stop = false;

    protected EsIndexer(@NotNull ValueTable table, @NotNull EsValueTableIndex index) {
      valueTable = table;
      this.index = index;
      total = valueTable.getVariableEntityCount();
    }

    @Override
    public IndexManager getIndexManager() {
      return EsIndexManager.this;
    }

    @Override
    public void run() {
      log.debug("Updating ValueTable index {}", index.getValueTableReference());
        index.delete();
        boolean success = false;
        try {
            createIndex();
            index.createMapping();
            index();
            success = true;
        } catch (RuntimeException ex) {
            //logging this explicitly, otherwise will be lost
            log.error("Unexpected error creating index", ex);
            throw ex;
        } finally {
            if (!success) {
                index.delete(); //cleanup the index, if failed for any reason
            }
        }
    }

    protected BulkRequestBuilder sendAndCheck(BulkRequestBuilder bulkRequest) {
      if(bulkRequest.numberOfActions() > 0) {
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if(bulkResponse.hasFailures()) {
          // process failures by iterating through each bulk response item
          throw new RuntimeException(bulkResponse.buildFailureMessage());
        }
        return opalSearchService.getClient().prepareBulk();
      }
      return bulkRequest;
    }

    protected abstract void index();

    @Override
    public ValueTableIndex getValueTableIndex() {
      return index;
    }

    @NotNull
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

  protected abstract class EsValueTableIndex implements ValueTableIndex {

    @NotNull
    private final String name;

    @NotNull
    private final String valueTableReference;

    private boolean mappingCreated;

    /**
     * @param vt
     * @param prefixName used to avoid same type name. (Must be unique in ES (even though in different ES indices))
     */
    EsValueTableIndex(@NotNull ValueTable vt) {
      name = indexName(vt);
      valueTableReference = vt.getTableReference();
    }

    @NotNull
    @Override
    public String getIndexName() {
      return name;
    }

    @NotNull
    @Override
    public String getRequestPath() {
      return getName() + "/" + getIndexName();
    }

    @Override
    public boolean requiresUpgrade() {
      EsMapping.Meta meta = readMapping().meta();
      String v = meta.getString("_opalversion");
      if(v == null) return true;
      try {
        Version indexOpalVersion = new Version(v);
        return runtimeVersionProvider.getVersion().compareTo(indexOpalVersion) > 0;
      } catch(Exception e) {
        return true;
      }
    }

    @NotNull
    public String getValueTableReference() {
      return valueTableReference;
    }

    protected void updateTimestamps() {
      try {
        EsMapping mapping = readMapping();
        //noinspection ConstantConditions
        mapping.meta().setString("_updated", DateTimeType.get().valueOf(new Date()).toString());
        opalSearchService.getClient().admin().indices().preparePutMapping(getName()).setType(getIndexName())
            .setSource(mapping.toXContent()).execute().actionGet();
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    protected BulkRequestBuilder sendAndCheck(BulkRequestBuilder bulkRequest) {
      if(bulkRequest.numberOfActions() > 0) {
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if(bulkResponse.hasFailures()) {
          // process failures by iterating through each bulk response item
          throw new RuntimeException(bulkResponse.buildFailureMessage());
        }
        return opalSearchService.getClient().prepareBulk();
      }
      return bulkRequest;
    }

    @Override
    public void delete() {
      if(opalSearchService.isEnabled() && opalSearchService.isRunning()) {
        try {
          opalSearchService.getClient().admin().indices().prepareDeleteMapping(getName()).setType(getIndexName())
              .execute().actionGet();
        } catch(TypeMissingException | IndexMissingException ignored) {
        } finally {
          mappingCreated = false;
        }
      }
    }

    protected void createMapping() {
      if(mappingCreated) return;
      getIndexMetaData(); // create index if it does not exist yet
      opalSearchService.getClient().admin().indices().preparePutMapping(getName()).setType(getIndexName())
          .setSource(getMapping()).execute().actionGet();
      mappingCreated = true;
    }

    protected abstract XContentBuilder getMapping();

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

        @NotNull
        @Override
        public Value getLastUpdate() {
          return DateTimeType.get().valueOf(meta.getString("_updated"));
        }

        @NotNull
        @Override
        public Value getCreated() {
          return DateTimeType.get().valueOf(meta.getString("_created"));
        }

      };
    }

    boolean isForTable(@NotNull ValueTable valueTable) {
      return valueTableReference.equals(valueTable.getTableReference());
    }

    @NotNull
    private String indexName(@NotNull ValueTable table) {
      return table.getTableReference().replace(' ', '_').replace('.', '-');
    }

    @NotNull
    protected EsMapping readMapping() {
      try {
        try {
          IndexMetaData indexMetaData = getIndexMetaData();

          if(indexMetaData != null) {
            MappingMetaData metaData = indexMetaData.mapping(getIndexName());
            if(metaData != null) {
              byte[] mappingSource = metaData.source().uncompressed();
              return new EsMapping(getIndexName(), mappingSource);
            }
          }

          mappingCreated = false;
          return new EsMapping(getIndexName());
        } catch(IndexMissingException e) {
          mappingCreated = false;
          return new EsMapping(getIndexName());
        }
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    protected ValueTable resolveTable() {
      return MagmaEngineTableResolver.valueOf(valueTableReference).resolveTable();
    }

    @Nullable
    private IndexMetaData getIndexMetaData() {
      if(opalSearchService.getClient() == null) return null;

      IndexMetaData imd = opalSearchService.getClient().admin().cluster().prepareState().setIndices(getName()).execute()
          .actionGet().getState().getMetaData().index(getName());
      return imd == null ? createIndex() : imd;
    }

    @NotNull
    @Override
    public Calendar now() {
      Calendar c = Calendar.getInstance();
      c.setTime(new Date());
      return c;
    }

    @Override
    public int hashCode() {
      return getIndexName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null && (obj == this ||
          obj instanceof EsValueTableIndex && ((ValueTableIndex) obj).getIndexName().equals(getIndexName()));
    }

  }

}
