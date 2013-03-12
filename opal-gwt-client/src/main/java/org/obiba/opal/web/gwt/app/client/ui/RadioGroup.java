/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.ui;

import java.util.ArrayList;
import java.util.Comparator;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Combines several {@code RadioButton} into a group for exposing a specific value per choice. For example, this allows
 * working with a group of radio buttons in terms of an {@code Enum} value instead of several boolean values.
 * <p/>
 * By default, this implementation uses {@code equals} to compare values. A {@code Comparator} instance can be provided
 * to override this behaviour.
 *
 * @param <T>
 */
public class RadioGroup<T> implements HasValue<T> {

  private final SimpleEventBus bus = new SimpleEventBus();

  private final ArrayList<HasValue<Boolean>> buttons = new ArrayList<HasValue<Boolean>>();

  private final ArrayList<T> values = new ArrayList<T>();

  private final Comparator<T> comparator;

  private Integer currentSelection = null;

  public RadioGroup() {
    // uses equals()
    this(null);
  }

  public RadioGroup(Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public HandlerRegistration addButton(HasValue<Boolean> button, final T value) {
    if(this.buttons.size() != this.values.size()) throw new IllegalStateException("buttons and values are out of sync");

    this.buttons.add(button);
    this.values.add(value);

    final int index = this.buttons.size() - 1;

    return button.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if(currentSelection == null || currentSelection != index) {
          currentSelection = index;
          ValueChangeEvent.fire(RadioGroup.this, getValue());
        }
      }
    });
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return bus.addHandler(ValueChangeEvent.getType(), handler);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    bus.fireEvent(event);
  }

  @Override
  public T getValue() {
    for(int i = 0; i < buttons.size(); i++) {
      Boolean value = buttons.get(i).getValue();
      if(value != null && value == true) {
        return values.get(i);
      }
    }
    return null;
  }

  @Override
  public void setValue(T value) {
    setValue(value, false);
  }

  @Override
  public void setValue(T value, boolean fireEvents) {
    for(int i = 0; i < values.size(); i++) {
      T buttonValue = values.get(i);
      if(equals(value, buttonValue)) {
        selectButton(i, fireEvents);
        return;
      }
    }
  }

  private void selectButton(int index, boolean fireEvents) {
    if(currentSelection != null && currentSelection == index) return;
    for(int i = 0; i < buttons.size(); i++) {
      boolean selected = i == index;
      buttons.get(i).setValue(selected, fireEvents);
    }
    currentSelection = index;
  }

  private boolean equals(T v1, T v2) {
    if(v1 == null) return v2 == null;
    if(v2 == null) return false;

    if(comparator != null) return comparator.compare(v1, v2) == 0;

    return v1.equals(v2);
  }
}
