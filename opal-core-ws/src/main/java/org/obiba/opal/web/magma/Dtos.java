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

import java.util.Locale;

import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.opal.web.model.Magma.AttributeDto;
import org.obiba.opal.web.model.Magma.CategoryDto;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.VariableDto;

import com.google.common.base.Function;

/**
 * Utilities for manipulating Magma Dto instances
 */
final class Dtos {

  public static Function<Variable, VariableDto> asDtoFunc(final LinkDto tableLink, final UriBuilder uriBuilder) {
    return new Function<Variable, VariableDto>() {
      @Override
      public VariableDto apply(Variable from) {
        return asDto(tableLink, uriBuilder, from).build();
      }
    };
  }

  public static VariableDto.Builder asDto(final LinkDto tableLink, UriBuilder uriBuilder, Variable from) {

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

    return var;

  }

  public static VariableDto.Builder asDto(Variable from) {
    return asDto(null, null, from);
  }

  public static Variable fromDto(VariableDto dto) {
    Variable.Builder builder = Variable.Builder.newVariable(dto.getName(), ValueType.Factory.forName(dto.getValueType()), dto.getEntityType());
    if(dto.getIsRepeatable()) {
      builder.repeatable();
      builder.occurrenceGroup(dto.getOccurrenceGroup());
    }
    if(dto.hasMimeType()) {
      builder.mimeType(dto.getMimeType());
    }
    if(dto.hasUnit()) {
      builder.unit(dto.getUnit());
    }
    for(AttributeDto attr : dto.getAttributesList()) {
      builder.addAttribute(fromDto(attr));
    }
    for(CategoryDto cat : dto.getCategoriesList()) {
      builder.addCategory(fromDto(cat));
    }

    return builder.build();
  }

  public static CategoryDto.Builder asDto(Category from) {
    CategoryDto.Builder c = CategoryDto.newBuilder().setName(from.getName()).setIsMissing(from.isMissing());
    for(Attribute attribute : from.getAttributes()) {
      c.addAttributes(asDto(attribute));
    }
    return c;
  }

  private static Category fromDto(CategoryDto dto) {
    Category.Builder builder = Category.Builder.newCategory(dto.getName()).missing(dto.getIsMissing());
    for(AttributeDto attr : dto.getAttributesList()) {
      builder.addAttribute(fromDto(attr));
    }
    return builder.build();
  }

  public static AttributeDto.Builder asDto(Attribute from) {
    AttributeDto.Builder a = AttributeDto.newBuilder().setName(from.getName()).setValue(from.getValue().toString());
    if(from.isLocalised()) {
      a.setLocale(from.getLocale().toString());
    }
    return a;
  }

  public static Attribute fromDto(AttributeDto dto) {
    Attribute.Builder builder = Attribute.Builder.newAttribute(dto.getName());
    if(dto.hasLocale()) {
      builder.withValue(new Locale(dto.getLocale()), dto.getValue());
    } else {
      builder.withValue(dto.getValue());
    }
    return builder.build();
  }

}
