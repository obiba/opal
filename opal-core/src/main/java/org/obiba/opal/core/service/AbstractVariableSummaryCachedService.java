package org.obiba.opal.core.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.obiba.magma.AttributeAware;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.core.magma.math.VariableSummary;
import org.obiba.opal.core.magma.math.VariableSummaryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import static org.obiba.opal.core.magma.math.AbstractVariableSummary.VariableSummaryBuilder;

public abstract class AbstractVariableSummaryCachedService< //
    TVariableSummary extends VariableSummary, //
    TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>, //
    TVariableSummaryBuilder extends VariableSummaryBuilder<TVariableSummary>> {

  private static final Logger log = LoggerFactory.getLogger(AbstractVariableSummaryCachedService.class);

  // Map<TableReference, Map<VariableName, TVariableSummaryBuilder>>
  private final Map<String, Map<String, TVariableSummaryBuilder>> summaryBuilders = Collections
      .synchronizedMap(new HashMap<String, Map<String, TVariableSummaryBuilder>>());

  @NotNull
  protected abstract Cache getCache();

  @NotNull
  protected abstract TVariableSummaryBuilder newVariableSummaryBuilder(@NotNull Variable variable);

  @NotNull
  protected TVariableSummaryBuilder getSummaryBuilder(@NotNull ValueTable valueTable, @NotNull Variable variable) {
    Map<String, TVariableSummaryBuilder> buildersByVariable = summaryBuilders.get(valueTable.getTableReference());
    if(buildersByVariable == null) {
      buildersByVariable = Maps.newHashMap();
      summaryBuilders.put(valueTable.getTableReference(), buildersByVariable);
    }
    TVariableSummaryBuilder builder = buildersByVariable.get(variable.getName());
    if(builder == null) {
      builder = newVariableSummaryBuilder(variable);
      buildersByVariable.put(variable.getName(), builder);
    }
    return builder;
  }

  private Iterable<TVariableSummaryBuilder> getSummaryBuilders(@NotNull ValueTable valueTable) {
    Map<String, TVariableSummaryBuilder> buildersByVariable = summaryBuilders.get(valueTable.getTableReference());
    return buildersByVariable == null ? Collections.<TVariableSummaryBuilder>emptyList() : buildersByVariable.values();
  }

  protected void computeAndCacheSummaries(ValueTable table) {
    Cache cache = getCache();
    for(TVariableSummaryBuilder summaryBuilder : getSummaryBuilders(table)) {
      log.debug("Compute {} summary", summaryBuilder.getVariable());
      TVariableSummary summary = summaryBuilder.build();
      String key = summary.getCacheKey(table);
      log.debug("Add to cache {}", key);
      cache.put(new Element(key, summary));
    }
  }

  protected void clearComputingSummaries(@NotNull ValueTable valueTable) {
    summaryBuilders.remove(valueTable.getTableReference());
  }

  @NotNull
  public TVariableSummary getSummary(@NotNull TVariableSummaryFactory summaryFactory, boolean refreshCache) {
    Variable variable = summaryFactory.getVariable();
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    log.debug("Get summary for {}", variable.getName());

    // don't cache transient variable summary
    if(isTransientVariable(variable)) {
      return summaryFactory.getSummary();
    }

    if(refreshCache) {
      clearVariableSummaryCache(summaryFactory);
    }

    return getCached(summaryFactory);
  }

  private boolean isTransientVariable(AttributeAware variable) {
    return variable.hasAttribute("opal", "transient") &&
        (boolean) variable.getAttribute("opal", "transient").getValue().getValue();
  }

  private void clearVariableSummaryCache(@NotNull TVariableSummaryFactory summaryFactory) {
    Cache cache = getCache();
    String key = summaryFactory.getCacheKey();
    cache.remove(key);
  }

  @SuppressWarnings("unchecked")
  private TVariableSummary getCached(TVariableSummaryFactory summaryFactory) {
    Cache cache = getCache();
    String key = summaryFactory.getCacheKey();
    Element element = cache.get(key);
    log.debug("Cache for {}: {}", key, element);

    if(element == null) {
      return cacheSummary(summaryFactory, cache, key);
    }

    // check timestamps
    if(isCacheObsolete(element, summaryFactory.getTable())) {
      return cacheSummary(summaryFactory, cache, key);
    }
    return (TVariableSummary) element.getObjectValue();
  }

  private TVariableSummary cacheSummary(TVariableSummaryFactory summaryFactory, Ehcache cache, String key) {
    TVariableSummary summary = summaryFactory.getSummary();

    log.debug("New cache entry for : {}", key);
    cache.put(new Element(key, summary));
    return summary;
  }

  private boolean isCacheObsolete(Element element, Timestamped table) {
    Date creationTime = new Date(element.getCreationTime());
    Value lastUpdate = table.getTimestamps().getLastUpdate();
    return !lastUpdate.isNull() && ((Date) lastUpdate.getValue()).after(creationTime);
  }
}
