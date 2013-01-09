/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security.support;

import java.util.List;

import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Converts opal administration related resources permissions from opal domain to magma domain.
 */
@Component
public class FilesPermissionConverter extends OpalPermissionConverter {

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
    /**
     * Read, write and delete.
     */
    FILES_ALL {
      @Override
      Iterable<String> convert(String node) {
        String[] args = args(node, "/files/(.+)");
        if(args.length == 0) {
          return Lists.newArrayList(magmaConvert("/files", "*:GET/*"));
        } else {
          return Lists.newArrayList(magmaConvert("/files/{0}", "*:GET/*", args));
        }
      }
    },
    /**
     * Read, write but cannot delete.
     */
    FILES_SHARE {
      @Override
      Iterable<String> convert(String node) {
        String[] args = args(node, "/files/(.+)");
        if(args.length == 0) {
          return Lists.newArrayList(magmaConvert("/files", "GET:GET/*"), //
              magmaConvert("/files", "POST:GET/*"));
        } else {
          return Lists.newArrayList(magmaConvert("/files/{0}", "GET:GET/*", args),//
              magmaConvert("/files/{0}", "POST:GET/*", args));
        }
      }
    },
    /**
     * Read only.
     */
    FILES_READ {
      @Override
      Iterable<String> convert(String node) {
        String[] args = args(node, "/files/(.+)");
        if(args.length == 0) {
          return Lists.newArrayList(magmaConvert("/files", "GET:GET/GET"));
        } else {
          return Lists.newArrayList(magmaConvert("/files/{0}", "GET:GET/GET", args));
        }
      }

    };

    abstract Iterable<String> convert(String node);

  }

}
