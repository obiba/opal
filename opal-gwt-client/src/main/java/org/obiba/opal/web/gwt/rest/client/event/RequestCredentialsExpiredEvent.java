/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client.event;

import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * An event fired when {@code RequestCredentials} are absent after receiving a {@code Response}.
 * @see RequestCredentials#hasExpired(com.google.gwt.http.client.RequestBuilder)
 */
public class RequestCredentialsExpiredEvent extends GwtEvent<RequestCredentialsExpiredEvent.Handler> {

  private static final Type<Handler> TYPE = new Type<Handler>();

  public interface Handler extends EventHandler {
    void onCredentialsExpired(RequestCredentialsExpiredEvent e);
  }

  public RequestCredentialsExpiredEvent() {
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCredentialsExpired(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

}
