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
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueOccurrenceColumn.ValueOccurrence;
import org.obiba.opal.web.model.client.magma.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.cellview.client.Column;

public class ValueOccurrenceColumn extends Column<ValueOccurrence, String> {

  private ValueSelectionHandler valueSelectionHandler = null;

  private final int pos;

  private VariableDto variable;

  private ValueRenderer valueRenderer;

  public ValueOccurrenceColumn(VariableDto variable, int pos) {
    super(createCell(variable));
    this.pos = pos;
    this.variable = variable;
    this.valueRenderer = ValueRenderer.valueOf(variable.getValueType().toUpperCase());

    if(variable.getValueType().equalsIgnoreCase("binary")) {
      setFieldUpdater(new FieldUpdater<ValueOccurrenceColumn.ValueOccurrence, String>() {

        @Override
        public void update(int index, ValueOccurrence object, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onBinaryValueSelection(ValueOccurrenceColumn.this.variable, index, object.getValueSet());
          }
        }
      });
    }
  }

  private static Cell<String> createCell(final VariableDto variable) {
    if(variable.getValueType().equalsIgnoreCase("binary")) {
      return new ClickableTextCell(new ClickableIconRenderer("icon-down"));
    } else {
      return new TextCell();
    }
  }

  public void setValueSelectionHandler(ValueSelectionHandler valueSelectionHandler) {
    this.valueSelectionHandler = valueSelectionHandler;
  }

  @Override
  public String getValue(ValueOccurrence value) {
    return valueRenderer.render(value.getValue(pos));
  }

  public static final class ValueOccurrence {

    private final ValueSetDto valueSet;

    private final int index;

    public ValueOccurrence(ValueSetDto valueSet, int index) {
      this.valueSet = valueSet;
      this.index = index;
    }

    public int getIndex() {
      return index;
    }

    public ValueSetDto getValueSet() {
      return valueSet;
    }

    private JsArrayString getValueSequence(int pos) {
      return JsArrays.toSafeArray(valueSet.getValuesArray().get(pos).getSequenceArray());
    }

    public String getValueType(int pos) {
      return valueSet.getValuesArray().get(pos).getValueType();
    }

    public String getValue(int pos) {
      JsArrayString valueSequence = getValueSequence(pos);
      if(index >= valueSequence.length()) return null;
      return valueSequence.get(index);
    }
  }

  public interface ValueSelectionHandler {

    public void onBinaryValueSelection(VariableDto variable, int index, ValueSetDto valueSet);

  }
}