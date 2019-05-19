/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.core.client.JsArray;

public class ListAttributeValueColumn extends AttributeColumn<JsArray<AttributeDto>> {

  public ListAttributeValueColumn(String attributeNamespace) {
    super(attributeNamespace, "*");
  }

  @Override
  protected JsArray<AttributeDto> getAttributes(JsArray<AttributeDto> object) {
    JsArray<AttributeDto> attrs = JsArrays.create();
    for(int i = 0; i < object.length(); i++) {
      attrs.push(object.get(i));
    }
    return attrs;
  }

  @Override
  protected String getAttributeName(JsArray<AttributeDto> object) {
    return object.get(0).getName();
  }

}