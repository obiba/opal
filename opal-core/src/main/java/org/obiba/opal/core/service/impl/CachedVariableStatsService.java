package org.obiba.opal.core.service.impl;

import javax.validation.constraints.NotNull;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.core.magma.math.CategoricalVariableSummaryFactory;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummaryFactory;
import org.obiba.opal.core.service.VariableStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;

@Component
public class CachedVariableStatsService implements VariableStatsService {

  private static final Logger log = LoggerFactory.getLogger(CachedVariableStatsService.class);

  @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.cache.variableSummaries}")
  private boolean cacheSummaries;

  private final CacheManager cacheManager;

  private final ContinuousVariableSummaryCachedService continuousSummaryService;

  private final CategoricalVariableSummaryCachedService categoricalSummaryService;

  public CachedVariableStatsService() {
    cacheManager = CacheManager.create(Resources.getResource("ehcache.xml"));
    continuousSummaryService = new ContinuousVariableSummaryCachedService();
    categoricalSummaryService = new CategoricalVariableSummaryCachedService();
  }

  @Override
  public void stackVariable(@NotNull ValueTable valueTable, @NotNull Variable variable, @NotNull Value value) {
    if(!cacheSummaries) log.debug("Variable summaries cache disabled!");

    // skip binary variable
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    categoricalSummaryService.getSummaryBuilder(valueTable, variable).addValue(value);

    if(variable.getValueType().isNumeric()) {
      continuousSummaryService.getSummaryBuilder(valueTable, variable).addValue(value);
    }
  }

  @Override
  public void computeSummaries(@NotNull ValueTable table) {
    if(!cacheSummaries) {
      log.info("Variable summaries cache disabled!");
      return;
    }
    Stopwatch stopwatch = Stopwatch.createStarted();
    continuousSummaryService.computeAndCacheSummaries(table);
    categoricalSummaryService.computeAndCacheSummaries(table);
    clearComputingSummaries(table);
    log.info("Variables summaries for {} computed in {}", table.getTableReference(), stopwatch.stop());
  }

  @Override
  public void clearComputingSummaries(@NotNull ValueTable valueTable) {
    if(!cacheSummaries) return;
    continuousSummaryService.clearComputingSummaries(valueTable);
    categoricalSummaryService.clearComputingSummaries(valueTable);
  }

  @NotNull
  @Override
  public CategoricalVariableSummary getCategoricalSummary(@NotNull CategoricalVariableSummaryFactory summaryFactory) {
    return categoricalSummaryService.getSummary(summaryFactory);
  }

  @NotNull
  @Override
  public ContinuousVariableSummary getContinuousSummary(@NotNull ContinuousVariableSummaryFactory summaryFactory) {
    return continuousSummaryService.getSummary(summaryFactory);
  }

  private class ContinuousVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<ContinuousVariableSummary, ContinuousVariableSummaryFactory, ContinuousVariableSummary.Builder> {

    private static final String CACHE_NAME = "opal-variable-summary-continuous";

    @NotNull
    @Override
    protected Cache getCache() {
      return cacheManager.getCache(CACHE_NAME);
    }

    @NotNull
    @Override
    protected ContinuousVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new ContinuousVariableSummary.Builder(variable, ContinuousVariableSummary.Distribution.normal);
    }
  }

  private class CategoricalVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<CategoricalVariableSummary, CategoricalVariableSummaryFactory, CategoricalVariableSummary.Builder> {

    private static final String CACHE_NAME = "opal-variable-summary-categorical";

    @NotNull
    @Override
    protected Cache getCache() {
      return cacheManager.getCache(CACHE_NAME);
    }

    @NotNull
    @Override
    protected CategoricalVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      boolean distinct = TextType.get().equals(variable.getValueType()) && variable.areAllCategoriesMissing();
      return new CategoricalVariableSummary.Builder(variable).distinct(distinct);
    }
  }

}
