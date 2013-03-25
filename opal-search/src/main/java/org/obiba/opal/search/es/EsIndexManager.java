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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.elasticsearch.action.admin.indices.exists.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.indices.TypeMissingException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.Timestampeds;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.web.magma.ValueTableUpdateListener;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class EsIndexManager implements IndexManager, ValueTableUpdateListener {

  private static final Logger log = LoggerFactory.getLogger(EsIndexManager.class);

  public static final String DEFAULT_OPAL_INDEX_NAME = "opal";

  public static final String DEFAULT_CLUSTER_NAME = "opal";

  protected static final int ES_BATCH_SIZE = 100;

  @Nonnull
  protected final ElasticSearchProvider esProvider;

  @Nonnull
  private final ElasticSearchConfigurationService esConfig;

  @Nonnull
  private final IndexManagerConfigurationService indexConfig;

  protected final Version runtimeVersion;

  protected EsIndexManager(@Nonnull ElasticSearchProvider esProvider,
      @Nonnull ElasticSearchConfigurationService esConfig, @Nonnull IndexManagerConfigurationService indexConfig,
      Version version) {

    Preconditions.checkNotNull(esProvider);
    Preconditions.checkNotNull(esConfig);
    Preconditions.checkNotNull(indexConfig);

    this.esProvider = esProvider;
    this.esConfig = esConfig;
    this.indexConfig = indexConfig;
    runtimeVersion = version;
  }

  @Override
  public boolean isReady() {
    return esConfig.getConfig().isEnabled();
  }

  @Override
  public boolean isIndexable(ValueTable valueTable) {
    return indexConfig.getConfig().isReadyForIndexing(valueTable, getIndex(valueTable));
  }

  protected String esIndexName() {
    return esConfig.getConfig().getIndexName();
  }

  protected Settings getIndexSettings() {
    return ImmutableSettings.settingsBuilder().put("number_of_shards", esConfig.getConfig().getShards())
        .put("number_of_replicas", esConfig.getConfig().getReplicas()).build();
  }

  protected IndexMetaData createIndex() {
    IndicesAdminClient idxAdmin = esProvider.getClient().admin().indices();
    if(!idxAdmin.exists(new IndicesExistsRequest(getName())).actionGet().exists()) {
      log.info("Creating index [{}]", getName());
      idxAdmin.prepareCreate(getName()).setSettings(getIndexSettings()).execute().actionGet();
    }
    return esProvider.getClient().admin().cluster().prepareState().setFilterIndices(getName()).execute().actionGet()
        .getState().getMetaData().index(getName());
  }

  @Override
  public void onDelete(ValueTable vt) {
    // Delete index
    getIndex(vt).delete();
  }

  protected abstract class EsIndexer implements IndexSynchronization {

    protected final ValueTable valueTable;

    private final EsValueTableIndex index;

    private final int total;

    protected int done = 0;

    protected boolean stop = false;

    protected EsIndexer(ValueTable table, EsValueTableIndex index) {
      valueTable = table;
      this.index = index;
      total = valueTable.getVariableEntities().size();
    }

    @Override
    public IndexManager getIndexManager() {
      return EsIndexManager.this;
    }

    @Override
    public void run() {
      log.debug("Updating ValueTable index {}", index.getValueTableReference());
      index.delete();
      createIndex();
      index();
    }

    protected BulkRequestBuilder sendAndCheck(BulkRequestBuilder bulkRequest) {
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

    protected abstract void index();

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

  protected abstract class EsValueTableIndex implements ValueTableIndex {

    private final String name;

    private final String valueTableReference;

    EsValueTableIndex(ValueTable vt) {
      name = indexName(vt);
      valueTableReference = tableReference(vt);
    }

    @Override
    public String getIndexName() {
      return name;
    }

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
        return runtimeVersion.compareTo(indexOpalVersion) > 0;
      } catch(Exception e) {
        return true;
      }
    }

    public String getValueTableReference() {
      return valueTableReference;
    }

    protected void updateTimestamps() {
      try {
        EsMapping mapping = readMapping();
        //noinspection ConstantConditions
        mapping.meta().setString("_updated", DateTimeType.get().valueOf(new Date()).toString());
        esProvider.getClient().admin().indices().preparePutMapping(getName()).setType(getIndexName())
            .setSource(mapping.toXContent()).execute().actionGet();
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void delete() {
      if(esProvider.isEnabled()) {
        try {
          esProvider.getClient().admin().indices().prepareDeleteMapping(getName()).setType(getIndexName()).execute()
              .actionGet();
        } catch(TypeMissingException e) {
          // ignored
        } catch(IndexMissingException e) {
          // ignored
        }
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

    protected String tableReference(ValueTable vt) {
      return vt.getDatasource().getName() + "." + vt.getName();
    }

    boolean isForTable(ValueTable valueTable) {
      return valueTableReference.equals(tableReference(valueTable));
    }

    private String indexName(ValueTable vt) {
      return tableReference(vt).replace(' ', '_').replace('.', '-');
    }

    protected EsMapping readMapping() {
      try {
        IndexMetaData indexMetaData = getIndexMetaData();

        if(indexMetaData != null) {
          MappingMetaData metaData = indexMetaData.mapping(getIndexName());
          if(metaData != null) {
            byte[] mappingSource = metaData.source().uncompressed();
            return new EsMapping(getIndexName(), mappingSource);
          }
        }

        return new EsMapping(getIndexName());
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    protected ValueTable resolveTable() {
      return MagmaEngineTableResolver.valueOf(valueTableReference).resolveTable();
    }

    @Nullable
    private IndexMetaData getIndexMetaData() {
      if(esProvider.getClient() == null) return null;

      IndexMetaData imd = esProvider.getClient().admin().cluster().prepareState().setFilterIndices(getName()).execute()
          .actionGet().getState().getMetaData().index(getName());
      return imd != null ? imd : createIndex();
    }

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
