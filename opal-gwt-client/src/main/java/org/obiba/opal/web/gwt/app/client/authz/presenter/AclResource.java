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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Acls;

public class AclResource {
  private String resource;

  private String action;

  public AclResource(String resource, String action) {
    super();
    this.resource = resource;
    this.action = action;
  }

  public String getResource() {
    return resource;
  }

  public String getAction() {
    return action;
  }

  public boolean hasPermission(Acls acls) {
    for(Acl acl : JsArrays.toIterable(acls.getAclsArray())) {
      if(acl.getResource().equals(resource) && hasAction(acl)) return true;
    }
    return false;
  }

  private boolean hasAction(Acl acl) {
    for(int i = 0; i < acl.getActionsArray().length(); i++) {
      if(acl.getActionsArray().get(i).equals(action)) {
        return true;
      }
    }
    return false;
  }
}