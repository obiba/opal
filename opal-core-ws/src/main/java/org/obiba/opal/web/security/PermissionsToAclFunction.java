/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import org.obiba.opal.core.service.SubjectAclService.Permissions;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.Acl;

import com.google.common.base.Function;

class PermissionsToAclFunction implements Function<Permissions, Opal.Acl> {

  static final PermissionsToAclFunction INSTANCE = new PermissionsToAclFunction();

  @Override
  public Acl apply(Permissions from) {
    return Acl.newBuilder().setPrincipal(from.getSubject()).setResource(from.getNode()).addAllActions(from.getPermissions()).build();
  }

}