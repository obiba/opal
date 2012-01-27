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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;

enum ValueRenderer {

  DATE {
    @Override
    protected String getValue(ValueDto value) {
      String valueStr = super.getValue(value);
      return valueStr.isEmpty() ? valueStr : valueStr.substring(0, 10);
    }
  },
  DATETIME {
    @Override
    protected String getValue(ValueDto value) {
      String valueStr = super.getValue(value);
      return valueStr.isEmpty() ? valueStr : valueStr.substring(0, 19).replace('T', ' ');
    }
  },
  BINARY {

    @Override
    protected String getValue(ValueDto value) {
      if(value.getLink().isEmpty()) return "";
      return translations.downloadLabel();
    }
  },
  INTEGER, DECIMAL, BOOLEAN, LOCALE, TEXT;

  private static Translations translations = GWT.create(Translations.class);

  private static final int MAX_VALUES_IN_SEQUENCE = 3;

  public String render(ValueDto value, boolean repeatable) {
    if(repeatable) {
      return getValueSequence(value);
    }
    return getValue(value);
  }

  protected String getValue(ValueDto value) {
    return value.getValue();
  }

  private String getValueSequence(ValueDto value) {
    JsArray<ValueDto> values = value.getValuesArray();
    if(values == null) return getValue(value);

    StringBuilder builder = new StringBuilder();
    int count = 0;
    for(ValueDto val : JsArrays.toIterable(values)) {
      if(count > 0) {
        builder.append(", ");
      }
      if(count == MAX_VALUES_IN_SEQUENCE) {
        builder.append("...");
        break;
      }
      builder.append(getValue(val));
      count++;
    }
    return builder.toString();
  }

}