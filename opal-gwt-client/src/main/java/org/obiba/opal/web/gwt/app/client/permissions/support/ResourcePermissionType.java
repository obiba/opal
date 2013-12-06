/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.support;

import java.util.ArrayList;

import org.obiba.opal.web.model.client.opal.AclAction;

public enum ResourcePermissionType {
  PROJECT(AclAction.PROJECT_ALL),
  DATASOURCE(AclAction.CREATE_TABLE, AclAction.DATASOURCE_ALL),
  TABLE(AclAction.TABLE_ALL, AclAction.TABLE_EDIT, AclAction.TABLE_READ, AclAction.TABLE_VALUES,
      AclAction.TABLE_VALUES_EDIT),
  VARIABLE(AclAction.VARIABLE_READ),
  REPORT_TEMPLATE(AclAction.REPORT_TEMPLATE_ALL, AclAction.REPORT_TEMPLATE_READ);

  public ArrayList<AclAction> getPermissions() {
    return (ArrayList<AclAction>) permissions.clone();
  }

  ResourcePermissionType(AclAction... permissions) {
    for(AclAction permission : permissions) {
      this.permissions.add(permission);
    }
  }

  private final ArrayList<AclAction> permissions = new ArrayList<AclAction>();
}
