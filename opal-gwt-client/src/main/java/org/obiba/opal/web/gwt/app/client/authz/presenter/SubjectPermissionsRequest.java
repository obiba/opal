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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclAddCallback;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclDeleteCallback;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclGetCallback;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclResource;

/**
 *
 */
public class SubjectPermissionsRequest {

  private List<AclRequest> aclRequests = new ArrayList<AclRequest>();

  public SubjectPermissionsRequest(AclRequest.Builder... builders) {
    for(AclRequest.Builder builder : builders) {
      aclRequests.add(builder.build());
    }
  }

  public AclRequest getMainAclRequest() {
    return aclRequests.get(0);
  }

  public Iterable<String> getResources(String header) {
    List<String> resources = new ArrayList<String>();
    for(AclRequest req : aclRequests) {
      if(req.getHeader().equals(header)) {
        for(AclResource res : req.getAcls()) {
          resources.add(res.getResource());
        }
      }
    }
    return resources;
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

  public void setAclGetCallback(AclGetCallback callback) {
    getMainAclRequest().setAclGetCallback(callback);
  }

  public void setAclDeleteCallback(AclDeleteCallback callback) {
    getMainAclRequest().setAclDeleteCallback(callback);
  }

  public void setAclAddCallback(AclAddCallback callback) {
    getMainAclRequest().setAclAddCallback(callback);
  }

}
