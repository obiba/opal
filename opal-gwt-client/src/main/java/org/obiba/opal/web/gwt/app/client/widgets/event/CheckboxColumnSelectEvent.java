/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.widgets.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that will be fired by {@link ChecboxColumn}.
 */
public class CheckboxColumnSelectEvent extends GwtEvent<CheckboxColumnSelectEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<Handler>();

  //
  // Instance Variables
  //

  private Object source;

  private Boolean value;

  //
  // Constructors
  //
  public CheckboxColumnSelectEvent(Object source, Boolean value) {
    this.source = source;
    this.value = value;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onCheckboxColumnSelection(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  public Object getSource() {
    return source;
  }

  public Boolean getValue() {
    return value;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    public void onCheckboxColumnSelection(CheckboxColumnSelectEvent event);
  }
}
