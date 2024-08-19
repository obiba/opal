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

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.math.Distribution;
import org.obiba.magma.math.summary.*;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.core.service.VariableSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Component
public class CachedVariableSummaryService implements VariableSummaryService, SystemService {

  private static final Logger log = LoggerFactory.getLogger(CachedVariableSummaryService.class);

  @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.cache.variableSummaries}")
  private boolean cacheSummaries;

  private final Map<Class<? extends VariableSummaryFactory<?>>, AbstractVariableSummaryCachedService<?, ?, ?>>
      summaryServices = new HashMap<>();

  public CachedVariableSummaryService() {
    summaryServices.put(ContinuousVariableSummaryFactory.class, new ContinuousVariableSummaryCachedService());
    summaryServices.put(CategoricalVariableSummaryFactory.class, new CategoricalVariableSummaryCachedService());
    summaryServices.put(DefaultVariableSummaryFactory.class, new DefaultVariableSummaryCachedService());
    summaryServices.put(BinaryVariableSummaryFactory.class, new BinaryVariableSummaryCachedService());
    summaryServices.put(TextVariableSummaryFactory.class, new TextVariableSummaryCachedService());
    summaryServices.put(GeoVariableSummaryFactory.class, new GeoVariableSummaryCachedService());
  }

  @SuppressWarnings("unchecked")
  private <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>, //
      TVariableSummaryBuilder extends AbstractVariableSummary.VariableSummaryBuilder<TVariableSummary, TVariableSummaryBuilder>> //
  AbstractVariableSummaryCachedService<TVariableSummary, TVariableSummaryFactory, TVariableSummaryBuilder> //
  getService(Class<? extends VariableSummaryFactory<TVariableSummary>> factoryClass) {
    return (AbstractVariableSummaryCachedService<TVariableSummary, TVariableSummaryFactory, TVariableSummaryBuilder>) summaryServices
        .get(factoryClass);
  }

  @Override
  public void stackVariable(@NotNull ValueTable valueTable, @NotNull Variable variable, @NotNull Value value) {
    if (!cacheSummaries) log.debug("Variable summaries cache disabled!");

    // skip binary variable
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    getService(CategoricalVariableSummaryFactory.class).getSummaryBuilder(valueTable, variable).addValue(value);

    if (variable.getValueType().isNumeric()) {
      getService(ContinuousVariableSummaryFactory.class).getSummaryBuilder(valueTable, variable).addValue(value);
    } else {
      getService(DefaultVariableSummaryFactory.class).getSummaryBuilder(valueTable, variable).addValue(value);
    }
  }

  @Override
  public void computeSummaries(@NotNull ValueTable table) {
    if (!cacheSummaries) {
      log.info("Variable summaries cache disabled!");
      return;
    }
    Stopwatch stopwatch = Stopwatch.createStarted();
    for (AbstractVariableSummaryCachedService<?, ?, ?> summaryService : summaryServices.values()) {
      summaryService.computeAndCacheSummaries(table);
    }
    clearComputingSummaries(table);
    log.info("Variables summaries for {} computed in {}", table.getTableReference(), stopwatch.stop());
  }

  @Override
  public void clearComputingSummaries(@NotNull ValueTable valueTable) {
    if (!cacheSummaries) return;
    for (AbstractVariableSummaryCachedService<?, ?, ?> summaryService : summaryServices.values()) {
      summaryService.clearComputingSummaries(valueTable);
    }
  }

  @NotNull
  @Override
  @SuppressWarnings("unchecked")
  public <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>> TVariableSummary getSummary(
      @NotNull VariableSummaryFactory<TVariableSummary> summaryFactory, boolean refreshCache) {
    return getService((Class<TVariableSummaryFactory>) summaryFactory.getClass())
        .getSummary(summaryFactory, refreshCache);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>> boolean isSummaryCached(
      @NotNull VariableSummaryFactory<TVariableSummary> summaryFactory) {
    return getService((Class<TVariableSummaryFactory>) summaryFactory.getClass()).isSummaryCached(summaryFactory);
  }

  @Override
  public void start() {
    summaryServices.values().forEach(AbstractVariableSummaryCachedService::loadCache);
  }

  @Override
  public void stop() {
    summaryServices.values().forEach(AbstractVariableSummaryCachedService::saveCache);
  }

  private class ContinuousVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<ContinuousVariableSummary, ContinuousVariableSummaryFactory, ContinuousVariableSummary.Builder> {

    @NotNull
    @Override
    protected String getCacheName() {
      return "opal-variable-summary-continuous";
    }

    @NotNull
    @Override
    protected ContinuousVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new ContinuousVariableSummary.Builder(variable, Distribution.normal);
    }
  }

  private class CategoricalVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<CategoricalVariableSummary, CategoricalVariableSummaryFactory, CategoricalVariableSummary.Builder> {

    @NotNull
    @Override
    protected String getCacheName() {
      return "opal-variable-summary-categorical";
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

    @NotNull
    @Override
    protected String getCacheName() {
      return "opal-variable-summary-default";
    }

    @NotNull
    @Override
    protected DefaultVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new DefaultVariableSummary.Builder(variable);
    }
  }

  private class BinaryVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<BinaryVariableSummary, BinaryVariableSummaryFactory, BinaryVariableSummary.Builder> {

    @NotNull
    @Override
    protected String getCacheName() {
      return "opal-variable-summary-binary";
    }

    @NotNull
    @Override
    protected BinaryVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new BinaryVariableSummary.Builder(variable);
    }
  }

  private class TextVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<TextVariableSummary, TextVariableSummaryFactory, TextVariableSummary.Builder> {

    @NotNull
    @Override
    protected String getCacheName() {
      return "opal-variable-summary-text";
    }

    @NotNull
    @Override
    protected TextVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new TextVariableSummary.Builder(variable);
    }
  }

  private class GeoVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<GeoVariableSummary, GeoVariableSummaryFactory, GeoVariableSummary.Builder> {

    @NotNull
    @Override
    protected String getCacheName() {
      return "opal-variable-summary-geo";
    }

    @NotNull
    @Override
    protected GeoVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new GeoVariableSummary.Builder(variable);
    }
  }
}
