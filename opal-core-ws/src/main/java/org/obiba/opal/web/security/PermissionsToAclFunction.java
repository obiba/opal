/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.security;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService.Permissions;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.Acl;

import com.google.common.base.Function;

public class PermissionsToAclFunction implements Function<Permissions, Opal.Acl> {

  public static final PermissionsToAclFunction INSTANCE = new PermissionsToAclFunction();

  @Override
  public Acl apply(Permissions from) {
    Acl.Builder builder = Acl.newBuilder().setDomain(from.getDomain()).setSubject(valueOf(from.getSubject()))
        .setResource(from.getNode());//.addAllActions(from.getPermissions());
    for(String p : from.getPermissions()) {
      builder.addActions(p);
    }
    return builder.build();
  }

  static final Opal.Subject valueOf(SubjectAcl.Subject from) {
    return Opal.Subject.newBuilder().setPrincipal(from.getPrincipal())
        .setType(Opal.Subject.SubjectType.valueOf(from.getType().name())).build();
  }

}