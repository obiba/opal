/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.summary;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javax.validation.constraints.NotNull;


import org.obiba.magma.AttributeAware;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.math.summary.BinaryVariableSummaryFactory;
import org.obiba.magma.math.summary.CategoricalVariableSummary;
import org.obiba.magma.math.summary.CategoricalVariableSummaryFactory;
import org.obiba.magma.math.summary.ContinuousVariableSummary;
import org.obiba.magma.math.summary.ContinuousVariableSummaryFactory;
import org.obiba.magma.math.summary.DefaultVariableSummary;
import org.obiba.magma.math.summary.DefaultVariableSummaryFactory;
import org.obiba.magma.math.summary.GeoVariableSummary;
import org.obiba.magma.math.summary.GeoVariableSummaryFactory;
import org.obiba.magma.math.summary.TextVariableSummary;
import org.obiba.magma.math.summary.TextVariableSummaryFactory;
import org.obiba.magma.math.summary.VariableSummary;
import org.obiba.magma.math.summary.VariableSummaryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

import static org.obiba.magma.math.summary.AbstractVariableSummary.VariableSummaryBuilder;

public abstract class AbstractVariableSummaryCachedService< //
    TVariableSummary extends VariableSummary, //
    TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>, //
    TVariableSummaryBuilder extends VariableSummaryBuilder<TVariableSummary, TVariableSummaryBuilder>> {

  private static final Logger log = LoggerFactory.getLogger(AbstractVariableSummaryCachedService.class);

  private final Cache<String, VariableSummary> summaryCache = CacheBuilder.newBuilder().build();

  // Map<TableReference, Map<VariableName, TVariableSummaryBuilder>>
  private final Map<String, Map<String, TVariableSummaryBuilder>> summaryBuilders = Collections
      .synchronizedMap(new HashMap<String, Map<String, TVariableSummaryBuilder>>());

  @NotNull
  protected abstract TVariableSummaryBuilder newVariableSummaryBuilder(@NotNull Variable variable);

  protected abstract String getCacheName();

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
    Iterable<TVariableSummaryBuilder> variableSummaryBuilders = getSummaryBuilders(table);
    for(TVariableSummaryBuilder summaryBuilder : variableSummaryBuilders) {
      String variableName = summaryBuilder.getVariable().getName();
      log.debug("Compute {} summary", variableName);
      TVariableSummary summary = summaryBuilder.build();
      String key = summary.getCacheKey(table);
      log.trace("Cache {} {} summary with key '{}'", variableName, getSummaryType(summary), key);
      summaryCache.put(key, summary);
    }
  }

  protected void clearComputingSummaries(@NotNull ValueTable valueTable) {
    summaryBuilders.remove(valueTable.getTableReference());
  }

  @NotNull
  public TVariableSummary getSummary(@NotNull TVariableSummaryFactory summaryFactory, boolean refreshCache) {
    Variable variable = summaryFactory.getVariable();

    log.debug("Get {} summary for {}", getSummaryType(summaryFactory), variable.getName());

    // don't cache transient variable summary
    if(isTransientVariable(variable)) {
      log.trace("Don't cache transient variable summary for {}", variable.getName());
      return summaryFactory.getSummary();
    }

    if(refreshCache) {
      log.trace("Force summary cache refresh for {}", variable.getName());
      clearVariableSummaryCache(summaryFactory);
    }

    return getCached(summaryFactory);
  }

  public boolean isSummaryCached(@NotNull TVariableSummaryFactory summaryFactory) {
    String key = summaryFactory.getCacheKey();
    boolean inCache = summaryCache.getIfPresent(key) != null;
    log.trace("{} summary {} cache {} ({})", StringUtils.capitalize(getSummaryType(summaryFactory)),
        inCache ? "IN" : "NOT IN", summaryFactory.getVariable().getName(), key);
    return inCache;
  }

  private boolean isTransientVariable(AttributeAware variable) {
    return variable.hasAttribute("opal", "transient") &&
        (boolean) variable.getAttribute("opal", "transient").getValue().getValue();
  }

  private void clearVariableSummaryCache(@NotNull TVariableSummaryFactory summaryFactory) {
    String key = summaryFactory.getCacheKey();
    summaryCache.invalidate(key);
  }

  @SuppressWarnings("unchecked")
  private TVariableSummary getCached(TVariableSummaryFactory summaryFactory) {
    String key = summaryFactory.getCacheKey();
    try {
      return (TVariableSummary) summaryCache.get(key, (Callable) () -> {
        String variableName = summaryFactory.getVariable().getName();
        String summaryType = getSummaryType(summaryFactory);
        log.debug("No {} summary in cache for {} ({})", summaryType, variableName, key);
        return summaryFactory.getSummary();
      });
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO eviction strategy
  private boolean isCacheObsolete(Date creationTime, Timestamped table) {
    Value lastUpdate = table.getTimestamps().getLastUpdate();
    return !lastUpdate.isNull() && ((Date) lastUpdate.getValue()).after(creationTime);
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  private String getSummaryType(TVariableSummary summary) {
    if(summary instanceof ContinuousVariableSummary) {
      return "continuous";
    }
    if(summary instanceof CategoricalVariableSummary) {
      return "categorical";
    }
    if(summary instanceof DefaultVariableSummary) {
      return "default";
    }
    if(summary instanceof TextVariableSummary) {
      return "text";
    }
    if(summary instanceof GeoVariableSummary) {
      return "geo";
    }
    throw new IllegalArgumentException("Unsupported summary class " + summary.getClass());
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  private String getSummaryType(TVariableSummaryFactory summaryFactory) {
    if(summaryFactory instanceof ContinuousVariableSummaryFactory) {
      return "continuous";
    }
    if(summaryFactory instanceof CategoricalVariableSummaryFactory) {
      return "categorical";
    }
    if(summaryFactory instanceof DefaultVariableSummaryFactory) {
      return "default";
    }
    if(summaryFactory instanceof TextVariableSummaryFactory) {
      return "text";
    }
    if(summaryFactory instanceof BinaryVariableSummaryFactory) {
      return "binary";
    }
    if(summaryFactory instanceof GeoVariableSummaryFactory) {
      return "geo";
    }
    throw new IllegalArgumentException("Unsupported factory class " + summaryFactory.getClass());
  }

}
