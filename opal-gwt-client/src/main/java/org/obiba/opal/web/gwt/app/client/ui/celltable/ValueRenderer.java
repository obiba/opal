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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.ValueRenderingHelper;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;

public enum ValueRenderer {

  DATE {
    @Override
    protected String transform(String valueStr) {
      if (valueStr == null) return null;
      if (valueStr.isEmpty()) return "";
      return valueStr.length() < 10 ? valueStr : valueStr.substring(0, 10);
    }
  },
  DATETIME {
    @Override
    protected String transform(String valueStr) {
      if (valueStr == null) return null;
      if (valueStr.isEmpty()) return "";
      return valueStr.length() < 19 ? valueStr : valueStr.substring(0, 19).replace('T', ' ');
    }
  },
  BINARY {
    @Override
    protected String getValue(ValueSetsDto.ValueDto value) {
      if (!value.hasLink() || value.getLink().isEmpty()) return "";
      String label = translations.downloadLabel();
      if (value.hasLength()) {
        label += " [" + ValueRenderingHelper.getSizeInBytes(value.getLength()) + "]";
      }

      return label;
    }

    @Override
    protected String getValue(String value) {
      if (value == null) return null;
      if (value.isEmpty()) return "";
      return translations.downloadLabel();
    }
  },
  INTEGER, DECIMAL, BOOLEAN, LOCALE, TEXT, POINT, LINESTRING, POLYGON;

  private static final Translations translations = GWT.create(Translations.class);

  private static final int MAX_VALUES_IN_SEQUENCE = 3;

  public String render(String value) {
    return getValue(value);
  }

  public String render(ValueSetsDto.ValueDto value) {
    return getValue(value);
  }

  public String render(ValueSetsDto.ValueDto value, boolean repeatable) {
    if (repeatable) {
      return getValueSequence(value);
    }
    return getValue(value);
  }

  protected String transform(String valueStr) {
    return valueStr == null ? null : valueStr;
  }

  protected String getValue(String value) {
    if (value == null) return null;
    if (value.isEmpty()) return "";
    return transform(value);
  }

  protected String getValue(ValueSetsDto.ValueDto value) {
    if (value == null) return null;
    return transform(value.hasValue() ? value.getValue() : null);
  }

  private String getValueSequence(ValueSetsDto.ValueDto value) {
    JsArray<ValueSetsDto.ValueDto> values = value.getValuesArray();
    if (values == null) return getValue(value);

    StringBuilder builder = new StringBuilder();
    int count = 0;
    for (ValueSetsDto.ValueDto val : JsArrays.toIterable(values)) {
      if (count > 0) {
        builder.append(", ");
      }
      if (count == MAX_VALUES_IN_SEQUENCE) {
        builder.append("...");
        break;
      }
      String valueStr = getValue(val);
      builder.append(valueStr == null ? "(null)" : valueStr);
      count++;
    }
    return builder.toString();
  }

}