/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Coordinate;
import org.obiba.magma.Datasource;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.math.stat.IntervalFrequency;
import org.obiba.magma.math.summary.BinaryVariableSummary;
import org.obiba.magma.math.summary.CategoricalVariableSummary;
import org.obiba.magma.math.summary.ContinuousVariableSummary;
import org.obiba.magma.math.summary.DefaultVariableSummary;
import org.obiba.magma.math.summary.GeoVariableSummary;
import org.obiba.magma.math.summary.TextVariableSummary;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.AttributeDto;
import org.obiba.opal.web.model.Magma.CategoryDto;
import org.obiba.opal.web.model.Magma.DatasourceDto;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.obiba.opal.web.model.Math;
import org.obiba.opal.web.model.Opal.LocaleDto;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Utilities for manipulating Magma Dto instances
 */
@SuppressWarnings("OverlyCoupledClass")
public final class Dtos {

  // private static final Logger log = LoggerFactory.getLogger(Dtos.class);

  public static final Function<VariableEntity, VariableEntityDto> variableEntityAsDtoFunc
      = new Function<VariableEntity, VariableEntityDto>() {

    @Override
    public VariableEntityDto apply(VariableEntity from) {
      return asDto(from).build();
    }

  };

  private Dtos() {}

  public static Function<Variable, VariableDto.Builder> asDtoFunc(final LinkDto tableLink) {
    return new Function<Variable, VariableDto.Builder>() {

      @Override
      public VariableDto.Builder apply(Variable from) {
        return asDto(tableLink, from);
      }
    };
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  public static VariableDto.Builder asDto(@Nullable LinkDto tableLink, Variable from) {
    VariableDto.Builder var = VariableDto.newBuilder().setName(from.getName()).setEntityType(from.getEntityType())
        .setValueType(from.getValueType().getName()).setIsRepeatable(from.isRepeatable()).setIndex(from.getIndex());
    if(from.getOccurrenceGroup() != null) {
      var.setOccurrenceGroup(from.getOccurrenceGroup());
    }
    if(from.getMimeType() != null) {
      var.setMimeType(from.getMimeType());
    }
    if(from.getUnit() != null) {
      var.setUnit(from.getUnit());
    }
    if(from.getReferencedEntityType() != null) {
      var.setReferencedEntityType(from.getReferencedEntityType());
    }
    for(Attribute attribute : from.getAttributes()) {
      var.addAttributes(asDto(attribute));
    }
    for(Category category : from.getCategories()) {
      var.addCategories(asDto(category));
    }
    if(tableLink != null) {
      var.setParentLink(tableLink);
    }
//    if(index != null) {
//      var.setIndex(index);
//    }
    return var;
  }

  public static VariableDto.Builder asDto(Variable from) {
    return asDto(null, from);
  }

  public static CategoryDto.Builder asDto(Category from) {
    CategoryDto.Builder c = CategoryDto.newBuilder().setName(from.getName()).setIsMissing(from.isMissing());
    for(Attribute attribute : from.getAttributes()) {
      c.addAttributes(asDto(attribute));
    }
    return c;
  }

  @SuppressWarnings("ConstantConditions")
  public static AttributeDto.Builder asDto(Attribute from) {
    AttributeDto.Builder a = AttributeDto.newBuilder().setName(from.getName()).setValue(from.getValue().toString());
    if(from.isLocalised()) {
      a.setLocale(from.getLocale().toString());
    }
    if(from.hasNamespace()) {
      a.setNamespace(from.getNamespace());
    }
    return a;
  }

  public static Variable fromDto(VariableDto variableDto) {
    Variable.Builder builder = Variable.Builder
        .newVariable(variableDto.getName(), ValueType.Factory.forName(variableDto.getValueType()),
            variableDto.getEntityType());
    for(CategoryDto category : variableDto.getCategoriesList()) {
      builder.addCategory(fromDto(category));
    }

    for(AttributeDto attribute : variableDto.getAttributesList()) {
      builder.addAttribute(fromDto(attribute));
    }

    if(variableDto.getOccurrenceGroup() != null) {
      builder.occurrenceGroup(variableDto.getOccurrenceGroup());
    }

    if(variableDto.getMimeType() != null) {
      builder.mimeType(variableDto.getMimeType());
    }

    if(variableDto.getReferencedEntityType() != null) {
      builder.referencedEntityType(variableDto.getReferencedEntityType());
    }

    if(variableDto.getUnit() != null) {
      builder.unit(variableDto.getUnit());
    }

    if(variableDto.getIsRepeatable()) {
      builder.repeatable();
    }

    builder.index(variableDto.getIndex());

    return builder.build();
  }

  public static Category fromDto(Magma.CategoryDto categoryDto) {
    Category.Builder builder = Category.Builder.newCategory(categoryDto.getName()).missing(categoryDto.getIsMissing());
    for(AttributeDto attributeDto : categoryDto.getAttributesList()) {
      builder.addAttribute(fromDto(attributeDto));
    }
    return builder.build();
  }

  public static Attribute fromDto(AttributeDto attributeDto) {
    Attribute.Builder builder = Attribute.Builder.newAttribute(attributeDto.getName());
    if(attributeDto.hasLocale()) {
      builder.withValue(new Locale(attributeDto.getLocale()), attributeDto.getValue());
    } else {
      builder.withValue(attributeDto.getValue());
    }
    if(attributeDto.hasNamespace()) {
      builder.withNamespace(attributeDto.getNamespace());
    }

    return builder.build();
  }

  public static TableDto.Builder asDto(ValueTable valueTable) {
    return asDto(valueTable, true);
  }

  public static TableDto.Builder asDto(ValueTable valueTable, boolean withCounts) {
    return asDto(valueTable, withCounts, withCounts);
  }

  public static TableDto.Builder asDto(ValueTable valueTable, boolean withVariableCounts, boolean withValueSetCount) {
    TableDto.Builder builder = TableDto.newBuilder() //
        .setName(valueTable.getName()) //
        .setEntityType(valueTable.getEntityType());

    if(withVariableCounts) {
      builder.setVariableCount(valueTable.getVariableCount());
    }

    if(withValueSetCount) {
      builder.setValueSetCount(valueTable.getVariableEntityCount());
    }

    Magma.TimestampsDto.Builder tsBuilder = asDto(valueTable.getTimestamps());
    if(tsBuilder != null) {
      builder.setTimestamps(tsBuilder);
    }

    builder.setDatasourceName(valueTable.getDatasource().getName());
    String link = "/datasource/" + valueTable.getDatasource().getName() + "/table/" + valueTable.getName();
    builder.setLink(link);
    if(valueTable.isView()) {
      builder.setViewLink(link.replaceFirst("/table/", "/view/"));
    }

    return builder;
  }

  public static Magma.TimestampsDto.Builder asDto(Timestamps ts) {
    Magma.TimestampsDto.Builder tsBuilder = Magma.TimestampsDto.newBuilder();
    if(!ts.getCreated().isNull()) {
      tsBuilder.setCreated(ts.getCreated().toString());
    }
    if(!ts.getLastUpdate().isNull()) {
      tsBuilder.setLastUpdate(ts.getLastUpdate().toString());
    }
    return tsBuilder;
  }

  public static DatasourceDto.Builder asDto(Datasource datasource) {
    Magma.DatasourceDto.Builder builder = Magma.DatasourceDto.newBuilder()//
        .setName(datasource.getName())//
        .setType(datasource.getType());

    List<String> tableNames = Lists.newArrayList();
    List<String> viewNames = Lists.newArrayList();
    for(ValueTable table : datasource.getValueTables()) {
      tableNames.add(table.getName());
      if(table.isView()) {
        viewNames.add(table.getName());
      }
    }
    Collections.sort(tableNames);
    Collections.sort(viewNames);
    builder.addAllTable(tableNames);
    builder.addAllView(viewNames);

    addTimestamps(builder, datasource);

    return builder;
  }

  public static Value fromDto(ValueSetsDto.ValueDto valueDto, final ValueType type, boolean isSequence) {
    if(isSequence) {
      if(valueDto.getValuesCount() == 0) {
        return type.nullSequence();
      }
      return type.sequenceOf(ImmutableList
          .copyOf(Iterables.transform(valueDto.getValuesList(), new Function<ValueSetsDto.ValueDto, Value>() {

            @Override
            public Value apply(ValueSetsDto.ValueDto input) {
              return fromDto(input, type, false);
            }
          })));
    } else {
      if(valueDto.hasValue()) {
        return type.valueOf(valueDto.getValue());
      }
      return type.nullValue();
    }

  }

  public static ValueSetsDto.ValueDto.Builder asDto(@NotNull Value value) {
    return asDto(null, value);
  }

  public static ValueSetsDto.ValueDto.Builder asDto(@Nullable String link, @NotNull Value value) {
    return asDto(link, value, false);
  }

  @SuppressWarnings("ConstantConditions")
  public static ValueSetsDto.ValueDto.Builder asDto(@Nullable String link, @NotNull Value value, boolean filterBinary) {
    Function<Object, String> toString = filterBinary ? FilteredToStringFunction.INSTANCE : Functions.toStringFunction();

    ValueSetsDto.ValueDto.Builder valueDto = ValueSetsDto.ValueDto.newBuilder();
    if(!value.isNull()) {
      if(value.isSequence()) {
        int i = 0;
        for(Value v : value.asSequence().getValue()) {
          valueDto.addValues(asDto(link + "?pos=" + i, v, filterBinary));
          i++;
        }
      } else {
        if(filterBinary && value.getValueType() == BinaryType.get()) {
          valueDto.setLength(value.getLength());
          valueDto.setLink(link);
        }
        String valueStr = toString.apply(value);
        valueDto.setValue(valueStr);
      }
    }

    return valueDto;
  }

  public static VariableEntityDto.Builder asDto(VariableEntity from) {
    return VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).setEntityType(from.getType());
  }

