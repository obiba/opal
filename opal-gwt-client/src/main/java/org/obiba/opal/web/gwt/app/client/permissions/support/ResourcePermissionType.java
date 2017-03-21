/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.support;

import java.util.ArrayList;
import java.util.Collections;

import org.obiba.opal.web.model.client.opal.AclAction;

public enum ResourcePermissionType {
  ADMINISTRATION(AclAction.PROJECT_ADD, AclAction.SYSTEM_ALL),
  DATASHIELD(AclAction.DATASHIELD_USE, AclAction.DATASHIELD_ALL),
  R(AclAction.R_USE),
  PROJECT(AclAction.PROJECT_ALL),
  DATASOURCE(AclAction.TABLE_ADD, AclAction.DATASOURCE_ALL),
  VCF_STORE(AclAction.VCF_STORE_VIEW, AclAction.VCF_STORE_EDIT, AclAction.VCF_STORE_ALL),
  TABLE(AclAction.TABLE_READ, AclAction.TABLE_VALUES, AclAction.TABLE_EDIT, AclAction.TABLE_VALUES_EDIT,
      AclAction.TABLE_ALL),
  VARIABLE(AclAction.VARIABLE_READ),
  REPORT_TEMPLATE(AclAction.REPORT_TEMPLATE_READ, AclAction.REPORT_TEMPLATE_ALL);

  public ArrayList<AclAction> getPermissions() {
    return (ArrayList<AclAction>) permissions.clone();
  }

  public boolean hasPermission(String name) {
    for(AclAction permission : permissions) {
      if(permission.getName().equals(name)) return true;
    }
    return false;
  }

  public static ResourcePermissionType getTypeByPermission(String permission) {
    for(ResourcePermissionType type : ResourcePermissionType.values()) {
      for(AclAction action : type.permissions) {
        if(action.getName().equals(permission)) {
          return type;
        }
      }
    }

    throw new IllegalArgumentException("AclAction is invalid");
  }

  public int count() {
    return permissions.size();
  }

  ResourcePermissionType(AclAction... permissions) {
    Collections.addAll(this.permissions, permissions);
  }

  private final ArrayList<AclAction> permissions = new ArrayList<AclAction>();
}
