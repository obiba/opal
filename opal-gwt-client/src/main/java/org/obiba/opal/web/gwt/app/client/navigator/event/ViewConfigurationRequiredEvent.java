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
 * An event signalling that an Opal view needs to be configured.
 */
public class ViewConfigurationRequiredEvent extends GwtEvent<ViewConfigurationRequiredEvent.Handler> {
  //
  // Static Variables
  //

  private static Type<Handler> TYPE;

  //
  // Instance Variables
  //

  private String datasourceName;

  private String viewName;

  //
  // Constructors
  //

  public ViewConfigurationRequiredEvent(String datasourceName, String viewName) {
    this.datasourceName = datasourceName;
    this.viewName = viewName;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onViewConfigurationRequired(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public String getViewName() {
    return viewName;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event);
  }
}
