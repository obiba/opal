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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclGetCallback;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.Acls;
import org.obiba.opal.web.model.client.opal.Subject;
import org.obiba.opal.web.model.client.opal.Subject.SubjectType;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;

/**
 * Helper class that handles a set of {@link AclRequest}.
 */
public class SubjectPermissionsRequest {

  private final SubjectType type;

  private List<AclRequest> aclRequests = new ArrayList<AclRequest>();

  private AclGetCallback aclGetCallback;

  public SubjectPermissionsRequest(SubjectType type, AclCallback callback, AclRequest... requests) {
    this.type = type;
    for(AclRequest req : requests) {
      // make a copy to ensure callbacks are not conflicting between subject types
      aclRequests.add(req.copy());
    }
    setAclCallback(callback);
  }

  public void get() {
    StringBuilder query = new StringBuilder();
    List<String> nodes = new ArrayList<String>();
    for(AclRequest req : aclRequests) {
      String node = req.getResource();
      if(nodes.contains(node) == false) {
        query.append("&").append("node=").append(node);
        nodes.add(node);
      }
    }

    ResourceRequestBuilderFactory.<JsArray<Acls>> newBuilder().forResource("/authz/query?domain=opal&type=" + type + query.toString()).get().withCallback(new ResourceCallback<JsArray<Acls>>() {

      @Override
      public void onResource(Response response, JsArray<Acls> resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          aclGetCallback.onGet(JsArrays.toSafeArray(resource));
        } else {
          aclGetCallback.onGetFailed(response);
        }
      }
    }).send();
  }

  public void getSubjects(final AclGetCallback callback) {
    ResourceRequestBuilderFactory.<JsArray<Acls>> newBuilder().forResource("/authz/query?domain=opal&type=" + type).get().withCallback(new ResourceCallback<JsArray<Acls>>() {

      @Override
      public void onResource(Response response, JsArray<Acls> resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          callback.onGet(JsArrays.toSafeArray(resource));
        } else {
          callback.onGetFailed(response);
        }
      }
    }).send();
  }

  private AclRequest getMainAclRequest() {
    return aclRequests.get(0);
  }

  public boolean hasAclRequest(String header) {
    for(AclRequest req : aclRequests) {
      if(req.getHeader().equals(header)) {
        return true;
      }
    }
    return false;
  }

  public AclRequest getAclRequest(String header) {
    for(AclRequest req : aclRequests) {
      if(req.getHeader().equals(header)) {
        return req;
      }
    }
    return null;
  }

  public Iterable<String> getHeaders() {
    List<String> names = new ArrayList<String>();
    for(AclRequest req : aclRequests) {
      names.add(req.getHeader());
    }
    return names;
  }

  public void delete(Subject subject) {
    for(int i = aclRequests.size() - 1; i >= 0; i--) {
      aclRequests.get(i).delete(subject);
    }
  }

  public void add(Subject subject) {
    getMainAclRequest().add(subject);
  }

  private void setAclCallback(AclCallback callback) {
    this.aclGetCallback = callback;
    for(AclRequest req : aclRequests) {
      req.setAclDeleteCallback(callback);
      req.setAclAddCallback(callback);
    }
  }

  public boolean hasPermission(String header, Acls acls) {
    if(hasAclRequest(header)) {
      if(!getAclRequest(header).hasPermission(acls)) {
        return false;
      }
    }
    return true;
  }

  public void authorize(Subject subject, String header) {
    if(hasAclRequest(header)) {
      getAclRequest(header).add(subject);
    }
  }

  public void unauthorize(Subject subject, String header) {
    if(hasAclRequest(header)) {
      getAclRequest(header).delete(subject);
    }
  }

}
