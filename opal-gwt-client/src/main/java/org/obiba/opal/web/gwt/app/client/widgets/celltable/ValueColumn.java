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
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

public class ValueColumn extends Column<ValueSetsDto.ValueSetDto, String> {

  private static Translations translations = GWT.create(Translations.class);

  private ValueSelectionHandler valueSelectionHandler = null;

  private int pos = 0;

  private VariableDto variable;

  /**
   * Value column, with only one value expected in the value set.
   * @param variable
   */
  public ValueColumn(VariableDto variable) {
    this(0, variable);
  }

  /**
   * Value column with expected position of the value in the value set.
   * @param pos
   * @param variable
   */
  public ValueColumn(int pos, VariableDto variable) {
    this(pos, variable, createCell(variable));
  }

  private static Cell<String> createCell(final VariableDto variable) {
    if(variable.getValueType().equalsIgnoreCase("binary")) {
      return new ClickableTextCell(new AbstractSafeHtmlRenderer<String>() {
        @Override
        public SafeHtml render(String object) {
          if(object == null || object.trim().isEmpty()) return new SafeHtmlBuilder().toSafeHtml();
          return new SafeHtmlBuilder().appendHtmlConstant("<a class=\"icon icon-down\">").appendEscaped(object).appendHtmlConstant("</a>").toSafeHtml();
        }
      });
    } else {
      return new TextCell();
    }
  }

  private ValueColumn(int pos, VariableDto variable, Cell<String> cell) {
    super(cell);

    this.pos = pos;
    this.variable = variable;

    if(variable.getValueType().equalsIgnoreCase("binary")) {
      setFieldUpdater(new FieldUpdater<ValueSetsDto.ValueSetDto, String>() {

        @Override
        public void update(int index, ValueSetDto valueSet, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onValueSelection(index, getPosition(), valueSet);
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
    if(valueSet.getValuesArray() == null || valueSet.getValuesArray().length() <= getPosition()) return "";
    ValueDto value = valueSet.getValuesArray().get(getPosition());
    if(variable.getIsRepeatable()) {
      return getValueSequence(value);
    } else {
      return getValue(value);
    }
  }

  /**
   * Get the position of the variable's value in the value set
   * @return
   */
  protected int getPosition() {
    return pos;
  }

  private String getValueSequence(ValueDto value) {
    JsArray<ValueDto> values = value.getValuesArray();
    if(values == null) return getValue(value);

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
      return translations.downloadLabel();
    } else {
      return value.getValue();
    }
  }

  public interface ValueSelectionHandler {

    void onValueSelection(int row, int column, ValueSetsDto.ValueSetDto valueSet);
  }

}