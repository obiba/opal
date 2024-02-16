/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.math.*;
import org.obiba.magma.math.summary.support.*;
import org.obiba.magma.type.*;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Get summary statistics directly from resource's tibble R symbol.
 */
class ResourceVectorSummarySource implements VectorSummarySource {

  private static final Logger log = LoggerFactory.getLogger(ResourceVectorSummarySource.class);

  private final ResourceVariableValueSource resourceVariableValueSource;

  private final Iterable<VariableEntity> entities;

  private final List<String> missings;

  private final String idsSymbol = "ids_" + Math.abs(new Random().nextInt());

  public ResourceVectorSummarySource(ResourceVariableValueSource resourceVariableValueSource, Iterable<VariableEntity> entities) {
    this.resourceVariableValueSource = resourceVariableValueSource;
    this.entities = entities;
    this.missings = resourceVariableValueSource.getVariable().hasCategories() ? resourceVariableValueSource.getVariable().getCategories().stream()
        .filter(Category::isMissing)
        .map(Category::getName)
        .collect(Collectors.toList()) : Lists.newArrayList();
  }

  @Nullable
  @Override
  public FrequenciesSummary asFrequenciesSummary(boolean detailed) {
    try {
      // just get the frequencies
      DefaultFrequenciesSummary summary = new DefaultFrequenciesSummary();

      String columnName = getColumnName();
      List<RServerResult> freqResults = queryDetailedFrequencies(columnName);

      int freqSum = freqResults.stream()
          .filter(RServerResult::isNamedList)
          .map(freqResult -> freqResult.asNamedList().get("n").asIntegers()[0])
          .mapToInt(Integer::intValue)
          .sum();

      // observations
      int notNullCount = 0;
      int n = 0;
      for (RNamedList<RServerResult> freqMap : freqResults.stream()
          .filter(RServerResult::isNamedList)
          .map(RServerResult::asNamedList)
          .collect(Collectors.toList())) {
        int count = freqMap.get("n").asIntegers()[0];
        n += count;
        if (freqMap.get(columnName).isNull()) {
          summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NULL_NAME, count, count * 1F / freqSum, true));
        } else if (detailed) {
          String value = freqMap.get(columnName).asStrings()[0];
          summary.addFrequency(new DefaultFrequency(value, count, count * 1F / freqSum, missings.contains(value)));
        } else {
          String value = freqMap.get(columnName).asStrings()[0];
          if (missings.contains(value)) {
            summary.addFrequency(new DefaultFrequency(value, count, count * 1F / freqSum, true));
          } else
            notNullCount += count;
        }
      }
      if (!detailed) {
        summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NOT_NULL_NAME, notNullCount, notNullCount * 1F / freqSum, false));
      }
      summary.setN(n);

      // most frequents first
      summary.sortFrequencies();

      return summary;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("Failed at getting summary from resource", e);
      else
        log.error("Failed at getting summary from resource: {}", e.getMessage());
      return null;
    }
  }

  @Nullable
  @Override
  public CategoricalSummary asCategoricalSummary(Set<Category> categories) {
    try {
      DefaultCategoricalSummary summary = new DefaultCategoricalSummary();

      String columnName = getColumnName();
      List<RServerResult> freqResults = queryDetailedFrequencies(columnName);

      int freqSum = freqResults.stream()
          .filter(RServerResult::isNamedList)
          .map(freqResult -> freqResult.asNamedList().get("n").asIntegers()[0])
          .mapToInt(Integer::intValue)
          .sum();

      // expected
      List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());
      boolean isLogical = resourceVariableValueSource.getValueType().equals(BooleanType.get());
      if (isLogical) {
        if (!categoryNames.contains("true")) categoryNames.add("true");
        if (!categoryNames.contains("false")) categoryNames.add("false");
      }

      // observations
      int n = 0;
      int otherFreq = 0;
      int max = 0;
      for (RNamedList<RServerResult> freqMap : freqResults.stream()
          .filter(RServerResult::isNamedList)
          .map(RServerResult::asNamedList)
          .collect(Collectors.toList())) {
        int count = freqMap.get("n").asIntegers()[0];
        n += count;
        if (freqMap.get(columnName).isNull()) {
          summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NULL_NAME, count, count * 1F / freqSum, true));
        } else {
          String value = freqMap.get(columnName).asStrings()[0];
          if (isLogical) {
            if (freqMap.get(columnName).isInteger())
              value = freqMap.get(columnName).asIntegers()[0] == 1 ? "true" : "false";
            else
              value = String.format("%s", freqMap.get(columnName).asLogical());
          }
          if (categoryNames.isEmpty() || categoryNames.contains(value)) {
            summary.addFrequency(new DefaultFrequency(value, count, count * 1F / freqSum, missings.contains(value)));
            if (count > max) {
              max = count;
              summary.setMode(value);
            }
          } else {
            otherFreq = otherFreq + count;
          }
        }
      }
      summary.setN(n);
      summary.setOtherFrequency(otherFreq);

      return summary;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("Failed at getting summary from resource", e);
      else
        log.error("Failed at getting summary from resource: {}", e.getMessage());
      return null;
    }
  }

  @Nullable
  @Override
  public ContinuousSummary asContinuousSummary(Distribution distribution, List<Double> defaultPercentiles, int intervals, Set<Category> categories) {
    assignIds();
    ResourceContinuousSummary summary = new ResourceContinuousSummary();

    String columnName = getColumnName();
    List<RServerResult> descResults = queryDescriptiveStatistics(columnName, categories);
    RNamedList<RServerResult> extendedDescResults = null;
    try {
      extendedDescResults = queryExtendedDescriptiveStatistics(columnName, categories);
    } catch (Exception e) {
      log.warn("Some descriptive statistics could not be queried", e);
    }
    List<RServerResult> freqResults = queryDefaultFrequencies(columnName, categories);
    RNamedList<RServerResult> histResult = queryHistogram(columnName, intervals, categories);
    List<RServerResult> percentileResults = queryPercentiles(columnName, defaultPercentiles, categories);
    rmIds();

    summary.setStats(descResults, extendedDescResults);
    percentileResults.stream()
        .map(percentile -> percentile.isNull() ? 0 : percentile.asDoubles()[0])
        .forEach(summary::addPercentile);
    if (histResult != null) {
      List<RServerResult> breaks = histResult.get("breaks").asList();
      if (histResult.get("counts").isList()) {
        List<RServerResult> counts = histResult.get("counts").asList();
        List<RServerResult> density = histResult.get("density").asList();
        for (int i = 0; i < counts.size(); i++) {
          DefaultInterval interval = new DefaultInterval();
          interval.setLower(breaks.get(i).asDoubles()[0]);
          interval.setUpper(breaks.get(i + 1).asDoubles()[0]);
          interval.setFreq(counts.get(i).asIntegers()[0]);
          interval.setDensity(density.get(i).asDoubles()[0]);
          summary.addIntervalFrequency(interval);
        }
      } else if (histResult.get("counts").isInteger()) {
        DefaultInterval interval = new DefaultInterval();
        interval.setLower(breaks.get(0).asDoubles()[0]);
        interval.setUpper(breaks.get(1).asDoubles()[0]);
        interval.setFreq(histResult.get("counts").asIntegers()[0]);
        interval.setDensity(histResult.get("density").asDoubles()[0]);
        summary.addIntervalFrequency(interval);
      }
    }

    int freqSum = freqResults.stream()
        .filter(RServerResult::isNamedList)
        .map(freqResult -> freqResult.asNamedList().get("n").asIntegers()[0])
        .mapToInt(Integer::intValue)
        .sum();

    for (RNamedList<RServerResult> freqMap : freqResults.stream()
        .filter(RServerResult::isNamedList)
        .map(RServerResult::asNamedList)
        .collect(Collectors.toList())) {
      int count = freqMap.get("n").asIntegers()[0];
      boolean isNA = freqMap.get("na").asLogical();
      if (freqMap.get("na").isInteger())
        isNA = freqMap.get("na").asIntegers()[0] == 1;
      if (isNA) {
        summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NULL_NAME, count, count * 1F / freqSum, true));
      } else {
        summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NOT_NULL_NAME, count, count * 1F / freqSum, false));
      }
    }

    return summary;
  }

  @Nullable
  @Override
  public GeoSummary asGeoSummary() {
    try {
      // just get the frequencies
      DefaultGeoSummary summary = new DefaultGeoSummary();
      String columnName = getColumnName();
      List<RServerResult> freqResults = queryDetailedFrequencies(columnName);

      int freqSum = freqResults.stream()
          .filter(RServerResult::isNamedList)
          .map(freqResult -> freqResult.asNamedList().get("n").asIntegers()[0])
          .mapToInt(Integer::intValue)
          .sum();

      // observations
      int n = 0;
      int notNullCount = 0;
      List<Coordinate> coords = Lists.newArrayList();
      for (RNamedList<RServerResult> freqMap : freqResults.stream()
          .filter(RServerResult::isNamedList)
          .map(RServerResult::asNamedList)
          .collect(Collectors.toList())) {
        int count = freqMap.get("n").asIntegers()[0];
        n += count;
        if (freqMap.get(columnName).isNull()) {
          summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NULL_NAME, count, count * 1F / freqSum, true));
        } else {
          String value = freqMap.get(columnName).asStrings()[0];
          if (missings.contains(value))
            summary.addFrequency(new DefaultFrequency(value, count, count * 1F / freqSum, missings.contains(value)));
          else {
            addCoordinates(coords, resourceVariableValueSource.getVariable().getValueType().valueOf(value));
            notNullCount += count;
          }
        }
      }
      summary.addCoordinates(coords);
      summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NOT_NULL_NAME, notNullCount, notNullCount * 1F / freqSum, false));
      summary.setN(n);

      return summary;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("Failed at getting summary from resource", e);
      else
        log.error("Failed at getting summary from resource: {}", e.getMessage());
      return null;
    }
  }

  //
  // Private methods
  //

  private void addCoordinates(List<Coordinate> coords, Value value) {
    if (value.getValueType() == PointType.get()) {
      coords.add((Coordinate) value.getValue());
    } else if (value.getValueType() == LineStringType.get()) {
      coords.addAll((Collection<Coordinate>) value.getValue());
    } else if (value.getValueType() == PolygonType.get()) {
      Collection<List<Coordinate>> coordinateList = (Collection<List<Coordinate>>) value.getValue();
      for (List<Coordinate> coordinate : coordinateList) {
        coords.addAll(coordinate);
      }
    }
  }

  private RNamedList<RServerResult> queryHistogram(String columnName, int intervals, Set<Category> categories) {
    String histStatement = String.format("hist(%s %s %%>%% select(`%s`) %%>%% pull(), breaks = %s, plot = FALSE)",
        getTibbleStatement(columnName),
        getFilterMissingsStatement(columnName, categories),
        columnName, intervals);
    String cmd = String.format("is.null(base::assign('x', %s))", histStatement);
    try {
      execute(cmd);
      cmd = "list(breaks = x$breaks, counts = x$counts, density = x$density)";
      RServerResult result = execute(cmd);
      return result.asNamedList();
    } catch (Exception e) {
      // will fail if there are only NAs for instance
      return null;
    }
  }

  private List<RServerResult> queryPercentiles(String columnName, List<Double> defaultPercentiles, Set<Category> categories) {
    String probs = defaultPercentiles.stream()
        .map(pct -> pct / 100 + "")
        .collect(Collectors.joining(","));
    String cmd = String.format("quantile(%s %s %%>%% select(`%s`) %%>%% pull(), prob = c(%s), na.rm = TRUE)",
        getTibbleStatement(columnName),
        getFilterMissingsStatement(columnName, categories),
        columnName, probs);
    RServerResult result = execute(cmd);
    return result.asList();
  }

  private List<RServerResult> queryDescriptiveStatistics(String columnName, Set<Category> categories) {
    String cmd = String.format("%s %s %%>%% .resource.get_descriptive_stats(`%s`)",
        getTibbleStatement(columnName),
        getFilterMissingsStatement(columnName, categories),
        columnName);
    RServerResult result = execute(cmd);
    return result.asList();
  }

  private RNamedList<RServerResult> queryExtendedDescriptiveStatistics(String columnName, Set<Category> categories) {
    String cmd = String.format("%s %s %%>%% .resource.get_ext_descriptive_stats(`%s`)",
        getTibbleStatement(columnName),
        getFilterMissingsStatement(columnName, categories),
        columnName);
    RServerResult result = execute(cmd);
    if (result.isList())
      return result.asList().get(0).asNamedList();
    return result.asNamedList();
  }

  private List<RServerResult> queryDefaultFrequencies(String columnName, Set<Category> categories) {
    // TODO include missing categories
    String cmd = String.format("%s %%>%% .resource.get_default_frequencies(`%s`)",
        getTibbleStatement(columnName), columnName);
    RServerResult result = execute(cmd);
    return result.asList();
  }

  private List<RServerResult> queryDetailedFrequencies(String columnName) {
    assignIds();
    String cmd = String.format("%s %%>%% .resource.get_detailed_frequencies(`%s`)",
        getTibbleStatement(columnName),
        columnName);
    RServerResult result = execute(cmd);
    rmIds();
    return result.asList();
  }

  private void assignIds() {
    String idsVector = StreamSupport.stream(entities.spliterator(), false)
        .map(e -> (RVariableEntity) e)
        .map(e -> e.isNumeric() ? e.getRIdentifier() : String.format("\"%s\"", e.getRIdentifier()))
        .collect(Collectors.joining(","));
    idsVector = String.format("c(%s)", idsVector);
    execute(String.format("is.null(base::assign(\"%s\", %s))", idsSymbol, idsVector));
  }

  private void rmIds() {
    execute(String.format("base::rm(\"%s\")", idsSymbol));
  }

  private RServerResult execute(String script) {
    return resourceVariableValueSource.getValueTable().execute(script);
  }

  private String getSymbol() {
    return resourceVariableValueSource.getValueTable().getConnector().getSymbol();
  }

  private String getColumnName() {
    return resourceVariableValueSource.getValueTable().getColumnName(resourceVariableValueSource.getVariable());
  }

  private String getIdColumn() {
    return resourceVariableValueSource.getValueTable().getIdColumn();
  }

  private String getTibbleStatement(String columnName) {
    String symbolWithMutateStatement = resourceVariableValueSource.hasColumn() ?
        getSymbol() :
        String.format("%s %%>%% mutate(`%s` = %s)", getSymbol(), columnName, getMutateStatement());
    return isAllIds() ? symbolWithMutateStatement :
        String.format("%s %%>%% filter(`%s` %%in%% %s)", symbolWithMutateStatement, getIdColumn(), idsSymbol);
  }

  private String getMutateStatement() {
//    if (resourceVariableValueSource.hasScript()) {
//      String script = resourceVariableValueSource.getScript();
//      return "null".equals(script) ? getNAForValueType() : script;
//    }
    return getNAForValueType();
  }

  private String getNAForValueType() {
    if (resourceVariableValueSource.getValueType().equals(IntegerType.get()))
      return "NA_integer_";
    if (resourceVariableValueSource.getValueType().equals(DecimalType.get()))
      return "NA_real_";
    return "NA";
  }

  private boolean isAllIds() {
    long count = entities instanceof Collection ?
        ((Collection<VariableEntity>) entities).size() :
        StreamSupport.stream(entities.spliterator(), false).count();
    return resourceVariableValueSource.getValueTable().getVariableEntityCount() == count;
  }

  private String getFilterMissingsStatement(String columnName, Set<Category> categories) {
    String filterStatement = "";
    if (categories != null) {
      String missings = categories.stream()
          .filter(Category::isMissing)
          .map(Category::getName).collect(Collectors.joining(", "));
      if (!Strings.isNullOrEmpty(missings)) {
        filterStatement = String.format("%%>%% filter(!(`%s` %%in%% c(%s)))", columnName, missings);
      }
    }
    return filterStatement;
  }

  public static class ResourceContinuousSummary extends DefaultContinuousSummary {

    public void setStats(List<RServerResult> descResults, RNamedList<RServerResult> extendedDescResults) {
      setMean(getStat(descResults, "mean"));
      setMin(getStat(descResults, "min"));
      setMax(getStat(descResults, "max"));
      setSum(getStat(descResults, "sum"));
      setSumsq(getStat(descResults, "sumsq"));
      setGeometricMean(getStat(descResults, "geomean"));
      setVariance(getStat(descResults, "variance"));
      setStandardDeviation(getStat(descResults, "stddev"));

      if (extendedDescResults != null && !extendedDescResults.getNames().isEmpty()) {
        setMedian(extendedDescResults.get("median").isNull() ? Double.NaN : extendedDescResults.get("median").asDoubles()[0]);
        setSkewness(extendedDescResults.get("skewness").isNull() ? Double.NaN : extendedDescResults.get("skewness").asDoubles()[0]);
        setKurtosis(extendedDescResults.get("kurtosis").isNull() ? Double.NaN : extendedDescResults.get("kurtosis").asDoubles()[0]);
      } else {
        setMedian(Double.NaN);
        setSkewness(Double.NaN);
        setKurtosis(Double.NaN);
      }

      try {
        setN(descResults.stream()
            .filter(RServerResult::isNamedList)
            .map(RServerResult::asNamedList)
            .filter(map -> !map.get("n").isNull())
            .map(map -> map.get("n").asIntegers()[0])
            .findFirst().orElse(0));
      } catch (Exception e) {
        // ignored
      }
    }

    private double getStat(List<RServerResult> descResults, String type) {
      try {
        return descResults.stream()
            .filter(RServerResult::isNamedList)
            .map(RServerResult::asNamedList)
            .filter(map -> !map.get(type).isNull())
            .map(map -> map.get(type).asDoubles()[0])
            .findFirst().orElse(0d);
      } catch (Exception e) {
        return 0d;
      }
    }

  }
}
