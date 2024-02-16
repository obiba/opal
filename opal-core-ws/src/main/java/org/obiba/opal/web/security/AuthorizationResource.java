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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
@Scope("request")
@Path("/authz/{resource:.*}")
public class AuthorizationResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("resource")
  private String resource;

  @GET
  public Iterable<Opal.Acl> get(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") SubjectType type) {
    return Iterables
        .transform(subjectAclService.getNodePermissions(domain, getNode(), type), PermissionsToAclFunction.INSTANCE);
  }

  @POST
  public Opal.Acl add(@QueryParam("domain") @DefaultValue("opal") String domain, @QueryParam("subject") String subject,
      @QueryParam("type") SubjectType type, @QueryParam("perm") String permission) {
    subjectAclService.addSubjectPermission(domain, getNode(), type.subjectFor(subject), permission);
    return PermissionsToAclFunction.INSTANCE
        .apply(subjectAclService.getSubjectNodePermissions(domain, getNode(), type.subjectFor(subject)));
  }

  @DELETE
  public Opal.Acl delete(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("subject") String subject, @QueryParam("type") SubjectType type, @QueryParam("perm") String permission) {
    if(Strings.isNullOrEmpty(permission)) {
      subjectAclService.deleteSubjectPermissions(domain, getNode(), type.subjectFor(subject));
    } else {
      subjectAclService.deleteSubjectPermissions(domain, getNode(), type.subjectFor(subject), permission);
    }
    return PermissionsToAclFunction.INSTANCE
        .apply(subjectAclService.getSubjectNodePermissions(domain, getNode(), type.subjectFor(subject)));
  }

  private String getNode() {
    return '/' + resource;
  }
}
