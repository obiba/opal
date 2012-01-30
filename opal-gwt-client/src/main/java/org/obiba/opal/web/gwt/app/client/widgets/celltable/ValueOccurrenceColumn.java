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
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.ValueSetDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.TextColumn;

public class ValueOccurrenceColumn extends TextColumn<ValueOccurrence> {

  private final int pos;

  private ValueRenderer renderer;

  public ValueOccurrenceColumn(int pos) {
    super();
    this.pos = pos;
  }

  @Override
  public String getValue(ValueOccurrence value) {
    if(renderer == null) {
      try {
        renderer = ValueRenderer.valueOf(value.getValueType(pos).toUpperCase());
      } catch(Exception e) {
        return ValueRenderer.TEXT.render(value.getValue(pos));
      }
    }
    return renderer.render(value.getValue(pos));
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

    private JsArray<ValueDto> getValueSequence(int pos) {
      return JsArrays.toSafeArray(valueSet.getValuesArray().get(pos).getValuesArray());
    }

    public String getValueType(int pos) {
      return valueSet.getValuesArray().get(pos).getValueType();
    }

    public ValueDto getValue(int pos) {
      JsArray<ValueDto> valueSequence = getValueSequence(pos);
      if(index >= valueSequence.length()) return null;
      return valueSequence.get(index);
    }
  }
}