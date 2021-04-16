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

import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Converts projects related resources permissions from opal domain to magma domain.
 */
@Component
public class ProjectPermissionConverter extends OpalPermissionConverter {

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
    PROJECT_ALL {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/project/(.+)");
        return Lists.newArrayList(toRest("/datasource/{0}", "*:GET/*", args),
            toRest("/identifiers/mappings", "GET"),
            toRest("/datasource-plugin", "GET:GET/GET"),
            toRest("/project/{0}", "*:GET/*", args),
            toRest("/files/projects/{0}", "*:GET/*", args),
            toRest("/system/subject-profiles/_search", "GET"));
      }

    };

    public abstract Iterable<String> convert(String node);

  }

}
