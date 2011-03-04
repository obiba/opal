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

  public Iterable<String> getNames() {
    List<String> names = new ArrayList<String>();
    for(AclRequest req : aclRequests) {
      names.add(req.getName());
    }
    return names;
  }

}
