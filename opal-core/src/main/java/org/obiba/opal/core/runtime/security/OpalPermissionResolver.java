/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import org.apache.shiro.authz.Permission;
import org.obiba.opal.core.runtime.security.SpatialRealm.RestSpace;

import eu.flatwhite.shiro.spatial.SingleSpaceRelationProvider;
import eu.flatwhite.shiro.spatial.SingleSpaceResolver;
import eu.flatwhite.shiro.spatial.SpatialPermissionResolver;
import eu.flatwhite.shiro.spatial.finite.NodeRelationProvider;
import eu.flatwhite.shiro.spatial.finite.NodeResolver;

public class OpalPermissionResolver extends SpatialPermissionResolver {

  public OpalPermissionResolver() {
    super(new SingleSpaceResolver(new RestSpace()), new NodeResolver(), new SingleSpaceRelationProvider(new NodeRelationProvider()));
  }

  @Override
  public Permission resolvePermission(String permissionString) {
    // TODO Auto-generated method stub
    return super.resolvePermission(permissionString);
  }
}
