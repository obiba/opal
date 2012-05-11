/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fire this event to have all administration presenters authorize themselves and callback on this
 * {@code HasAuthorization}.
 * <p/>
 * This allows decoupling a link to the administration section from the section's content.
 */
public class RequestAdministrationPermissionEvent extends GwtEvent<RequestAdministrationPermissionEvent.Handler> {

  public interface Handler extends EventHandler {
    void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final HasAuthorization authorization;

  public RequestAdministrationPermissionEvent(HasAuthorization authorization) {
    this.authorization = authorization;
  }

  public HasAuthorization getHasAuthorization() {
    return authorization;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onAdministrationPermissionRequest(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }

}
