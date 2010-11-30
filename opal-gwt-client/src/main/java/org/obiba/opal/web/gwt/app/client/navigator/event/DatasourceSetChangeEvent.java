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
 * Signals that the set of datasources has changed (i.e., one or more datasources have been added or removed).
 */
public class DatasourceSetChangeEvent extends GwtEvent<DatasourceSetChangeEvent.Handler> {
  //
  // Instance Variables
  //

  private static Type<Handler> TYPE;

  //
  // Constructors
  //

  public DatasourceSetChangeEvent() {
    super();
  }

  //
  // GwtEvent Methods
  //

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDatasourceSetChanged(this);
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onDatasourceSetChanged(DatasourceSetChangeEvent event);
  }
}