  /**
   * Does not add values, nor timestamps.
   *
   * @param valueSet
   * @return
   */
  public static ValueSetsDto.ValueSetDto.Builder asDto(ValueSet valueSet) {
    VariableEntity entity = valueSet.getVariableEntity();
    return ValueSetsDto.ValueSetDto.newBuilder().setIdentifier(entity.getIdentifier());
  }

  public static LocaleDto asDto(Locale locale, @Nullable Locale displayLocale) {
    LocaleDto.Builder builder = LocaleDto.newBuilder().setName(locale.toString());

    if(displayLocale != null) {
      builder.setDisplay(locale.getDisplayName(displayLocale));
    }

    return builder.build();
  }

  public static Math.CategoricalSummaryDto.Builder asDto(CategoricalVariableSummary summary) {
    Math.CategoricalSummaryDto.Builder dtoBuilder = Math.CategoricalSummaryDto.newBuilder() //
        .setMode(summary.getMode()) //
        .setN(summary.getN());
    for(CategoricalVariableSummary.Frequency frequency : summary.getFrequencies()) {
      Math.FrequencyDto.Builder freqBuilder = Math.FrequencyDto.newBuilder() //
          .setValue(frequency.getValue()) //
          .setFreq(frequency.getFreq())//
          .setMissing(frequency.isMissing());

      if(isNumeric(frequency.getPct())) freqBuilder.setPct(frequency.getPct());
      dtoBuilder.addFrequencies(freqBuilder);
    }

    dtoBuilder.setOtherFrequency(summary.getOtherFrequency());
    return dtoBuilder;
  }

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public static Math.ContinuousSummaryDto.Builder asDto(ContinuousVariableSummary summary) {
    DescriptiveStatistics descriptiveStats = summary.getDescriptiveStats();

    Math.DescriptiveStatsDto.Builder descriptiveBuilder = Math.DescriptiveStatsDto.newBuilder()
        .setN(descriptiveStats.getN()).addAllPercentiles(summary.getPercentiles());

    if(isNumeric(descriptiveStats.getMin())) descriptiveBuilder.setMin(descriptiveStats.getMin());
    if(isNumeric(descriptiveStats.getMax())) descriptiveBuilder.setMax(descriptiveStats.getMax());
    if(isNumeric(descriptiveStats.getMean())) descriptiveBuilder.setMean(descriptiveStats.getMean());
    if(isNumeric(descriptiveStats.getSum())) descriptiveBuilder.setSum(descriptiveStats.getSum());
    if(isNumeric(descriptiveStats.getSumsq())) descriptiveBuilder.setSumsq(descriptiveStats.getSumsq());
    if(isNumeric(descriptiveStats.getStandardDeviation())) {
      descriptiveBuilder.setStdDev(descriptiveStats.getStandardDeviation());
    }
    if(isNumeric(descriptiveStats.getVariance())) descriptiveBuilder.setVariance(descriptiveStats.getVariance());
    if(isNumeric(descriptiveStats.getSkewness())) descriptiveBuilder.setSkewness(descriptiveStats.getSkewness());
    if(isNumeric(descriptiveStats.getGeometricMean())) {
      descriptiveBuilder.setGeometricMean(descriptiveStats.getGeometricMean());
    }
    if(isNumeric(descriptiveStats.getKurtosis())) descriptiveBuilder.setKurtosis(descriptiveStats.getKurtosis());
    double median = descriptiveStats.apply(new Median());
    if(isNumeric(median)) descriptiveBuilder.setMedian(median);
    if(isNumeric(descriptiveStats.getVariance())) descriptiveBuilder.setVariance(descriptiveStats.getVariance());

    Math.ContinuousSummaryDto.Builder continuousBuilder = Math.ContinuousSummaryDto.newBuilder()
        .addAllDistributionPercentiles(summary.getDistributionPercentiles());
    for(IntervalFrequency.Interval interval : summary.getIntervalFrequencies()) {
      Math.IntervalFrequencyDto.Builder freqBuilder = Math.IntervalFrequencyDto.newBuilder()
          .setFreq(interval.getFreq());
      if(isNumeric(interval.getLower())) freqBuilder.setLower(interval.getLower());
      if(isNumeric(interval.getUpper())) freqBuilder.setUpper(interval.getUpper());
      if(isNumeric(interval.getDensity())) freqBuilder.setDensity(interval.getDensity());
      if(isNumeric(interval.getDensityPct())) freqBuilder.setDensityPct(interval.getDensityPct());
      continuousBuilder.addIntervalFrequency(freqBuilder);
    }

    for(ContinuousVariableSummary.Frequency frequency : summary.getFrequencies()) {
      Math.FrequencyDto.Builder freqBuilder = Math.FrequencyDto.newBuilder() //
          .setValue(frequency.getValue()) //
          .setFreq(frequency.getFreq())//
          .setMissing(frequency.isMissing());
      if(isNumeric(frequency.getPct())) freqBuilder.setPct(frequency.getPct());
      continuousBuilder.addFrequencies(freqBuilder);
    }

    return continuousBuilder.setSummary(descriptiveBuilder);
  }

