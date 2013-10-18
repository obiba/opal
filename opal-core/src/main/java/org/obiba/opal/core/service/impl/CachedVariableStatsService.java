package org.obiba.opal.core.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.obiba.core.util.TimedExecution;
import org.obiba.magma.Timestamped;
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
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

@Component
public class CachedVariableStatsService implements VariableStatsService {

  private static final Logger log = LoggerFactory.getLogger(CachedVariableStatsService.class);

  private static final String CONTINUOUS_CACHE = "opal.variable.summary.continuous";

  private static final String CATEGORICAL_CACHE = "opal.variable.summary.categorical";

  // Map<TableReference, Map<VariableName, CategoricalVariableSummary.Builder>>
  private final Map<String, Map<String, CategoricalVariableSummary.Builder>> categoricalSummaryBuilders = Collections
      .synchronizedMap(new HashMap<String, Map<String, CategoricalVariableSummary.Builder>>());

  // Map<TableReference, Map<VariableName, ContinuousVariableSummary.Builder>>
  private final Map<String, Map<String, ContinuousVariableSummary.Builder>> continuousSummaryBuilders = Collections
      .synchronizedMap(new HashMap<String, Map<String, ContinuousVariableSummary.Builder>>());

  private final CacheManager cacheManager;

  public CachedVariableStatsService() {
    cacheManager = CacheManager.create(Resources.getResource("opal-ehcache.xml"));
  }

  @Override
  public void stackVariable(@Nonnull ValueTable valueTable, @Nonnull Variable variable, @Nonnull Value value) {
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
  public void computeSummaries(@Nonnull ValueTable table) {
    TimedExecution timedExecution = new TimedExecution().start();
    computeAndCacheCategoricalSummaries(table);
    computeAndCacheContinuousSummaries(table);
    clearComputingSummaries(table);
    log.info("Variables summaries for {} computed in {}", table.getTableReference(),
        timedExecution.end().formatExecutionTime());
  }

  private void computeAndCacheCategoricalSummaries(ValueTable table) {
    Cache cache = cacheManager.getCache(CATEGORICAL_CACHE);
    for(CategoricalVariableSummary.Builder summaryBuilder : getCategoricalSummaryBuilders(table)) {
      CategoricalVariableSummary summary = summaryBuilder.build();
      String key = summary.getCacheKey(table);
      log.debug("Add to cache {}", key);
      cache.put(new Element(key, summary));
    }
  }

  private void computeAndCacheContinuousSummaries(ValueTable table) {
    Cache cache = cacheManager.getCache(CONTINUOUS_CACHE);
    for(ContinuousVariableSummary.Builder summaryBuilder : getContinuousSummaryBuilders(table)) {
      ContinuousVariableSummary summary = summaryBuilder.build();
      String key = summary.getCacheKey(table);
      log.debug("Add to cache {}", key);
      cache.put(new Element(key, summary));
    }
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

  @Nonnull
  @Override
  public CategoricalVariableSummary getCategoricalSummary(@Nonnull CategoricalVariableSummaryFactory summaryFactory) {
    Variable variable = summaryFactory.getVariable();
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    log.debug("Get categorical summary for {}", variable.getName());

    // don't cache transient variable summary
    if("_transient".equals(variable.getName())) {
      return summaryFactory.getSummary();
    }

    return getCached(summaryFactory);
  }

  private CategoricalVariableSummary getCached(CategoricalVariableSummaryFactory summaryFactory) {
    Cache cache = cacheManager.getCache(CATEGORICAL_CACHE);
    String key = summaryFactory.getCacheKey();
    Element element = cache.get(key);
    log.debug("Cache for {}: {}", key, element);

    if(element == null) {
      return cacheCategoricalSummary(summaryFactory, cache, key);
    }

    // check timestamps
    if(isCacheObsolete(element, summaryFactory.getTable())) {
      return cacheCategoricalSummary(summaryFactory, cache, key);
    }
    return (CategoricalVariableSummary) element.getObjectValue();
  }

  private CategoricalVariableSummary cacheCategoricalSummary(CategoricalVariableSummaryFactory summaryFactory,
      Cache cache, String key) {
    CategoricalVariableSummary summary = summaryFactory.getSummary();
    cache.put(new Element(key, summary));
    return summary;
  }

  @Nonnull
  @Override
  public ContinuousVariableSummary getContinuousSummary(@Nonnull ContinuousVariableSummaryFactory summaryFactory) {
    Variable variable = summaryFactory.getVariable();
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    log.debug("Get continuous summary for {}", variable.getName());

    // don't cache transient variable summary
    if("_transient".equals(variable.getName())) {
      return summaryFactory.getSummary();
    }

    return getCached(summaryFactory);
  }

  private ContinuousVariableSummary getCached(ContinuousVariableSummaryFactory summaryFactory) {
    Cache cache = cacheManager.getCache(CONTINUOUS_CACHE);
    String key = summaryFactory.getCacheKey();
    Element element = cache.get(key);
    log.debug("Cache for {}: {}", key, element);

    if(element == null) {
      return cacheContinuousSummary(summaryFactory, cache, key);
    }

    // check timestamps
    if(isCacheObsolete(element, summaryFactory.getTable())) {
      // element.getCreationTime() will be updated
      return cacheContinuousSummary(summaryFactory, cache, key);
    }
    return (ContinuousVariableSummary) element.getObjectValue();
  }

  private ContinuousVariableSummary cacheContinuousSummary(ContinuousVariableSummaryFactory summaryFactory, Cache cache,
      String key) {
    ContinuousVariableSummary summary = summaryFactory.getSummary();
    cache.put(new Element(key, summary));
    return summary;
  }

  private boolean isCacheObsolete(Element element, Timestamped table) {
    Date creationTime = new Date(element.getCreationTime());
    Date tableLastUpdate = (Date) table.getTimestamps().getLastUpdate().getValue();
    return tableLastUpdate != null && tableLastUpdate.after(creationTime);
  }

}
