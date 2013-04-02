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
import java.util.Map;

import javax.annotation.Nonnull;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.core.util.TimedExecution;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableStatsIndex;
import org.obiba.opal.search.es.mapping.StatsMapping;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class EsStatsIndexManager extends EsIndexManager implements StatsIndexManager {

  private static final Logger log = LoggerFactory.getLogger(EsStatsIndexManager.class);

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  public EsStatsIndexManager(ElasticSearchProvider esProvider, ElasticSearchConfigurationService esConfig,
      IndexManagerConfigurationService indexConfig, Version version) {
    super(esProvider, esConfig, indexConfig, version);
  }

  @Nonnull
  @Override
  public ValueTableStatsIndex getIndex(@Nonnull ValueTable vt) {
    return (ValueTableStatsIndex) super.getIndex(vt);
  }

  @Override
  protected ValueTableIndex createIndex(@Nonnull ValueTable vt) {
    return new EsValueTableStatsIndex(vt);
  }

  @Override
  public boolean isIndexable(@Nonnull ValueTable valueTable) {
    return !getIndex(valueTable).isUpToDate();
  }

  @Nonnull
  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new Indexer(valueTable, (EsValueTableStatsIndex) index);
  }

  @Nonnull
  @Override
  public String getName() {
    return esIndexName() + "-stats";
  }

  private class Indexer extends EsIndexer {

    private final EsValueTableStatsIndex index;

    private Indexer(ValueTable table, EsValueTableStatsIndex index) {
      super(table, index);
      this.index = index;
    }

    @Override
    protected void index() {
      // do nothing as we calculate summaries on values indexation (from EsValuesIndexManager)
    }

    @Override
    protected XContentBuilder getMapping() {
      return new StatsMapping().createMapping(runtimeVersion, index.getIndexName(), valueTable);
    }

  }

  private class EsValueTableStatsIndex extends EsValueTableIndex implements ValueTableStatsIndex {

    private final Map<String, CategoricalVariableSummary.Builder> categoricalSummaryBuilders = Maps.newHashMap();

    private EsValueTableStatsIndex(@Nonnull ValueTable vt) {
      super(vt, "stats");
    }

    @Override
    public String getFieldName(String variable) {
      return getIndexName() + "-" + variable;
    }

    @Override
    public void indexVariable(@Nonnull Variable variable, @Nonnull Value value) {

      // skip binary variable
      if(variable.getValueType().equals(BinaryType.get())) {
        throw new RuntimeException("Cannot compute summary for binary variable " + variable.getName());
      }

      CategoricalVariableSummary.Builder builder = categoricalSummaryBuilders.get(variable.getName());
      if(builder == null) {
        boolean distinct = TextType.get().equals(variable.getValueType()) && variable.areAllCategoriesMissing();
        builder = new CategoricalVariableSummary.Builder(variable).distinct(distinct);
        categoricalSummaryBuilders.put(variable.getName(), builder);
      }
      builder.addValue(value);
    }

    @Override
    public void computeAndIndexSummaries() {
      try {
        BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

        TimedExecution timedExecution = new TimedExecution().start();

        for(CategoricalVariableSummary.Builder summaryBuilder : categoricalSummaryBuilders.values()) {
          indexSummary(bulkRequest, summaryBuilder.build());
        }

        sendAndCheck(bulkRequest);
        updateTimestamps();

        log.info("Variable summaries of table {} computed in {}", getValueTableReference(),
            timedExecution.end().formatExecutionTime());

      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void delete() {
      super.delete();
      categoricalSummaryBuilders.clear();
    }

    private void indexSummary(BulkRequestBuilder bulkRequest, CategoricalVariableSummary summary) throws IOException {

      XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
      builder.field("categorical-summary").startObject();
      builder.field("mode", summary.getMode()).field("n", summary.getN());
      builder.startArray("frequencies");
      for(CategoricalVariableSummary.Frequency frequency : summary.getFrequencies()) {
        builder.startObject() //
            .field("value", frequency.getValue()) //
            .field("freq", frequency.getFreq()) //
            .field("pct", frequency.getPct()) //
            .endObject();
      }
      builder.endArray(); // frequencies
      builder.endObject(); // categorical-summary
      builder.endObject();

      String variableReference = getValueTableReference() + ":" + summary.getVariable().getName();
      bulkRequest
          .add(esProvider.getClient().prepareIndex(getName(), getIndexName(), variableReference).setSource(builder));

    }

  }
}
