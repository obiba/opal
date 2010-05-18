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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 * An event fired when a request was sent and its response received, but nothing was configured to handle the response.
 */
public class UnhandledResponseEvent extends GwtEvent<UnhandledResponseEvent.Handler> {

  private static Type<UnhandledResponseEvent.Handler> TYPE;

  public static interface Handler extends EventHandler {
    public void onUnhandledResponse(UnhandledResponseEvent e);
  }

  private final Request request;

  private final Response response;

  /**
   * @param selectedItem
   */
  public UnhandledResponseEvent(Request request, Response response) {
    this.request = request;
    this.response = response;
  }

  public Request getRequest() {
    return request;
  }

  public Response getResponse() {
    return response;
  }

  public static Type<UnhandledResponseEvent.Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<UnhandledResponseEvent.Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onUnhandledResponse(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

}
