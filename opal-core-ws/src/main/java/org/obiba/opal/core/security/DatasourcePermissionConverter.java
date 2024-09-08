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
 * Converts datasources related resources permissions from opal domain to magma domain.
 */
@Component
public class DatasourcePermissionConverter extends OpalPermissionConverter {

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
    DATASOURCE_ALL {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/datasource/(.+)");
        return Lists.newArrayList(toRest("/datasource/{0}", "*:GET/*", args),
            toRest("/datasource/{0}/_sql", "POST:GET", args),
            toRest("/datasources/_sql", "POST", args),
            toRest("/identifiers/mappings", "GET"),
            toRest("/datasource-plugin", "GET:GET/GET"),
            toRest("/project/{0}", "GET:GET", args),
            toRest("/project/{0}/summary", "GET:GET", args),
            toRest("/project/{0}/state", "GET:GET", args),
            toRest("/project/{0}/identifiers-mappings", "GET:GET", args),
            toRest("/project/{0}/transient-datasource", "*:GET/*", args),
            toRest("/project/{0}/command", "*:GET/*", args),
            toRest("/project/{0}/permissions/datasource", "*:GET/*", args),
            toRest("/project/{0}/permissions/table", "*:GET/*", args),
            toRest("/project/{0}/permissions/subject", "GET:GET/GET", args),
            toRest("/files/projects/{0}", "GET:GET/*", args),
            toRest("/files/projects/{0}", "POST:GET/*", args),
            toRest("/files/projects/{0}", "PUT:GET/*", args),
            toRest("/system/subject-profiles/_search", "GET"));
      }

    },
    TABLE_ADD {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/datasource/(.+)");
        return Lists.newArrayList(toRest("/datasource/{0}/tables", "GET:GET", args),
            toRest("/datasource/{0}/_sql", "POST:GET", args),
            toRest("/datasources/_sql", "POST", args),
            toRest("/datasource/{0}/tables", "POST:GET", args),
            toRest("/datasource/{0}/tables", "DELETE", args),
            toRest("/datasource/{0}/views", "POST:GET", args),
            toRest("/identifiers/mappings", "GET"),
            toRest("/datasource-plugin", "GET:GET/GET"),
            toRest("/project/{0}", "GET:GET", args),
            toRest("/project/{0}/resources", "GET:GET", args),
            toRest("/project/{0}/state", "GET:GET", args),
            toRest("/project/{0}/identifiers-mappings", "GET:GET", args),
            toRest("/project/{0}/summary", "GET:GET", args),
            toRest("/project/{0}/transient-datasources", "POST", args),
            toRest("/project/{0}/commands/_import", "POST:GET", args),
            toRest("/project/{0}/commands/_export", "POST:GET", args),
            toRest("/project/{0}/commands/_copy", "POST:GET", args),
            toRest("/project/{0}/commands/_refresh", "POST:GET", args),
            toRest("/files/projects/{0}", "GET:GET/*", args),
            toRest("/files/projects/{0}", "POST:GET/*", args),
            toRest("/files/projects/{0}", "PUT:GET/*", args));
      }
    },
    DATASOURCE_VIEW {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/datasource/(.+)");
        return Lists.newArrayList(toRest("/datasource/{0}", "GET:GET/GET", args),
            toRest("/datasource/{0}/_sql", "POST:GET", args),
            toRest("/datasources/_sql", "POST", args),
            toRest("/project/{0}/commands/_analyse", "POST:GET", args),
            toRest("/project/{0}/commands/_export", "POST:GET", args),
            toRest("/project/{0}/commands/_copy", "POST:GET", args),
            toRest("/project/{0}/commands/_refresh", "POST:GET", args),
            toRest("/project/{0}", "GET:GET", args),
            toRest("/project/{0}/resources", "GET:GET", args),
            toRest("/project/{0}/state", "GET:GET", args),
            toRest("/project/{0}/summary", "GET:GET", args),
            toRest("/project/{0}/analyses", "GET:GET", args),
            toRest("/project/{0}/analyses/_export", "GET:GET", args));
      }

    };

    public abstract Iterable<String> convert(String node);

  }

}
