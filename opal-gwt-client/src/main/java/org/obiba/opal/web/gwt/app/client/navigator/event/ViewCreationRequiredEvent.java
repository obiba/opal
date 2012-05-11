/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * An event signalling that an Opal view needs to be created.
 */
public class ViewCreationRequiredEvent extends GwtEvent<ViewCreationRequiredEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<Handler>();

  //
  // Instance Variables
  //

  private String datasourceName;

  //
  // Constructors
  //

  public ViewCreationRequiredEvent(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public ViewCreationRequiredEvent() {
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onViewCreationRequired(this);
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

  public String getDatasourceName() {
    return datasourceName;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    public void onViewCreationRequired(ViewCreationRequiredEvent event);
  }
}
