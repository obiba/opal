/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
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

public class TableIndexStatusRefreshEvent extends GwtEvent<TableIndexStatusRefreshEvent.Handler> {

  public interface Handler extends EventHandler {

    void onRefresh(TableIndexStatusRefreshEvent event);

  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public TableIndexStatusRefreshEvent() {
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRefresh(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
