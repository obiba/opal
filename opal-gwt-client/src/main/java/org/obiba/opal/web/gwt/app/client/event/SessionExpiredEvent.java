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

import org.obiba.opal.web.gwt.rest.client.DefaultResourceRequestBuilder;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class SessionExpiredEvent extends GwtEvent<SessionExpiredEvent.Handler> {
  public interface Handler extends EventHandler {
    void onSessionExpired(SessionExpiredEvent event);
  }

  private static Type<Handler> TYPE;

  private final DefaultResourceRequestBuilder<? extends JavaScriptObject> failedRequest;

  public SessionExpiredEvent(DefaultResourceRequestBuilder<? extends JavaScriptObject> resourceRequestBuilder) {
    this.failedRequest = resourceRequestBuilder;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSessionExpired(this);
  }

  @Override
  public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public DefaultResourceRequestBuilder<? extends JavaScriptObject> getFailedRequest() {
    return failedRequest;
  }
}
