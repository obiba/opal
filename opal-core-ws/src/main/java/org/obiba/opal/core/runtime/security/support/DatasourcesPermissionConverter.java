/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.security.support;

import java.util.List;

import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Converts datasources related resources permissions from opal domain to magma domain.
 */
@Component
public class DatasourcesPermissionConverter extends OpalPermissionConverter {

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

  enum Permission {
    DATASOURCE_ALL {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)");
        return Lists.newArrayList(magmaConvert("/datasource/{0}", "*:GET/*", args),//
            magmaConvert("/shell/command", "*:GET/*"), //
            magmaConvert("/functional-units/unit", "GET:GET/GET"),  //
            magmaConvert("/functional-units/entities/table", "GET"));
      }

    },
    CREATE_TABLE {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)");
        List<String> perms = Lists.newArrayList(magmaConvert("/datasource/{0}/tables", "GET:GET", args), //
            magmaConvert("/datasource/{0}/tables", "POST:GET", args));
        return perms;
      }
    },
    CREATE_VIEW {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)");
        List<String> perms = Lists.newArrayList(magmaConvert("/datasource/{0}/tables", "GET:GET", args), //
            magmaConvert("/datasource/{0}/views", "POST:GET", args));
        return perms;
      }
    },
    TABLE_ALL {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/table/(.+)");
        return Lists.newArrayList(magmaConvert("/datasource/{0}/table/{1}", "*:GET/*", args));
      }

    },
    TABLE_READ {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/table/(.+)");
        return Lists.newArrayList(magmaConvert("/datasource/{0}/table/{1}", "GET:GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/variable", "GET:GET/GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/variables", "GET:GET/GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/facet", "GET:GET/GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/facets", "POST:GET/*", args),//
            magmaConvert("/datasource/{0}/table/{1}/variable/_transient/summary", "POST", args));
      }

    },
    TABLE_VALUES {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/table/(.+)");
        return Lists.newArrayList(magmaConvert("/datasource/{0}/table/{1}/valueSet", "GET:GET/GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/entities", "GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/variables", "GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/index", "GET:GET/GET", args), //
            magmaConvert("/datasource/{0}/table/{1}/index/_search", "GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/index/_search", "POST", args),//
            magmaConvert("/datasource/{0}/table/{1}/index/_schema", "GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/facet", "GET:GET/GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/facets", "POST:GET/*", args));
      }

    },
    TABLE_EDIT {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/table/(.+)");
        List<String> perms = Lists.newArrayList(magmaConvert("/datasource/{0}/table/{1}/variables", "POST:GET", args),
            magmaConvert("/datasource/{0}/table/{1}/index", "*:GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/index/schedule", "*:GET", args));
        Iterables.addAll(perms, TABLE_READ.convert(node));
        return perms;
      }

    },
    VIEW_ALL {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/view/(.+)");
        List<String> perms = Lists.newArrayList(magmaConvert("/datasource/{0}/view/{1}", "*:GET/*", args));
        Iterables.addAll(perms, TABLE_ALL.convert(node.replace("/view/", "/table/")));
        return perms;
      }

    },
    VIEW_READ {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/view/(.+)");
        List<String> perms = Lists.newArrayList(magmaConvert("/datasource/{0}/view/{1}/xml", "GET:GET", args));
        Iterables.addAll(perms, TABLE_READ.convert(node.replace("/view/", "/table/")));
        return perms;
      }

    },
    VIEW_VALUES {
      @Override
      public Iterable<String> convert (String node){
        return TABLE_VALUES.convert(node.replace("/view/", "/table/"));
      }

    },
    VIEW_EDIT {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/view/(.+)");
        List<String> perms = Lists.newArrayList(magmaConvert("/datasource/{0}/view/{1}", "PUT:GET", args),//
            magmaConvert("/datasource/{0}/view/{1}/variables", "POST:GET", args),//
            magmaConvert("/datasource/{0}/view/{1}/from/variable/_transient/summary", "GET:GET", args),//
            magmaConvert("/datasource/{0}/view/{1}/from/variable/_transient/summary", "POST:GET", args),//
            magmaConvert("/datasource/{0}/view/{1}/from/variable/_transient/_compile", "POST:GET", args));
        Iterables.addAll(perms, VIEW_READ.convert(node));
        return perms;
      }

    },
    VIEW_VALUES_EDIT {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/view/(.+)");
        List<String> perms = Lists
            .newArrayList(magmaConvert("/datasource/{0}/view/{1}/from/valueSets/variable/_transient", "POST", args));
        Iterables.addAll(perms, VIEW_VALUES.convert(node));
        Iterables.addAll(perms, VIEW_EDIT.convert(node));
        return perms;
      }

    },
    VARIABLE_READ {
      @Override
      public Iterable<String> convert (String node){
        String[] args = args(node, "/datasource/(.+)/table/(.+)/variable/(.+)");
        return Lists.newArrayList(magmaConvert("/datasource/{0}/table/{1}/variable/{2}", "GET:GET/GET", args),//
            magmaConvert("/datasource/{0}/table/{1}/variable/_transient/summary", "POST:GET", args));
      }
    } ;

    public abstract Iterable<String> convert (String node);

  }

}