  public static Math.DefaultSummaryDto.Builder asDto(DefaultVariableSummary summary) {
    Math.DefaultSummaryDto.Builder dtoBuilder = Math.DefaultSummaryDto.newBuilder() //
        .setN(summary.getN());
    for(DefaultVariableSummary.Frequency frequency : summary.getFrequencies()) {
      Math.FrequencyDto.Builder freqBuilder = Math.FrequencyDto.newBuilder() //
          .setValue(frequency.getValue()) //
          .setFreq(frequency.getFreq())//
          .setMissing(frequency.isMissing());
      if(isNumeric(frequency.getPct())) freqBuilder.setPct(frequency.getPct());
      dtoBuilder.addFrequencies(freqBuilder);
    }
    return dtoBuilder;
  }

  public static Math.BinarySummaryDto.Builder asDto(BinaryVariableSummary summary) {
    Math.BinarySummaryDto.Builder dtoBuilder = Math.BinarySummaryDto.newBuilder() //
        .setN(summary.getN());
    for(BinaryVariableSummary.Frequency frequency : summary.getFrequencies()) {
      Math.FrequencyDto.Builder freqBuilder = Math.FrequencyDto.newBuilder() //
          .setValue(frequency.getValue()) //
          .setFreq(frequency.getFreq())//
          .setMissing(false);
      if(isNumeric(frequency.getPct())) freqBuilder.setPct(frequency.getPct());
      dtoBuilder.addFrequencies(freqBuilder);
    }
    return dtoBuilder;
  }

