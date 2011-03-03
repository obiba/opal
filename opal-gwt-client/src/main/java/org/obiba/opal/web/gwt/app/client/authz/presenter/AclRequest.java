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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.Acl;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;

/**
 *
 */
public class AclRequest {

  private List<AclResource> acls;

  private AclGetCallback aclGetCallback;

  private AclAddCallback aclAddCallback;

  private AclDeleteCallback aclDeleteCallback;

  private AclRequest(String resource, String perm) {
    getAcls().add(new AclResource(resource, perm));
  }

  public void addResource(String resource, String perm) {
    getAcls().add(new AclResource(resource, perm));
  }

  private List<AclResource> getAcls() {
    return acls != null ? acls : (acls = new ArrayList<AclRequest.AclResource>());
  }

  public void setAclGetCallback(AclGetCallback aclGetCallback) {
    this.aclGetCallback = aclGetCallback;
  }

  public void setAclDeleteCallback(AclDeleteCallback aclDeleteCallback) {
    this.aclDeleteCallback = aclDeleteCallback;
  }

  public void setAclAddCallback(AclAddCallback aclAddCallback) {
    this.aclAddCallback = aclAddCallback;
  }

  public void get() {
    AclResource acl = getAcls().get(0);
    ResourceRequestBuilderFactory.<JsArray<Acl>> newBuilder().forResource("/authz" + acl.resource).get().withCallback(new ResourceCallback<JsArray<Acl>>() {

      @Override
      public void onResource(Response response, JsArray<Acl> resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          aclGetCallback.onGet(JsArrays.toSafeArray(resource));
        } else {
          aclGetCallback.onGetFailed(response);
        }
      }
    }).send();
  }

  public void add(final String subject) {
    add(subject, 0);
  }

  private void add(final String subject, final int index) {
    final AclResource acl = getAcls().get(index);
    ResourceRequestBuilderFactory.<Acl> newBuilder().forResource("/authz" + acl.resource + "?subject=" + subject + "&perm=" + acl.perm).post()//
    .withCallback(new ResourceCallback<Acl>() {

      @Override
      public void onResource(Response response, Acl resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          if(index == getAcls().size() - 1) {
            aclAddCallback.onAdd(resource);
          } else {
            add(subject, index + 1);
          }
        } else {
          aclAddCallback.onAddFailed(response, subject, acl.resource, acl.perm);
        }
      }
    }).send();
  }

  public void delete(final String subject) {
    delete(subject, 0);
  }

  private void delete(final String subject, final int index) {
    AclResource acl = getAcls().get(index);
    ResourceRequestBuilderFactory.<Acl> newBuilder().forResource("/authz" + acl.resource + "?subject=" + subject).delete()//
    .withCallback(new ResourceCallback<Acl>() {

      @Override
      public void onResource(Response response, Acl resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          if(index == getAcls().size() - 1) {
            aclDeleteCallback.onDelete(subject);
          } else {
            delete(subject, index + 1);
          }
        } else {
          aclDeleteCallback.onDeleteFailed(response, subject);
        }
      }
    }).send();
  }

  public static Builder newBuilder(String resource, String perm) {
    return new Builder(resource, perm);
  }

  //
  // Inner classes
  //

  private class AclResource {
    String resource;

    String perm;

    public AclResource(String resource, String perm) {
      super();
      this.resource = resource;
      this.perm = perm;
    }
  }

  public static class Builder {
    private AclRequest request;

    Builder(String resource, String perm) {
      request = new AclRequest(resource, perm);
    }

    public Builder and(String resource, String perm) {
      request.addResource(resource, perm);
      return this;
    }

    public AclRequest build() {
      return request;
    }

  }

  //
  // Interfaces
  //

  public interface AclGetCallback {
    public void onGet(JsArray<Acl> resource);

    public void onGetFailed(Response response);
  }

  public interface AclDeleteCallback {
    public void onDelete(String subject);

    public void onDeleteFailed(Response response, String subject);
  }

  public interface AclAddCallback {
    public void onAdd(Acl resource);

    public void onAddFailed(Response response, String subject, String resource, String perm);
  }
}
