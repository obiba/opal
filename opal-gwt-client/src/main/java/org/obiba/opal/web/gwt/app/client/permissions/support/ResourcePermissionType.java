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
import java.util.Iterator;

import org.obiba.opal.web.model.client.opal.AclAction;

public enum ResourcePermissionType {
  PROJECT(AclAction.PROJECT_ALL),
  DATASOURCE(AclAction.CREATE_TABLE, AclAction.DATASOURCE_ALL),
  TABLE(AclAction.TABLE_READ, AclAction.TABLE_VALUES, AclAction.TABLE_EDIT, AclAction.TABLE_VALUES_EDIT,
      AclAction.TABLE_ALL),
  VARIABLE(AclAction.VARIABLE_READ),
  REPORT_TEMPLATE(AclAction.REPORT_TEMPLATE_ALL, AclAction.REPORT_TEMPLATE_READ);

  public ArrayList<AclAction> getPermissions() {
    return (ArrayList<AclAction>) permissions.clone();
  }

  public boolean hasPermission(String name) {
    for (Iterator<AclAction> iterator = permissions.iterator(); iterator.hasNext();) {
      if (iterator.next().getName().equals(name)) return true;
    }

    return false;
  }

  public static ResourcePermissionType getTypeByPermission(String permission) {
    for (ResourcePermissionType type : ResourcePermissionType.values()) {
      for (AclAction action : type.permissions) {
        if (action.getName().equals(permission)) {
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
    for(AclAction permission : permissions) {
      this.permissions.add(permission);
    }
  }

  private final ArrayList<AclAction> permissions = new ArrayList<AclAction>();
}
