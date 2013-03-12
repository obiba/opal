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

import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;

public class VariableAttributeColumn extends AttributeColumn<VariableDto> {

  public VariableAttributeColumn(String attributeName) {
    super(attributeName);
  }

  @Override
  protected JsArray<AttributeDto> getAttributes(VariableDto object) {
    return object.getAttributesArray();
  }
}