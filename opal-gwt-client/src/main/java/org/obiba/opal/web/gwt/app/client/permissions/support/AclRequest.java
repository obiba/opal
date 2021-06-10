/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.permissions.support;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.Acls;
import org.obiba.opal.web.model.client.opal.Subject;

/**
 *
 */
public class AclRequest {

  private final AclAction action;

  private final String resource;

  private AclAddCallback aclAddCallback;

  private AclDeleteCallback aclDeleteCallback;

  public AclRequest(AclAction action, String resource) {
    this.action = action;
    this.resource = resource;
  }

  public AclRequest copy() {
    return new AclRequest(action, resource);
  }

  public String getHeader() {
    return action.getName();
  }

  public String getResource() {
    return resource;
  }

  public void setAclDeleteCallback(AclDeleteCallback aclDeleteCallback) {
    this.aclDeleteCallback = aclDeleteCallback;
  }

  public void setAclAddCallback(AclAddCallback aclAddCallback) {
    this.aclAddCallback = aclAddCallback;
  }

  public void add(final Subject subject) {
    ResourceRequestBuilderFactory.<Acl>newBuilder().forResource(
        "/authz" + resource + "?subject=" + subject.getPrincipal() + "&type=" + subject.getType() + "&perm=" +
            action.getName()).post()//
        .withCallback(new ResourceCallback<Acl>() {

          @Override
          public void onResource(Response response, Acl acl) {
            if (response.getStatusCode() == Response.SC_OK) {
              aclAddCallback.onAdd(acl);
            } else {
              aclAddCallback.onAddFailed(response, subject, resource, action);
            }
          }
        }).send();
  }

  public void delete(final Subject subject) {
    ResourceRequestBuilderFactory.<Acl>newBuilder().forResource(
        "/authz" + resource + "?subject=" + subject.getPrincipal() + "&type=" + subject.getType() + "&perm=" +
            action.getName()).delete()//
        .withCallback(new ResourceCallback<Acl>() {

          @Override
          public void onResource(Response response, Acl resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              aclDeleteCallback.onDelete(subject);
            } else {
              aclDeleteCallback.onDeleteFailed(response, subject);
            }
          }
        }).send();
  }

  public boolean hasPermission(Acls acls) {
    String decodedResource = URL.decodePathSegment(resource);
    for (Acl acl : JsArrays.toIterable(acls.getAclsArray())) {
      if (acl.getResource().equals(decodedResource) && hasAction(acl)) return true;
    }
    return false;
  }

  private boolean hasAction(Acl acl) {
    for (int i = 0; i < acl.getActionsArray().length(); i++) {
      if (acl.getActionsArray().get(i).equals(action.getName())) {
        return true;
      }
    }
    return false;
  }

  //
  // Interfaces
  //

  public interface AclGetCallback {
    void onGet(JsArray<Acls> resource);

    void onGetFailed(Response response);
  }

  public interface AclDeleteCallback {
    void onDelete(Subject subject);

    void onDeleteFailed(Response response, Subject subject);
  }

  public interface AclAddCallback {
    void onAdd(Acl resource);

    void onAddFailed(Response response, Subject subject, String resource, AclAction action);
  }
}
