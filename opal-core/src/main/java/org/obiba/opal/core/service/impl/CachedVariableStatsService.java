package org.obiba.opal.core.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.obiba.core.util.TimedExecution;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary;
import org.obiba.opal.core.service.VariableStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

@Component
public class CachedVariableStatsService implements VariableStatsService {

  private static final Logger log = LoggerFactory.getLogger(CachedVariableStatsService.class);

  // Map<TableReference, Map<VariableName, CategoricalVariableSummary.Builder>>
  private final Map<String, Map<String, CategoricalVariableSummary.Builder>> categoricalSummaryBuilders = Maps
      .newHashMap();

  // Map<TableReference, Map<VariableName, ContinuousVariableSummary.Builder>>
  private final Map<String, Map<String, ContinuousVariableSummary.Builder>> continuousSummaryBuilders = Maps
      .newHashMap();

  @Override
  public void computeVariable(@Nonnull ValueTable valueTable, @Nonnull Variable variable, @Nonnull Value value) {
    // skip binary variable
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    addValueToCategoricalSummaryBuilder(valueTable, variable, value);
    addValueToContinuousSummaryBuilder(valueTable, variable, value);
  }

  private void addValueToCategoricalSummaryBuilder(ValueTable valueTable, Variable variable, Value value) {
    getCategoricalSummaryBuilder(valueTable, variable).addValue(value);
  }

  private void addValueToContinuousSummaryBuilder(ValueTable valueTable, Variable variable, Value value) {
    if(variable.getValueType().isNumeric()) {
      getContinuousSummaryBuilder(valueTable, variable).addValue(value);
    }
  }

  @Nonnull
  private CategoricalVariableSummary.Builder getCategoricalSummaryBuilder(@Nonnull ValueTable valueTable,
      @Nonnull Variable variable) {
    Map<String, CategoricalVariableSummary.Builder> buildersByVariable = categoricalSummaryBuilders
        .get(valueTable.getTableReference());
    if(buildersByVariable == null) {
      buildersByVariable = Maps.newHashMap();
      categoricalSummaryBuilders.put(valueTable.getTableReference(), buildersByVariable);
    }
    CategoricalVariableSummary.Builder builder = buildersByVariable.get(variable.getName());
    if(builder == null) {
      boolean distinct = TextType.get().equals(variable.getValueType()) && variable.areAllCategoriesMissing();
      builder = new CategoricalVariableSummary.Builder(variable).distinct(distinct);
      buildersByVariable.put(variable.getName(), builder);
    }
    return builder;
  }

  @Nonnull
  private ContinuousVariableSummary.Builder getContinuousSummaryBuilder(@Nonnull ValueTable valueTable,
      @Nonnull Variable variable) {
    Map<String, ContinuousVariableSummary.Builder> buildersByVariable = continuousSummaryBuilders
        .get(valueTable.getTableReference());
    if(buildersByVariable == null) {
      buildersByVariable = Maps.newHashMap();
      continuousSummaryBuilders.put(valueTable.getTableReference(), buildersByVariable);
    }
    ContinuousVariableSummary.Builder builder = buildersByVariable.get(variable.getName());
    if(builder == null) {
      builder = new ContinuousVariableSummary.Builder(variable, ContinuousVariableSummary.Distribution.normal);
      buildersByVariable.put(variable.getName(), builder);
    }
    return builder;
  }

  @Override
  public void computeSummaries(@Nonnull ValueTable valueTable) {

    //TODO lock maps
    TimedExecution timedExecution = new TimedExecution().start();

    // categorical summaries
    for(CategoricalVariableSummary.Builder summaryBuilder : getCategoricalSummaryBuilders(valueTable)) {
      CategoricalVariableSummary summary = summaryBuilder.build();
      //TODO cache summary
    }

    // continuous summaries
    for(ContinuousVariableSummary.Builder summaryBuilder : getContinuousSummaryBuilders(valueTable)) {
      ContinuousVariableSummary summary = summaryBuilder.build();
      //TODO cache summary
    }

    clearComputingSummaries(valueTable);

    log.info("Variables summaries for {} computed in {}", valueTable.getTableReference(),
        timedExecution.end().formatExecutionTime());
  }

  private Iterable<CategoricalVariableSummary.Builder> getCategoricalSummaryBuilders(@Nonnull ValueTable valueTable) {
    Map<String, CategoricalVariableSummary.Builder> buildersByVariable = categoricalSummaryBuilders
        .get(valueTable.getTableReference());
    return buildersByVariable == null
        ? Collections.<CategoricalVariableSummary.Builder>emptyList()
        : buildersByVariable.values();
  }

  private Iterable<ContinuousVariableSummary.Builder> getContinuousSummaryBuilders(@Nonnull ValueTable valueTable) {
    Map<String, ContinuousVariableSummary.Builder> buildersByVariable = continuousSummaryBuilders
        .get(valueTable.getTableReference());
    return buildersByVariable == null
        ? Collections.<ContinuousVariableSummary.Builder>emptyList()
        : buildersByVariable.values();
  }

  @Override
  public void clearComputingSummaries(@Nonnull ValueTable valueTable) {
    categoricalSummaryBuilders.remove(valueTable.getTableReference());
    continuousSummaryBuilders.remove(valueTable.getTableReference());
  }

  // TODO cache if !"_transient".equals(getVariable().getName()) && !summary.isFiltered()
  @Nonnull
  @Override
  public CategoricalVariableSummary getCategoricalSummary(@Nonnull Variable variable, @Nonnull ValueTable table,
      @Nonnull ValueSource valueSource, boolean distinct, Integer offset, Integer limit) {
    log.debug("Compute categorical summary for {}", variable.getName());

    return new CategoricalVariableSummary.Builder(variable) //
        .distinct(distinct) //
        .filter(offset, limit) //
        .addTable(table, valueSource) //
        .build();
  }

  // TODO cache if !"_transient".equals(getVariable().getName()) && !summary.isFiltered()
  @Nonnull
  @Override
  public ContinuousVariableSummary getContinuousSummary(@Nonnull Variable variable, @Nonnull ValueTable table,
      @Nonnull ValueSource valueSource, @Nonnull ContinuousVariableSummary.Distribution distribution,
      List<Double> percentiles, int intervals, Integer offset, Integer limit) {
    log.debug("Compute continuous summary for {}", variable.getName());

    return new ContinuousVariableSummary.Builder(variable, distribution) //
        .defaultPercentiles(percentiles) //
        .intervals(intervals) //
        .filter(offset, limit) //
        .addTable(table, valueSource) //
        .build();
  }

}
