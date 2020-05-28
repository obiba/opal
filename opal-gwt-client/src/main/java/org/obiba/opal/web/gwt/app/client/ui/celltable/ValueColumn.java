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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
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
import com.google.gwt.user.cellview.client.Column;

public class ValueColumn extends Column<ValueSetsDto.ValueSetDto, String> {

  private ValueSelectionHandler valueSelectionHandler = null;

  private int pos = 0;

  private final VariableDto variable;

  private final ValueRenderer valueRenderer;

  private final Cell<String> cell;

  /**
   * Value column, with only one value expected in the value set.
   *
   * @param variable
   */
  public ValueColumn(VariableDto variable) {
    this(0, variable);
  }

  /**
   * Value column with expected position of the value in the value set.
   *
   * @param pos
   * @param variable
   */
  public ValueColumn(int pos, VariableDto variable) {
    this(pos, variable, createCell(variable));
  }

  private static Cell<String> createCell(VariableDto variable) {
    if(variable.getIsRepeatable()) {
      return new ClickableTextCell(new ClickableIconRenderer(IconType.LIST));
    }
    if("binary".equalsIgnoreCase(variable.getValueType())) {
      return new ClickableTextCell(new ClickableIconRenderer(IconType.DOWNLOAD));
    }
    if(variable.getValueType().matches("point|linestring|polygon")) {
      return new ClickableTextCell(new ClickableIconRenderer(IconType.MAP_MARKER));
    }
    if(VariableDtos.ValueType.TEXT.is(variable.getValueType())) {
      if(Strings.isNullOrEmpty(variable.getReferencedEntityType())) return new TextCell(new MultilineTextRenderer());
      else return new ClickableTextCell(new ClickableIconRenderer(IconType.ELLIPSIS_VERTICAL));
    }
    if((VariableDtos.ValueType.INTEGER.is(variable.getValueType()) || VariableDtos.ValueType.DECIMAL.is(variable.getValueType()))
        && !Strings.isNullOrEmpty(variable.getReferencedEntityType())) {
      return new ClickableTextCell(new ClickableIconRenderer(IconType.ELLIPSIS_VERTICAL));
    }
    return new TextCell();
  }

  private ValueColumn(int pos, VariableDto variable, Cell<String> cell) {
    super(cell);
    this.cell = cell;
    this.pos = pos;
    this.variable = variable;
    valueRenderer = ValueRenderer.valueOf(variable.getValueType().toUpperCase());

    if(variable.getIsRepeatable()) {
      setFieldUpdater(new FieldUpdater<ValueSetsDto.ValueSetDto, String>() {

        @Override
        public void update(int index, ValueSetDto valueSet, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onValueSequenceSelection(ValueColumn.this.variable, index, getPosition(), valueSet);
          }
        }
      });
    } else if("binary".equalsIgnoreCase(variable.getValueType())) {
      setFieldUpdater(new FieldUpdater<ValueSetsDto.ValueSetDto, String>() {

        @Override
        public void update(int index, ValueSetDto valueSet, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onBinaryValueSelection(ValueColumn.this.variable, index, getPosition(), valueSet);
          }
        }
      });
    } else if(variable.getValueType().matches("point|linestring|polygon")) {
      setFieldUpdater(new FieldUpdater<ValueSetsDto.ValueSetDto, String>() {

        @Override
        public void update(int index, ValueSetDto valueSet, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onGeoValueSelection(ValueColumn.this.variable, index, getPosition(), valueSet,
                valueSet.getValuesArray().get(getPosition()));
          }
        }
      });
    } else if((VariableDtos.ValueType.TEXT.is(variable.getValueType()) || VariableDtos.ValueType.INTEGER.is(variable.getValueType()) || VariableDtos.ValueType.DECIMAL.is(variable.getValueType())) &&
        !Strings.isNullOrEmpty(variable.getReferencedEntityType())) {
      setFieldUpdater(new FieldUpdater<ValueSetsDto.ValueSetDto, String>() {

        @Override
        public void update(int index, ValueSetDto valueSet, String value) {
          if(valueSelectionHandler != null) {
            valueSelectionHandler.onEntityIDSelection(ValueColumn.this.variable, index, getPosition(), valueSet,
                valueSet.getValuesArray().get(getPosition()));
          }
        }
      });
    }
  }

  public void setValueSelectionHandler(ValueSelectionHandler valueSelectionHandler) {
    this.valueSelectionHandler = valueSelectionHandler;
  }

  @Override
  public void render(Cell.Context context, ValueSetDto valueSet, SafeHtmlBuilder sb) {
    String valueStr = getValue(valueSet);
    if (valueStr == null)
      sb.appendHtmlConstant("<span class='help-block no-bottom-margin' style='font-size: smaller'>(null)</span>");
    else
      cell.render(context, valueStr, sb);
  }

  @Override
  public String getValue(ValueSetDto valueSet) {
    if(valueSet.getValuesArray() == null || valueSet.getValuesArray().length() <= getPosition()) return null;
    ValueDto value = valueSet.getValuesArray().get(getPosition());
    return valueRenderer.render(value, variable.getIsRepeatable());
  }

  /**
   * Get the position of the variable's value in the value set
   *
   * @return
   */
  protected int getPosition() {
    return pos;
  }

  public interface ValueSelectionHandler {

    void onBinaryValueSelection(VariableDto variableDto, int row, int column, ValueSetsDto.ValueSetDto valueSet);

    void onGeoValueSelection(VariableDto variableDto, int row, int column, ValueSetsDto.ValueSetDto valueSet,
        ValueSetsDto.ValueDto value);

    void onValueSequenceSelection(VariableDto variableDto, int row, int column, ValueSetsDto.ValueSetDto valueSet);

    void onEntityIDSelection(VariableDto variableDto, int row, int column, ValueSetsDto.ValueSetDto valueSet,
        ValueSetsDto.ValueDto value);
  }

}