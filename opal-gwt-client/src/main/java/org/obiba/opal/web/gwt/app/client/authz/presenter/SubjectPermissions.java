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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SubjectPermissions {

  private String subject;

  private Map<String, Boolean> permissionMap = new HashMap<String, Boolean>();

  public SubjectPermissions(String subject) {
    this.subject = subject;
  }

  public String getSubject() {
    return subject;
  }

  public boolean hasPermission(String name) {
    return permissionMap.get(name);
  }

  public void addPermission(String name) {
    permissionMap.put(name, true);
  }

  public void removePermission(String name) {
    permissionMap.put(name, false);
  }

  public Set<String> getPermissionNames() {
    return permissionMap.keySet();
  }

}
