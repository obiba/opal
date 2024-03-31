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

import java.util.Collection;
import java.util.HashSet;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
@Scope("request")
@Path("/authz-subject/{subject:.*}")
public class SubjectCredentialsGroupAuthorizationResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("subject")
  private String subject;

  @GET
  public Iterable<Opal.Acl> get(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") SubjectType type) {

    SubjectAcl.Subject aclSubject = type.subjectFor(subject);
    Collection<SubjectAclService.Permissions> permissions = new HashSet<>();
    for(SubjectAclService.Permissions p : subjectAclService.getSubjectPermissions(aclSubject)) {
      if(p.getDomain().equals(domain)) {
        permissions.add(p);
      }
    }

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }
}
