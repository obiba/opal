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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;

/**
 * Helper class that handles a set of {@link AclRequest}.
 */
public class SubjectPermissionsRequest {

  private List<AclRequest> aclRequests = new ArrayList<AclRequest>();

  private AclGetCallback aclGetCallback;

  public SubjectPermissionsRequest(AclCallback callback, AclRequest.Builder... builders) {
    for(AclRequest.Builder builder : builders) {
      aclRequests.add(builder.build());
    }
    setAclCallback(callback);
  }

  public void get() {
    StringBuilder query = new StringBuilder();
    for(AclRequest req : aclRequests) {
      for(AclResource res : req.getAclResources()) {
        query.append("&").append("node=").append(res.getResource());
      }
    }

    ResourceRequestBuilderFactory.<JsArray<Acls>> newBuilder().forResource("/authz/query?by=subject" + query.toString()).get().withCallback(new ResourceCallback<JsArray<Acls>>() {

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

  public void delete(String subject) {
    for(int i = aclRequests.size() - 1; i >= 0; i--) {
      aclRequests.get(i).delete(subject);
    }
  }

  public void add(String subject) {
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
      for(AclResource res : getAclRequest(header).getAclResources()) {
        if(!res.hasPermission(acls)) {
          return false;
        }
      }
    }
    return true;
  }

  public void authorize(String subject, String header) {
    if(hasAclRequest(header)) {
      getAclRequest(header).add(subject);
    }
  }

  public void unauthorize(String subject, String header) {
    if(hasAclRequest(header)) {
      getAclRequest(header).delete(subject);
    }
  }

}
