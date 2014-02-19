package org.obiba.opal.core.service;

import javax.validation.constraints.NotNull;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.math.summary.CategoricalVariableSummary;
import org.obiba.magma.math.summary.CategoricalVariableSummaryFactory;
import org.obiba.magma.math.summary.ContinuousVariableSummary;
import org.obiba.magma.math.summary.ContinuousVariableSummaryFactory;
import org.obiba.magma.math.summary.DefaultVariableSummary;
import org.obiba.magma.math.summary.DefaultVariableSummaryFactory;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

@Component
public class CachedVariableStatsService implements VariableStatsService {

  private static final Logger log = LoggerFactory.getLogger(CachedVariableStatsService.class);

  @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.cache.variableSummaries}")
  private boolean cacheSummaries;

  @Autowired
  private CacheManager cacheManager;

  private final ContinuousVariableSummaryCachedService continuousSummaryService
      = new ContinuousVariableSummaryCachedService();

  private final CategoricalVariableSummaryCachedService categoricalSummaryService
      = new CategoricalVariableSummaryCachedService();

  private final DefaultVariableSummaryCachedService defaultSummaryService = new DefaultVariableSummaryCachedService();

  @Override
  public void stackVariable(@NotNull ValueTable valueTable, @NotNull Variable variable, @NotNull Value value) {
    if(!cacheSummaries) log.debug("Variable summaries cache disabled!");

    // skip binary variable
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    categoricalSummaryService.getSummaryBuilder(valueTable, variable).addValue(value);

    if(variable.getValueType().isNumeric()) {
      continuousSummaryService.getSummaryBuilder(valueTable, variable).addValue(value);
    } else {
      defaultSummaryService.getSummaryBuilder(valueTable, variable).addValue(value);
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
    defaultSummaryService.computeAndCacheSummaries(table);
    clearComputingSummaries(table);
    log.info("Variables summaries for {} computed in {}", table.getTableReference(), stopwatch.stop());
  }

  @Override
  public void clearComputingSummaries(@NotNull ValueTable valueTable) {
    if(!cacheSummaries) return;
    continuousSummaryService.clearComputingSummaries(valueTable);
    categoricalSummaryService.clearComputingSummaries(valueTable);
    defaultSummaryService.clearComputingSummaries(valueTable);
  }

  @NotNull
  @Override
  public CategoricalVariableSummary getCategoricalSummary(@NotNull CategoricalVariableSummaryFactory summaryFactory,
      boolean refreshCache) {
    return categoricalSummaryService.getSummary(summaryFactory, refreshCache);
  }

  @NotNull
  @Override
  public ContinuousVariableSummary getContinuousSummary(@NotNull ContinuousVariableSummaryFactory summaryFactory,
      boolean refreshCache) {
    return continuousSummaryService.getSummary(summaryFactory, refreshCache);
  }

  @NotNull
  @Override
  public DefaultVariableSummary getDefaultSummary(@NotNull DefaultVariableSummaryFactory summaryFactory,
      boolean refreshCache) {
    return defaultSummaryService.getSummary(summaryFactory, refreshCache);
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

  private class DefaultVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<DefaultVariableSummary, DefaultVariableSummaryFactory, DefaultVariableSummary.Builder> {

    private static final String CACHE_NAME = "opal-variable-summary-default";

    @NotNull
    @Override
    protected Cache getCache() {
      return cacheManager.getCache(CACHE_NAME);
    }

    @NotNull
    @Override
    protected DefaultVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new DefaultVariableSummary.Builder(variable);
    }
  }

}
