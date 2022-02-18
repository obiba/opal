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

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.magma.*;
import org.obiba.magma.math.*;
import org.obiba.magma.math.summary.support.*;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;
import org.obiba.opal.spi.resource.TabularResourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ResourceVariableValueSource extends AbstractRVariableValueSource {

  private static final Logger log = LoggerFactory.getLogger(ResourceVariableValueSource.class);

  private Variable variable;

  private final ResourceView valueTable;

  private final TabularResourceConnector.Column column;

  public ResourceVariableValueSource(Variable variable, TabularResourceConnector.Column column, ResourceView valueTable) {
    this.variable = variable;
    this.column = column;
    this.valueTable = valueTable;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public Value getValue(ValueSet valueSet) {
    // note: no column name vs. variable name mapping for now (such info could be one of the variable's attribute?)
    // or could also be derived from an R script
    Map<Integer, List<Object>> columnValues = ((RValueSet) valueSet).getValuesByPosition();
    if (!columnValues.containsKey(column.getPosition()))
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    return getValue(columnValues.get(column.getPosition()));
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @Override
  public VectorSource asVectorSource() throws VectorSourceNotSupportedException {
    return new ResourceVectorSource();
  }

  //
  // Private methods
  //


  //
  // Private classes
  //

  private class ResourceVectorSource implements VectorSource {

    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @Override
    public Iterable<Value> getValues(Iterable<VariableEntity> entities) {
      return valueTable.getConnector().getColumn(valueTable.getColumnName(variable)).asVector(variable.getValueType(), valueTable.getIdColumn(), entities);
    }

    @Override
    public boolean supportVectorSummary() {
      return true;
    }

    @Override
    public VectorSummarySource getVectorSummarySource(Iterable<VariableEntity> entities) throws VectorSummarySourceNotSupportedException {
      VectorSummarySource summary = new ResourceVectorSummarySource(entities);
      return summary;
    }

  }

  private class ResourceVectorSummarySource implements VectorSummarySource {

    private final Iterable<VariableEntity> entities;

    private final List<String> missings;

    private final String idsSymbol = "ids_" + Math.abs(new Random().nextInt());

    public ResourceVectorSummarySource(Iterable<VariableEntity> entities) {
      this.entities = entities;
      this.missings = variable.hasCategories() ? variable.getCategories().stream()
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
          if (isNull(freqMap.get(columnName).asNativeJavaObject())) {
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
          if (isNull(freqMap.get(columnName).asNativeJavaObject())) {
            summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NULL_NAME, count, count * 1F / freqSum, true));
          } else {
            String value = freqMap.get(columnName).asStrings()[0];
            if (categoryNames.contains(value)) {
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
      assignIds(entities);
      String columnName = getColumnName();
      List<RServerResult> descResults = queryDescriptiveStatistics(columnName);
      List<RServerResult> freqResults = queryDefaultFrequencies(columnName);
      rmIds();

      ResourceContinuousSummary summary = new ResourceContinuousSummary();

      summary.setStats(descResults);

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
        if (freqMap.get("na").asLogical()) {
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
          if (isNull(freqMap.get(columnName).asNativeJavaObject())) {
            summary.addFrequency(new DefaultFrequency(FrequenciesSummary.NULL_NAME, count, count * 1F / freqSum, true));
          } else {
            String value = freqMap.get(columnName).asStrings()[0];
            if (missings.contains(value))
              summary.addFrequency(new DefaultFrequency(value, count, count * 1F / freqSum, missings.contains(value)));
            else {
              addCoordinates(coords, variable.getValueType().valueOf(value));
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

    private String getColumnName() {
      return valueTable.getColumnName(variable);
    }

    private List<RServerResult> queryDescriptiveStatistics(String columnName) {
      String cmd = String.format("%s %%>%% filter(`%s` %%in%% %s, !is.na(`%s`)) %%>%% select(`%s`) %%>%% summarise(" +
              "mean = mean(`%s`)," +
              "n = n()," +
              "min = min(`%s`)," +
              "max = max(`%s`)," +
              "median = median(`%s`)," +
              "geomean = exp(mean(log(`%s`)))," +
              "stddev = sd(`%s`)," +
              "variance = var(`%s`)," +
              "skewness = moments::skewness(`%s`)," +
              "kurtosis = moments::kurtosis(`%s`)," +
              "sum = sum(`%s`)," +
              "sumsq = sum((`%s`)^2))",
          valueTable.getConnector().getSymbol(),
          valueTable.getIdColumn(), idsSymbol, columnName,
          columnName, columnName,
          columnName, columnName, columnName, columnName, columnName, columnName, columnName, columnName, columnName, columnName);
      RServerResult result = valueTable.execute(cmd);
      return result.asList();
    }

    private List<RServerResult> queryDefaultFrequencies(String columnName) {
      String cmd = String.format("%s %%>%% filter(`%s` %%in%% %s) %%>%% group_by(na = is.na(`%s`)) %%>%% summarise(n = n())",
          valueTable.getConnector().getSymbol(),
          valueTable.getIdColumn(), idsSymbol, columnName,
          columnName);
      RServerResult result = valueTable.execute(cmd);
      return result.asList();
    }

    private List<RServerResult> queryDetailedFrequencies(String columnName) {
      assignIds(entities);
      String cmd = String.format("%s %%>%% filter(`%s` %%in%% %s) %%>%% select(`%s`) %%>%% group_by(`%s`) %%>%% summarise(n = n())",
          valueTable.getConnector().getSymbol(),
          valueTable.getIdColumn(), idsSymbol,
          columnName, columnName);
      RServerResult result = valueTable.execute(cmd);
      rmIds();
      return result.asList();
    }

    private boolean isNull(Object objValue) {
      return objValue == null || JSONObject.NULL.equals(objValue) || objValue.toString().equals("NA");
    }

    private void assignIds(Iterable<VariableEntity> entities) {
      String idsVector = StreamSupport.stream(entities.spliterator(), false)
          .map(e -> (RVariableEntity) e)
          .map(e -> e.isNumeric() ? e.getRIdentifier() : String.format("\"%s\"", e.getRIdentifier()))
          .collect(Collectors.joining(","));
      idsVector = String.format("c(%s)", idsVector);
      valueTable.execute(String.format("base::assign(\"%s\", %s)", idsSymbol, idsVector));
    }

    private void rmIds() {
      valueTable.execute(String.format("base::rm(\"%s\")", idsSymbol));
    }
  }

  public static class ResourceContinuousSummary extends DefaultContinuousSummary {


    public void setStats(List<RServerResult> descResults) {
      setMin(getStat(descResults, "min"));
      setMax(getStat(descResults, "max"));
      setSum(getStat(descResults, "sum"));
      setSumsq(getStat(descResults, "sumsq"));
      setMean(getStat(descResults, "mean"));
      setMedian(getStat(descResults, "median"));
      setGeometricMean(getStat(descResults, "geomean"));
      setVariance(getStat(descResults, "variance"));
      setStandardDeviation(getStat(descResults, "stddev"));
      setSkewness(getStat(descResults, "skewness"));
      setKurtosis(getStat(descResults, "kurtosis"));

      try {
        setN(descResults.stream()
            .filter(RServerResult::isNamedList)
            .map(RServerResult::asNamedList)
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
            .map(map -> map.get(type).asDoubles()[0])
            .findFirst().orElse(0d);
      } catch (Exception e) {
        return 0d;
      }
    }

  }
}
