/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.authz.presenter;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclAddCallback;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclDeleteCallback;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclGetCallback;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.SubjectAcls;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;

public class DefaultAclCallback implements AclGetCallback, AclDeleteCallback, AclAddCallback {

  private EventBus eventBus;

  public DefaultAclCallback(EventBus eventBus) {
    super();
    this.eventBus = eventBus;
  }

  @Override
  public void onGet(JsArray<SubjectAcls> resource) {
  }

  @Override
  public void onGetFailed(Response response) {
    eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "Failed getting permissions", null));
  }

  @Override
  public void onDelete(String subject) {
  }

  @Override
  public void onDeleteFailed(Response response, String subject) {
    eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "Failed deleting permissions of " + subject, null));
  }

  @Override
  public void onAdd(Acl resource) {
  }

  @Override
  public void onAddFailed(Response response, String subject, String resource, String perm) {
    eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "Failed adding permissions of " + subject, null));
  }

}