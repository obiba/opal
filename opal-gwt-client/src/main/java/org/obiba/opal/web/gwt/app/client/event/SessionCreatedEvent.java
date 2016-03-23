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

public class SessionCreatedEvent extends GwtEvent<SessionCreatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onSessionCreated(SessionCreatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>();

  private final String uri;

  public SessionCreatedEvent(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSessionCreated(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
