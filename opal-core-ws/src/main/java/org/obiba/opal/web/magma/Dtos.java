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

import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.AttributeDto;
import org.obiba.opal.web.model.Magma.CategoryDto;
import org.obiba.opal.web.model.Magma.DatasourceDto;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.obiba.opal.web.model.Opal.LocaleDto;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Utilities for manipulating Magma Dto instances
 */
public final class Dtos {

  public static final Function<Value, ValueDto> valueAsDtoFunc = new Function<Value, ValueDto>() {

    @Override
    public ValueDto apply(Value from) {
      return asDto(from).build();
    }

  };

  public static final Function<VariableEntity, VariableEntityDto> variableEntityAsDtoFunc = new Function<VariableEntity, VariableEntityDto>() {

    @Override
    public VariableEntityDto apply(VariableEntity from) {
      return asDto(from).build();
    }

  };

  public static Function<Variable, VariableDto> asDtoFunc(final LinkDto tableLink, final UriBuilder uriBuilder) {
    return new Function<Variable, VariableDto>() {

      private int index = 0;

      @Override
      public VariableDto apply(Variable from) {
        return asDto(tableLink, uriBuilder, from, index++).build();
      }
    };
  }

  public static VariableDto.Builder asDto(final LinkDto tableLink, UriBuilder uriBuilder, Variable from, Integer index) {
    VariableDto.Builder var = VariableDto.newBuilder().setName(from.getName()).setEntityType(from.getEntityType()).setValueType(from.getValueType().getName()).setIsRepeatable(from.isRepeatable());
    if(from.getOccurrenceGroup() != null) {
      var.setOccurrenceGroup(from.getOccurrenceGroup());
    }
    if(from.getMimeType() != null) {
      var.setMimeType(from.getMimeType());
    }
    if(from.getUnit() != null) {
      var.setUnit(from.getUnit());
    }
    if(uriBuilder != null) {
      var.setLink(uriBuilder.build(from.getName()).toString());
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

  public static VariableDto.Builder asDto(final LinkDto tableLink, UriBuilder uriBuilder, Variable from) {
    return asDto(tableLink, uriBuilder, from, null);
  }

  public static VariableDto.Builder asDto(Variable from) {
    return asDto(null, null, from);
  }

  public static CategoryDto.Builder asDto(Category from) {
    CategoryDto.Builder c = CategoryDto.newBuilder().setName(from.getName()).setIsMissing(from.isMissing());
    for(Attribute attribute : from.getAttributes()) {
      c.addAttributes(asDto(attribute));
    }
    return c;
  }

  public static AttributeDto.Builder asDto(Attribute from) {
    AttributeDto.Builder a = AttributeDto.newBuilder().setName(from.getName()).setValue(from.getValue().toString());
    if(from.isLocalised()) {
      a.setLocale(from.getLocale().toString());
    }
    return a;
  }

  public static Variable fromDto(VariableDto variableDto) {
    Variable.Builder builder = Variable.Builder.newVariable(variableDto.getName(), ValueType.Factory.forName(variableDto.getValueType()), variableDto.getEntityType());
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
    if(attributeDto.getLocale() != null) {
      builder.withValue(new Locale(attributeDto.getLocale()), attributeDto.getValue());
    } else {
      builder.withValue(attributeDto.getValue());
    }

    return builder.build();
  }

  public static TableDto.Builder asDto(ValueTable valueTable, UriBuilder uriBuilder) {
    TableDto.Builder builder = TableDto.newBuilder() //
    .setName(valueTable.getName()) //
    .setEntityType(valueTable.getEntityType()) //
    .setDatasourceName(valueTable.getDatasource().getName()) //
    .setVariableCount(Iterables.size(valueTable.getVariables())) //
    .setValueSetCount(valueTable.getVariableEntities().size());
    if(uriBuilder != null) {
      builder.setLink(uriBuilder.build(valueTable.getDatasource().getName(), valueTable.getName()).toString());
    }
    if(valueTable.isView()) {
      UriBuilder viewLink = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getView");
      builder.setViewLink(viewLink.build(valueTable.getDatasource().getName(), valueTable.getName()).toString());
    }
    return builder;
  }

  public static DatasourceDto.Builder asDto(Datasource datasource) {
    Magma.DatasourceDto.Builder builder = Magma.DatasourceDto.newBuilder()//
    .setName(datasource.getName())//
    .setType(datasource.getType());

    final List<String> tableNames = Lists.newArrayList();
    final List<String> viewNames = Lists.newArrayList();
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

  public static ValueDto.Builder asDto(Value value) {
    ValueDto.Builder valueBuilder = ValueDto.newBuilder().setValueType(value.getValueType().getName()).setIsSequence(value.isSequence());
    if(value.isNull() == false) {
      valueBuilder.setValue(value.toString());
    }
    return valueBuilder;
  }

  public static VariableEntityDto.Builder asDto(VariableEntity from) {
    return VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).setEntityType(from.getType());
  }

  public static LocaleDto asDto(Locale locale, Locale displayLocale) {
    LocaleDto.Builder builder = LocaleDto.newBuilder().setName(locale.toString());

    if(displayLocale != null) {
      builder.setDisplay(locale.getDisplayName(displayLocale));
    }

    return builder.build();
  }
}
