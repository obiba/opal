/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.core.client.JsArray;

public class AttributeValueColumn extends AttributeColumn<AttributeDto> {

  public AttributeValueColumn() {
    super("");
  }

  @Override
  protected JsArray<AttributeDto> getAttributes(AttributeDto object) {
    JsArray<AttributeDto> attrs = JsArrays.create();
    attrs.push(object);
    return attrs;
  }

  @Override
  protected String getAttributeName(AttributeDto object) {
    return object.getName();
  }

}