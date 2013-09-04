/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

@Component
@Scope("request")
@Path("/authz-subject/{subject:.*}")
public class UserGroupAuthorizationResource {

  private final SubjectAclService subjectAclService;

  @PathParam("subject")
  private String subject;

  @Autowired
  public UserGroupAuthorizationResource(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @GET
  public Iterable<Opal.Acl> get(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") SubjectAclService.SubjectType type) {

    SubjectAclService.Subject aclSubject = type.subjectFor(subject);

    Set<SubjectAclService.Permissions> permissions = new HashSet<SubjectAclService.Permissions>();
    for(SubjectAclService.Permissions p : subjectAclService.getSubjectPermissions(aclSubject)) {
      if(p.getDomain().equals(domain)) {
        permissions.add(p);
      }
    }

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }
}
