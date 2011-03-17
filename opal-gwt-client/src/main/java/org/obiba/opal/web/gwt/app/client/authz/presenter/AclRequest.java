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

import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Acls;
import org.obiba.opal.web.model.client.opal.Subject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;

/**
 *
 */
public class AclRequest {

  private String header;

  private List<AclResource> aclResources;

  private AclAddCallback aclAddCallback;

  private AclDeleteCallback aclDeleteCallback;

  private AclRequest(String header, String resource, String action) {
    this.header = header;
    getAclResources().add(new AclResource(resource, action));
  }

  private AclRequest(AclRequest copy) {
    this.header = copy.header;
    for(AclResource res : copy.getAclResources()) {
      getAclResources().add(new AclResource(res.getResource(), res.getAction()));
    }
  }

  public String getHeader() {
    return header;
  }

  public void addAclResource(String resource, String perm) {
    getAclResources().add(new AclResource(resource, perm));
  }

  public List<AclResource> getAclResources() {
    return aclResources != null ? aclResources : (aclResources = new ArrayList<AclResource>());
  }

  public void setAclDeleteCallback(AclDeleteCallback aclDeleteCallback) {
    this.aclDeleteCallback = aclDeleteCallback;
  }

  public void setAclAddCallback(AclAddCallback aclAddCallback) {
    this.aclAddCallback = aclAddCallback;
  }

  public void add(final Subject subject) {
    add(subject, 0);
  }

  private void add(final Subject subject, final int index) {
    final AclResource acl = getAclResources().get(index);
    ResourceRequestBuilderFactory.<Acl> newBuilder().forResource("/authz" + acl.getResource() + "?subject=" + subject.getPrincipal() + "&type=" + subject.getType() + "&perm=" + acl.getAction()).post()//
    .withCallback(new ResourceCallback<Acl>() {

      @Override
      public void onResource(Response response, Acl resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          if(index == getAclResources().size() - 1) {
            aclAddCallback.onAdd(resource);
          } else {
            add(subject, index + 1);
          }
        } else {
          aclAddCallback.onAddFailed(response, subject, acl.getResource(), acl.getAction());
        }
      }
    }).send();
  }

  public void delete(final Subject subject) {
    delete(subject, 0);
  }

  private void delete(final Subject subject, final int index) {
    AclResource acl = getAclResources().get(index);
    ResourceRequestBuilderFactory.<Acl> newBuilder().forResource("/authz" + acl.getResource() + "?subject=" + subject.getPrincipal() + "&type=" + subject.getType()).delete()//
    .withCallback(new ResourceCallback<Acl>() {

      @Override
      public void onResource(Response response, Acl resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          if(index == getAclResources().size() - 1) {
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

  public static Builder newBuilder(String name, String resource, String perm) {
    return new Builder(name, resource, perm);
  }

  public static ResourceAuthorizationRequestBuilder newResourceAuthorizationRequestBuilder() {
    return ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/authz/query").get();
  }

  //
  // Inner classes
  //

  public static class Builder {
    private AclRequest request;

    Builder(String name, String resource, String perm) {
      request = new AclRequest(name, resource, perm);
    }

    public Builder and(String resource, String perm) {
      request.addAclResource(resource, perm);
      return this;
    }

    public AclRequest build() {
      // make a copy to ensure callback are not conflicting between subject types
      return new AclRequest(request);
    }

  }

  //
  // Interfaces
  //

  public interface AclGetCallback {
    public void onGet(JsArray<Acls> resource);

    public void onGetFailed(Response response);
  }

  public interface AclDeleteCallback {
    public void onDelete(Subject subject);

    public void onDeleteFailed(Response response, Subject subject);
  }

  public interface AclAddCallback {
    public void onAdd(Acl resource);

    public void onAddFailed(Response response, Subject subject, String resource, String perm);
  }
}
