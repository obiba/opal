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
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

public class ValueColumn extends Column<ValueSetsDto.ValueSetDto, String> {

  private ValueSelectionHandler valueSelectionHandler = null;

  private int pos = 0;

  public ValueColumn() {
    this(0);
  }

  public ValueColumn(int pos) {
    this(0, "");
  }

  public ValueColumn(String type) {
    this(0, type);
  }

  public ValueColumn(int pos, String type) {
    super(type.equalsIgnoreCase("binary") ? new ClickableTextCell(new AbstractSafeHtmlRenderer<String>() {
      @Override
      public SafeHtml render(String object) {
        return new SafeHtmlBuilder().appendHtmlConstant("<a>").appendEscaped(object).appendHtmlConstant("</a>").toSafeHtml();
      }
    }) : new TextCell());

    this.pos = pos;

    if(type.equalsIgnoreCase("binary")) {
      setFieldUpdater(new FieldUpdater<ValueSetsDto.ValueSetDto, String>() {

        @Override
        public void update(int index, ValueSetDto valueSet, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onValueSelection(index, ValueColumn.this.pos, valueSet);
          }
        }
      });
    }
  }

  public void setValueSelectionHandler(ValueSelectionHandler valueSelectionHandler) {
    this.valueSelectionHandler = valueSelectionHandler;
  }

  @Override
  public String getValue(ValueSetDto valueSet) {
    if(valueSet.getValuesArray() == null || valueSet.getValuesArray().length() <= pos) return "";
    ValueDto value = valueSet.getValuesArray().get(pos);
    if(value.getValuesArray() != null) {
      return getValueSequence(value);
    } else {
      return getValue(value);
    }
  }

  private String getValueSequence(ValueDto value) {
    JsArray<ValueDto> values = value.getValuesArray();
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for(ValueDto val : JsArrays.toIterable(values)) {
      if(!first) {
        builder.append(", ");
      } else {
        first = false;
      }
      builder.append(getValue(val));
    }
    return builder.toString();
  }

  private String getValue(ValueDto value) {
    if(value.hasLink()) {
      return "download";
    } else {
      return value.getValue();
    }
  }

  public interface ValueSelectionHandler {

    void onValueSelection(int row, int column, ValueSetsDto.ValueSetDto valueSet);
  }

}