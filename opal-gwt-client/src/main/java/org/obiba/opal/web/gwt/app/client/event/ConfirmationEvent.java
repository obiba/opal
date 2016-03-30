/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class ConfirmationEvent extends GwtEvent<ConfirmationEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<>();

  //
  // Instance Variables
  //

  private final Object source;

  private final boolean confirmed;

  //
  // Constructors
  //

  public ConfirmationEvent(Object source, boolean confirmed) {
    this.source = source;
    this.confirmed = confirmed;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onConfirmation(this);
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

  public boolean isConfirmed() {
    return confirmed;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onConfirmation(ConfirmationEvent event);
  }
}
