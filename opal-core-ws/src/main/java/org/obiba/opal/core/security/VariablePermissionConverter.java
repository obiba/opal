/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Converts datasource table variables related resources permissions from opal domain to magma domain.
 */
@Component
public class VariablePermissionConverter extends OpalPermissionConverter {

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
    VARIABLE_READ {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/datasource/(.+)/table/(.+)/variable/(.+)");
        return Lists.newArrayList(toRest("/datasource/{0}/table/{1}/variable/{2}", "GET:GET/GET", args),//
            toRest("/datasource/{0}/table/{1}/variable/_transient/summary", "POST:GET", args),//
            toRest("/project/{0}", "GET:GET", args),//
            toRest("/project/{0}/state", "GET:GET", args),//
            toRest("/project/{0}/summary", "GET:GET", args));
      }
    };

    public abstract Iterable<String> convert(String node);

  }

}
