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

import org.obiba.opal.web.model.Opal.AclAction;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Converts opal administration related resources permissions from opal domain to magma domain.
 */
@Component
public class AdministrationPermissionConverter extends OpalPermissionConverter {

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
    DATABASES_ALL {
      @Override
      public Iterable<String> convert(String node) {
        return Lists.newArrayList(magmaConvert("/jdbc/databases", "*:POST/*"));
      }

    },
    R_SESSION_ALL {
      @Override
      public Iterable<String> convert(String node) {
        return Lists.newArrayList(magmaConvert("/r/session", "*:GET/*"));
      }

    },
    DATASHIELD_ALL {
      @Override
      public Iterable<String> convert(String node) {
        return Lists.newArrayList(magmaConvert("/datashield", "*:GET/*"));
      }

    },
    DATASHIELD_SESSION_ALL {
      @Override
      public Iterable<String> convert(String node) {
        return Lists.newArrayList(magmaConvert("/datashield/session", "*:GET/*"));
      }

    };

    public abstract Iterable<String> convert(String node);

  }

}
