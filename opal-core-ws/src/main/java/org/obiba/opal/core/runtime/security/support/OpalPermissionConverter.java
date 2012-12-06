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

/**
 * Convert a opal domain permission as a set of magma domain permissions.
 */
public abstract class OpalPermissionConverter extends DomainPermissionConverter {

  public static final String OPAL_DOMAIN = "opal";

  public OpalPermissionConverter() {
    super(OPAL_DOMAIN);
  }

  @Override
  protected boolean hasPermission(String permission) {
    for(AclAction action : AclAction.values()) {
      if(action.toString().equals(permission)) {
        return hasPermission(action);
      }
    }
    return false;
  }

  protected abstract boolean hasPermission(AclAction action);

}
