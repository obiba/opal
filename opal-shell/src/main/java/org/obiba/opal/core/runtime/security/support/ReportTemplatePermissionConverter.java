/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.security.support;

import org.obiba.opal.core.security.DomainPermissionConverter;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Converts opal administration related resources permissions from opal domain to magma domain.
 */
@Component
public class ReportTemplatePermissionConverter extends DomainPermissionConverter {

  public ReportTemplatePermissionConverter() {
    super("opal");
  }

  @Override
  protected boolean hasPermission(String permission) {
    for(Permission perm : Permission.values()) {
      if(perm.toString().equals(permission)) {
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
    REPORT_TEMPLATE_ALL {
      @Override
      public Iterable<String> convert(String node) {
        if(node.startsWith("/project/")) {
          String[] args = args(node, "/project/(.+)/report-template/(.+)");
          return Lists.newArrayList(
              toRest("/project/{0}", "GET:GET", args),//
              toRest("/project/{0}/summary", "GET:GET", args),//
              toRest("/project/{0}/report-template/{1}", "*:GET/*", args),//
              toRest("/files/meta/reports/{0}/{1}", "GET:GET/GET", args),//
              toRest("/files/reports/{0}/{1}", "*:GET/*", args));
        } else {
          String[] args = args(node, "/report-template/(.+)");
          return Lists.newArrayList(toRest("/report-template/{0}", "*:GET/*", args),//
              toRest("/files/meta/reports/{0}", "GET:GET/GET", args),//
              toRest("/files/reports/{0}", "*:GET/*", args));
        }
      }

    },
    REPORT_TEMPLATE_READ {
      @Override
      public Iterable<String> convert(String node) {
        if(node.startsWith("/project/")) {
          String[] args = args(node, "/project/(.+)/report-template/(.+)");
          return Lists.newArrayList(
              toRest("/project/{0}", "GET:GET", args),//
              toRest("/project/{0}/summary", "GET:GET", args),//
              toRest("/project/{0}/report-template/{1}", "GET:GET/GET", args),//
              toRest("/files/meta/reports/{0}/{1}", "GET:GET/GET", args),//
              toRest("/files/reports/{0}/{1}", "GET:GET/GET", args));
        } else {
          String[] args = args(node, "/report-template/(.+)");
          return Lists.newArrayList(toRest("/report-template/{0}", "GET:GET/GET", args),//
              toRest("/files/meta/reports/{0}", "GET:GET/GET", args),//
              toRest("/files/reports/{0}", "GET:GET/GET", args));
        }
      }

    };

    public abstract Iterable<String> convert(String node);

  }

}
