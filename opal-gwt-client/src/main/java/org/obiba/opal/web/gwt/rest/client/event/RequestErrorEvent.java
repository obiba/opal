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

/**
 * An event fired when an exception is raised during the processing of a request built by the {@code
 * ResourceRequestBuilder}
 */
public class RequestErrorEvent extends GwtEvent<RequestErrorEvent.Handler> {

  private static Type<RequestErrorEvent.Handler> TYPE;

  public static interface Handler extends EventHandler {
    public void onRequestError(RequestErrorEvent e);
  }

  private final Throwable exception;

  /**
   * @param selectedItem
   */
  public RequestErrorEvent(Throwable t) {
    this.exception = t;
  }

  public Throwable getSelection() {
    return exception;
  }

  public static Type<RequestErrorEvent.Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<RequestErrorEvent.Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRequestError(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

}
