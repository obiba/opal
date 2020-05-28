/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;

import com.google.gwt.core.client.JsArray;

public class CategoryAttributeColumn extends AttributeColumn<CategoryDto> {

  public CategoryAttributeColumn(String attributeName) {
    super(attributeName);
  }

  @Override
  protected JsArray<AttributeDto> getAttributes(CategoryDto object) {
    List<AttributeDto> attributeDtos = JsArrays.toList(object.getAttributesArray());
    Collections.sort(attributeDtos, new Comparator<AttributeDto>() {
      @Override
      public int compare(AttributeDto attributeDto, AttributeDto attributeDto2) {
        return attributeDto.getLocale().compareTo(attributeDto2.getLocale());
      }
    });

    JsArray<AttributeDto> sorted = JsArrays.create().cast();

    for(AttributeDto attributeDto : attributeDtos) {
      sorted.push(attributeDto);
    }
    return sorted;
  }
}