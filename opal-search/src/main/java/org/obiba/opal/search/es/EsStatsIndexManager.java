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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.core.util.TimedExecution;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.math.stat.IntervalFrequency;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary.Distribution;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableStatsIndex;
import org.obiba.opal.search.es.mapping.StatsMapping;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Search.EsCategoricalSummaryDto;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.googlecode.protobuf.format.JsonFormat;

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

    private final Map<String, ContinuousVariableSummary.Builder> continuousSummaryBuilders = Maps.newHashMap();

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

      addValueToCategoricalSummaryBuilder(variable, value);
      addValueToContinuousSummaryBuilder(variable, value);
    }

    private void addValueToCategoricalSummaryBuilder(Variable variable, Value value) {
      CategoricalVariableSummary.Builder builder = categoricalSummaryBuilders.get(variable.getName());
      if(builder == null) {
        boolean distinct = TextType.get().equals(variable.getValueType()) && variable.areAllCategoriesMissing();
        builder = new CategoricalVariableSummary.Builder(variable).distinct(distinct);
        categoricalSummaryBuilders.put(variable.getName(), builder);
      }
      builder.addValue(value);
    }

    private void addValueToContinuousSummaryBuilder(Variable variable, Value value) {
      if(!variable.getValueType().isNumeric()) return;
      ContinuousVariableSummary.Builder builder = continuousSummaryBuilders.get(variable.getName());
      if(builder == null) {
        builder = new ContinuousVariableSummary.Builder(variable, Distribution.normal);
        continuousSummaryBuilders.put(variable.getName(), builder);
      }
      builder.addValue(value);
    }

    @Override
    public void computeAndIndexSummaries() {

      BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

      TimedExecution timedExecution = new TimedExecution().start();

      // categorical summaries
      for(CategoricalVariableSummary.Builder summaryBuilder : categoricalSummaryBuilders.values()) {
        indexSummary(summaryBuilder.build(), bulkRequest);
      }

      // continuous summaries
      for(ContinuousVariableSummary.Builder summaryBuilder : continuousSummaryBuilders.values()) {
        indexSummary(summaryBuilder.build(), bulkRequest);
      }

      sendAndCheck(bulkRequest);
      updateTimestamps();

      log.info("Variable summaries of table {} computed in {}", getValueTableReference(),
          timedExecution.end().formatExecutionTime());
    }

    @Override
    public void delete() {
      super.delete();
      categoricalSummaryBuilders.clear();
    }

    @Override
    public void indexSummary(@Nonnull ContinuousVariableSummary summary) {
      TimedExecution timedExecution = new TimedExecution().start();

      BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();
      indexSummary(summary, bulkRequest);
      sendAndCheck(bulkRequest);
      updateTimestamps();

      log.debug("Indexed variable {} categorical summary in {}", summary.getVariable().getName(),
          timedExecution.end().formatExecutionTime());
    }

    @Override
    public void indexSummary(@Nonnull CategoricalVariableSummary summary) {
      TimedExecution timedExecution = new TimedExecution().start();

      BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();
      indexSummary(summary, bulkRequest);
      sendAndCheck(bulkRequest);
      updateTimestamps();

      log.debug("Indexed variable {} continuous summary in {}", summary.getVariable().getName(),
          timedExecution.end().formatExecutionTime());
    }

    private void indexSummary(@Nonnull CategoricalVariableSummary summary, @Nonnull BulkRequestBuilder bulkRequest) {

      EsCategoricalSummaryDto summaryDto = EsCategoricalSummaryDto.newBuilder() //
          .setNature("categorical") //
          .setDistinct(summary.isDistinct()) //
          .setSummary(Dtos.asDto(summary)).build();

      IndexRequestBuilder request = esProvider.getClient() //
          .prepareIndex(getName(), getIndexName(), getValueTableReference() + ":" + summary.getVariable().getName()) //
          .setSource(JsonFormat.printToString(summaryDto));
      bulkRequest.add(request);
    }

    @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
    private void indexSummary(@Nonnull ContinuousVariableSummary summary, @Nonnull BulkRequestBuilder bulkRequest) {
      try {

        DescriptiveStatistics descriptiveStats = summary.getDescriptiveStats();

        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

        builder.startObject("summary");
        builder.field("nature", "continuous");
        builder.field("distribution", summary.getDistribution().name());
        builder.field("intervals", summary.getIntervals());
        builder.field("n", descriptiveStats.getN());
        builder.field("min", descriptiveStats.getMin());
        builder.field("max", descriptiveStats.getMax());
        builder.field("mean", descriptiveStats.getMean());
        builder.field("geometricMean", descriptiveStats.getGeometricMean());
        builder.field("sum", descriptiveStats.getSum());
        builder.field("sumsq", descriptiveStats.getSumsq());
        builder.field("stdDev", descriptiveStats.getStandardDeviation());
        builder.field("variance", descriptiveStats.getVariance());
        builder.field("skewness", descriptiveStats.getSkewness());
        builder.field("kurtosis", descriptiveStats.getKurtosis());
        builder.field("median", descriptiveStats.apply(new Median()));
        builder.field("values", descriptiveStats.getValues());
        builder.field("percentiles", summary.getPercentiles());
        builder.field("distributionPercentiles", summary.getDistributionPercentiles());

        builder.startArray("intervalFrequencies");
        for(IntervalFrequency.Interval frequency : summary.getIntervalFrequencies()) {
          builder.startObject() //
              .field("lower", frequency.getLower()) //
              .field("upper", frequency.getUpper()) //
              .field("freq", frequency.getFreq()) //
              .field("density", frequency.getDensity()) //
              .field("densityPct", frequency.getDensityPct()) //
              .endObject();
        }
        builder.endArray(); // intervalFrequencies

        builder.endObject(); // summary
        builder.endObject();

        String variableReference = getValueTableReference() + ":" + summary.getVariable().getName();
        bulkRequest
            .add(esProvider.getClient().prepareIndex(getName(), getIndexName(), variableReference).setSource(builder));
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}

