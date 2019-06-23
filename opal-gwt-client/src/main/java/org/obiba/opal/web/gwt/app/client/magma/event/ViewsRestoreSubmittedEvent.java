/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ViewsRestoreSubmittedEvent extends GwtEvent<ViewsRestoreSubmittedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onViewsRestoreSubmitted(ViewsRestoreSubmittedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public ViewsRestoreSubmittedEvent() { }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onViewsRestoreSubmitted(this);
  }


}
