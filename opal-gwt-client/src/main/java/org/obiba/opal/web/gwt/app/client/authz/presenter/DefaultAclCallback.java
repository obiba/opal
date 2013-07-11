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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.Acls;
import org.obiba.opal.web.model.client.opal.Subject;

import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Response;

public class DefaultAclCallback implements AclCallback {

  private final EventBus eventBus;

  public DefaultAclCallback(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void onGet(JsArray<Acls> resource) {
  }

  @Override
  public void onGetFailed(Response response) {
    eventBus.fireEvent(NotificationEvent.newBuilder().error("Failed getting permissions").build());
  }

  @Override
  public void onDelete(Subject subject) {
  }

  @Override
  public void onDeleteFailed(Response response, Subject subject) {
    eventBus.fireEvent(NotificationEvent.newBuilder().error("Failed deleting permissions of " + subject).build());
  }

  @Override
  public void onAdd(Acl resource) {
  }

  @Override
  public void onAddFailed(Response response, Subject subject, String resource, AclAction action) {
    eventBus.fireEvent(NotificationEvent.newBuilder().error("Failed adding permissions of " + subject).build());
  }

}