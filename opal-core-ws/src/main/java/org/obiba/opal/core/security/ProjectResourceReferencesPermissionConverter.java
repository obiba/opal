/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import com.google.common.collect.Lists;
import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

/**
 * Converts project resource references related resources permissions from opal domain to magma domain.
 */
@Component
public class ProjectResourceReferencesPermissionConverter extends OpalPermissionConverter {

  @Override
  protected boolean hasPermission(AclAction action) {
    for (Permission perm : Permission.values()) {
      if (perm.toString().equals(action.toString())) {
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
    RESOURCES_ALL {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/project/(.+)/resources");
        return Lists.newArrayList(toRest("/project/{0}/resources", "*:GET/*", args),
            toRest("/project/{0}", "GET:GET", args),
            toRest("/datasource/{0}/tables", "GET:GET", args),
            toRest("/project/{0}/summary", "GET:GET", args),
            toRest("/project/{0}/state", "GET:GET", args),
            toRest("/project/{0}/permissions/resources", "*:GET/*", args),
            toRest("/files/projects/{0}", "GET:GET/*", args),
            toRest("/files/projects/{0}", "POST:GET/*", args),
            toRest("/files/projects/{0}", "PUT:GET/*", args),
            toRest("/system/subject-profiles/_search", "GET"));
      }

    },
    RESOURCES_VIEW {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/project/(.+)/resources");
        return Lists.newArrayList(toRest("/project/{0}/resources", "GET:GET/GET", args),
            toRest("/project/{0}", "GET:GET", args),
            toRest("/datasource/{0}/tables", "GET:GET", args),
            toRest("/project/{0}/summary", "GET:GET", args),
            toRest("/project/{0}/state", "GET:GET", args));
      }

    };

    public abstract Iterable<String> convert(String node);

  }

}