  public static Math.TextSummaryDto.Builder asDto(TextVariableSummary summary, int maxResults) {
    Math.TextSummaryDto.Builder dtoBuilder = Math.TextSummaryDto.newBuilder() //
        .setN(summary.getN());
    int i = 0;
    // limit to X top text frequencies...
    long otherFrequency = 0;
    for(TextVariableSummary.Frequency frequency : summary.getFrequencies()) {

      Math.FrequencyDto.Builder freqBuilder = Math.FrequencyDto.newBuilder() //
          .setValue(frequency.getValue()) //
          .setFreq(frequency.getFreq())//
          .setMissing(frequency.isMissing());

      if(isNumeric(frequency.getPct())) freqBuilder.setPct(frequency.getPct());

      if("N/A".equals(frequency.getValue())) {
        dtoBuilder.addFrequencies(freqBuilder);
      } else if(i < maxResults) {
        dtoBuilder.addFrequencies(freqBuilder);
        i++;
      } else {
        otherFrequency += frequency.getFreq();
      }
    }

    dtoBuilder.setOtherFrequency(otherFrequency);

    return dtoBuilder;
  }

  public static Math.GeoSummaryDto.Builder asDto(GeoVariableSummary summary) {
    Math.GeoSummaryDto.Builder dtoBuilder = Math.GeoSummaryDto.newBuilder() //
        .setN(summary.getN());
    for(GeoVariableSummary.Frequency frequency : summary.getFrequencies()) {

      Math.FrequencyDto.Builder freqBuilder = Math.FrequencyDto.newBuilder() //
          .setValue(frequency.getValue()) //
          .setFreq(frequency.getFreq())//
          .setMissing(frequency.isMissing());

      if(isNumeric(frequency.getPct())) freqBuilder.setPct(frequency.getPct());
      dtoBuilder.addFrequencies(freqBuilder);
    }

    for(Coordinate coord : summary.getCoordinates()) {
      dtoBuilder.addPoints(Math.PointDto.newBuilder().setLon(coord.getLongitude()).setLat(coord.getLatitude()).build());
    }

    return dtoBuilder;
  }

  private static void addTimestamps(DatasourceDto.Builder builder, Timestamped datasource) {
    Timestamps ts = datasource.getTimestamps();
    Magma.TimestampsDto.Builder tsBuilder = Magma.TimestampsDto.newBuilder();
    if(!ts.getCreated().isNull()) {
      tsBuilder.setCreated(ts.getCreated().toString());
    }
    if(!ts.getLastUpdate().isNull()) {
      tsBuilder.setLastUpdate(ts.getLastUpdate().toString());
    }
    builder.setTimestamps(tsBuilder);
  }

  private static boolean isNumeric(double d) {
    return !Double.isNaN(d) && !Double.isInfinite(d);
  }

  private static final class FilteredToStringFunction implements Function<Object, String> {

    private static final Function<Object, String> INSTANCE = new FilteredToStringFunction();

    @Override
    public String apply(Object o) {
      Value input = (Value) o;
      if(input.getValueType() == BinaryType.get()) {
        return "byte[]";
      }
      return input.toString();
    }

  }

}
