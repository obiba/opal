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

import java.util.List;

import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Converts datasource tables related resources permissions from opal domain to magma domain.
 */
@Component
public class TablePermissionConverter extends OpalPermissionConverter {

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
    TABLE_ALL {
      @Override
      public Iterable<String> convert(String node) {
        boolean isView = node.contains("/view/");
        String[] args = isView ? args(node, "/datasource/(.+)/view/(.+)") : args(node, "/datasource/(.+)/table/(.+)");

        List<String> perms = Lists.newArrayList(toRest("/datasource/{0}/table/{1}", "*:GET/*", args),
            toRest("/datasource/{0}/_sql", "POST:GET", args),
            toRest("/datasources/_sql", "POST", args),
            toRest("/project/{0}/commands/_export", "POST:GET", args),
            toRest("/project/{0}/commands/_copy", "POST:GET", args),
            toRest("/project/{0}/commands/_refresh", "POST:GET", args),
            toRest("/project/{0}", "GET:GET", args),
            toRest("/project/{0}/resources", "GET:GET", args),
            toRest("/project/{0}/state", "GET:GET", args),
            toRest("/project/{0}/summary", "GET:GET", args),
            toRest("/project/{0}/permissions/table/{1}", "*:GET/*", args),
            toRest("/files/projects/{0}", "GET:GET/*", args),
            toRest("/files/projects/{0}", "POST:GET/*", args),
            toRest("/files/projects/{0}", "PUT:GET/*", args),
            toRest("/system/subject-profiles/_search", "GET"));

        if(isView) perms.add(toRest("/datasource/{0}/view/{1}", "*:GET/*", args));

        return perms;
      }

    },
    TABLE_READ {
      @Override
      public Iterable<String> convert(String node) {
        boolean isView = node.contains("/view/");
        String[] args = isView ? args(node, "/datasource/(.+)/view/(.+)") : args(node, "/datasource/(.+)/table/(.+)");

        List<String> perms = Lists.newArrayList(toRest("/datasource/{0}/table/{1}", "GET:GET", args),
            toRest("/datasource/{0}/table/{1}/index", "GET", args),
            toRest("/datasource/{0}/table/{1}/index/_schema", "GET", args),
            toRest("/datasource/{0}/table/{1}/variable", "GET:GET/GET", args),
            toRest("/datasource/{0}/table/{1}/variables", "GET:GET/GET", args),
            toRest("/datasource/{0}/table/{1}/facet", "GET:GET/GET", args),
            toRest("/datasource/{0}/table/{1}/facets/_search", "POST:GET", args),
            toRest("/datasource/{0}/table/{1}/variable/_transient/summary", "POST", args),
            toRest("/project/{0}", "GET:GET", args),
            toRest("/project/{0}/resources", "GET:GET", args),
            toRest("/project/{0}/state", "GET:GET", args),
            toRest("/project/{0}/summary", "GET:GET", args));

        if(isView) perms.add(toRest("/datasource/{0}/view/{1}/xml", "GET:GET", args));

        return perms;
      }

    },
    TABLE_VALUES {
      @Override
      public Iterable<String> convert(String node) {
        boolean isView = node.contains("/view/");
        String[] args = isView ? args(node, "/datasource/(.+)/view/(.+)") : args(node, "/datasource/(.+)/table/(.+)");

        List<String> perms = Lists.newArrayList(toRest("/datasource/{0}/table/{1}/valueSet", "GET:GET/GET", args),
            toRest("/datasource/{0}/table/{1}/entities", "GET", args),
            toRest("/datasource/{0}/table/{1}/index", "GET:GET/GET", args),
            toRest("/datasource/{0}/table/{1}/index/_search", "GET", args),
            toRest("/datasource/{0}/table/{1}/index/_search", "POST", args),
            toRest("/datasource/{0}/table/{1}/index/_schema", "GET", args),
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
            toRest("/project/{0}/table/{1}/analyses", "GET:GET", args),
            toRest("/project/{0}/table/{1}/analyses/_export", "GET:GET", args),
            toRest("/project/{0}/table/{1}/analysis", "GET:GET/GET", args));
        Iterables.addAll(perms, TABLE_READ.convert(node));
        return perms;
      }

    },
    TABLE_EDIT {
      @Override
      public Iterable<String> convert(String node) {
        boolean isView = node.contains("/view/");
        String[] args = isView ? args(node, "/datasource/(.+)/view/(.+)") : args(node, "/datasource/(.+)/table/(.+)");

        List<String> perms = Lists.newArrayList(toRest("/datasource/{0}/table/{1}", "PUT:GET", args),
            toRest("/datasource/{0}/table/{1}", "DELETE", args),
            toRest("/datasource/{0}/table/{1}/variables", "POST:GET/*", args),
            toRest("/datasource/{0}/table/{1}/variables", "DELETE:GET", args));

        if(isView) {
          perms.addAll(Lists.newArrayList(toRest("/datasource/{0}/view/{1}", "PUT:GET", args),
              toRest("/datasource/{0}/view/{1}", "DELETE", args),
              toRest("/datasource/{0}/table/{1}", "DELETE", args),
              toRest("/datasource/{0}/table/{1}/index", "GET", args),
              toRest("/datasource/{0}/table/{1}/index/_schema", "GET", args),
              toRest("/datasource/{0}/view/{1}/variables", "POST:GET/*", args),
              toRest("/datasource/{0}/view/{1}/variables", "DELETE:GET/*", args),
              toRest("/datasource/{0}/view/{1}/from/variable/_transient/summary", "GET:GET", args),
              toRest("/datasource/{0}/view/{1}/from/variable/_transient/summary", "POST:GET", args),
              toRest("/datasource/{0}/view/{1}/from/variable/_transient/_compile", "POST:GET", args),
              toRest("/datasource/{0}/view/{1}/vcs", "GET:GET/*", args)));
        }

        perms.addAll(Lists.newArrayList(toRest("/datasource/{0}/table/{1}/index", "GET:GET", args),
            toRest("/datasource/{0}/_sql", "POST:GET", args),
            toRest("/datasources/_sql", "POST", args),
            toRest("/datasource/{0}/table/{1}/index/schedule", "GET:GET", args),
            toRest("/files/projects/{0}", "GET:GET/*", args),
            toRest("/files/projects/{0}", "POST:GET/*", args),
            toRest("/files/projects/{0}", "PUT:GET/*", args)));

        Iterables.addAll(perms, TABLE_READ.convert(node));
        return perms;
      }

    },
    TABLE_VALUES_EDIT {
      @Override
      public Iterable<String> convert(String node) {
        boolean isView = node.contains("/view/");
        String[] args = isView ? args(node, "/datasource/(.+)/view/(.+)") : args(node, "/datasource/(.+)/table/(.+)");

        List<String> perms = Lists.newArrayList();
        if(isView) perms.add(toRest("/datasource/{0}/view/{1}/from/valueSets/variable/_transient", "POST", args));
        Iterables.addAll(perms, TABLE_VALUES.convert(node));
        Iterables.addAll(perms, TABLE_EDIT.convert(node));
        return perms;
      }

    };

    public abstract Iterable<String> convert(String node);

  }

}
