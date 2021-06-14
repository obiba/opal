/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import com.google.common.collect.Lists;
import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts opal administration related resources permissions from opal domain to magma domain.
 */
@Component
public class DataShieldProfilePermissionConverter extends OpalPermissionConverter {

  @Override
  protected boolean hasPermission(AclAction action) {
    for(Permission perm : Permission.values()) {
      if(perm.toString().equals(action.toString())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterable<String> convert(String domain, String node, String permission) {
    return Permission.valueOf(permission.toUpperCase()).convert(node);
  }

  public enum Permission {
    DATASHIELD_PROFILE_USE {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/datashield/profile/(.+)");
        return Lists.newArrayList(toRest("/datashield/session", "*:GET/*"),
            toRest("/datashield/env", "GET:GET/GET"),
            toRest("/datashield/profiles", "GET"),
            toRest("/service/r/workspaces", "GET"),
            toRest("/service/r/workspaces", "DELETE"),
            toRest("/datashield/profile/{0}", "GET:GET/GET", args));
      }
    };

    public abstract Iterable<String> convert(String node);

  }

}
