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

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
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
import org.obiba.opal.web.model.Opal.LocaleDto;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Utilities for manipulating Magma Dto instances
 */
@SuppressWarnings({ "OverlyCoupledClass", "UnusedDeclaration" })
public final class Dtos {

  public static final Function<VariableEntity, VariableEntityDto> variableEntityAsDtoFunc
      = new Function<VariableEntity, VariableEntityDto>() {

    @Override
    public VariableEntityDto apply(VariableEntity from) {
      return asDto(from).build();
    }

  };

  public static final Function<VariableDto, Variable> variableFromDtoFunc = new Function<VariableDto, Variable>() {

    @Override
    public Variable apply(VariableDto from) {
      return fromDto(from);
    }

  };

  private Dtos() {}

  public static Function<Variable, VariableDto.Builder> asDtoFunc(final LinkDto tableLink) {
    return new Function<Variable, VariableDto.Builder>() {

      private int index = 0;

      @Override
      public VariableDto.Builder apply(Variable from) {
        return asDto(tableLink, from, index++);
      }
    };
  }

  public static VariableDto.Builder asDto(@Nullable LinkDto tableLink, Variable from, @Nullable Integer index) {
    VariableDto.Builder var = VariableDto.newBuilder().setName(from.getName()).setEntityType(from.getEntityType())
        .setValueType(from.getValueType().getName()).setIsRepeatable(from.isRepeatable());
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
    if(index != null) {
      var.setIndex(index);
    }
    return var;
  }

  public static VariableDto.Builder asDto(@Nullable LinkDto tableLink, Variable from) {
    return asDto(tableLink, from, null);
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
      //noinspection ConstantConditions
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
      builder.setVariableCount(Iterables.size(valueTable.getVariables()));
    }

    if(withValueSetCount) {
      builder.setValueSetCount(valueTable.getVariableEntities().size());
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

  @SuppressWarnings("ConstantConditions")
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

  public static ValueSetsDto.ValueDto.Builder asDto(Value value) {
    return asDto(null, value);
  }

  public static ValueSetsDto.ValueDto.Builder asDto(@Nullable String link, Value value) {
    return asDto(link, value, false);
  }

  @SuppressWarnings("ConstantConditions")
  public static ValueSetsDto.ValueDto.Builder asDto(@Nullable String link, Value value, boolean filterBinary) {
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
    return ValueSetsDto.ValueSetDto.newBuilder().setIdentifier(valueSet.getVariableEntity().getIdentifier());
  }

  public static LocaleDto asDto(Locale locale, @Nullable Locale displayLocale) {
    LocaleDto.Builder builder = LocaleDto.newBuilder().setName(locale.toString());

    if(displayLocale != null) {
      builder.setDisplay(locale.getDisplayName(displayLocale));
    }

    return builder.build();
  }

  private static final class FilteredToStringFunction implements Function<Object, String> {

    private static final Function<Object, String> INSTANCE = new FilteredToStringFunction();

    @Override
    public String apply(Object o) {
      Value input = (Value) o;
      if(input.getValueType() == BinaryType.get()) {
        return "byte[" + (input.isNull() ? 0 : 1) + "]";
      }
      return input.toString();
    }

  }

}
