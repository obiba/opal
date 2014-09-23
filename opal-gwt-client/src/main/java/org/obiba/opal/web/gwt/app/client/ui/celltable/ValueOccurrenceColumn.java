/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueOccurrenceColumn.ValueOccurrence;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.Column;

public class ValueOccurrenceColumn extends Column<ValueOccurrence, String> {

  private ValueSelectionHandler valueSelectionHandler = null;

  private final int pos;

  private final VariableDto variable;

  private final ValueRenderer valueRenderer;

  public ValueOccurrenceColumn(VariableDto variable, final int pos) {
    super(createCell(variable));
    this.pos = pos;
    this.variable = variable;
    valueRenderer = ValueRenderer.valueOf(variable.getValueType().toUpperCase());

    if("binary".equalsIgnoreCase(variable.getValueType())) {
      setFieldUpdater(new FieldUpdater<ValueOccurrence, String>() {

        @Override
        public void update(int index, ValueOccurrence object, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler
                .onBinaryValueSelection(ValueOccurrenceColumn.this.variable, index, object.getValueSet());
          }
        }
      });
    } else if(variable.getValueType().matches("point|linestring|polygon")) {
      setFieldUpdater(new FieldUpdater<ValueOccurrence, String>() {

        @Override
        public void update(int index, ValueOccurrence object, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onGeoValueSelection(ValueOccurrenceColumn.this.variable, index, object.getValueSet(),
                object.getValueSet().getValuesArray().get(pos));
          }
        }
      });
    } else if("text".equalsIgnoreCase(variable.getValueType()) && !Strings.isNullOrEmpty(variable.getReferencedEntityType())) {
      setFieldUpdater(new FieldUpdater<ValueOccurrence, String>() {

        @Override
        public void update(int index, ValueOccurrence object, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onEntityIDSelection(ValueOccurrenceColumn.this.variable, index, object.getValueSet(),
                object.getValueSet().getValuesArray().get(pos));
          }
        }
      });
    }
  }

  private static Cell<String> createCell(VariableDto variable) {
    if("binary".equalsIgnoreCase(variable.getValueType())) {
      return new ClickableTextCell(new ClickableIconRenderer(IconType.DOWNLOAD));
    }
    if("text".equalsIgnoreCase(variable.getValueType()) && !Strings.isNullOrEmpty(variable.getReferencedEntityType())) {
      return new ClickableTextCell(new ClickableIconRenderer(IconType.ELLIPSIS_VERTICAL));
    }
    if(variable.getValueType().matches("point|linestring|polygon")) {
      return new ClickableTextCell(new ClickableIconRenderer(IconType.MAP_MARKER));
    }
    return new TextCell();
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

    private JsArray<ValueDto> getValueSequence(int pos) {
      return JsArrays.toSafeArray(valueSet.getValuesArray().get(pos).getValuesArray());
    }

    @Nullable
    public ValueDto getValue(int pos) {
      JsArray<ValueDto> valueSequence = getValueSequence(pos);
      if(index >= valueSequence.length()) return null;
      return valueSequence.get(index);
    }
  }

  public interface ValueSelectionHandler {

    void onBinaryValueSelection(VariableDto variable, int index, ValueSetDto valueSet);

    void onGeoValueSelection(VariableDto variable, int index, ValueSetDto valueSet, ValueSetsDto.ValueDto value);

    void onEntityIDSelection(VariableDto variable, int index, ValueSetDto valueSet, ValueSetsDto.ValueDto value);

  }
}