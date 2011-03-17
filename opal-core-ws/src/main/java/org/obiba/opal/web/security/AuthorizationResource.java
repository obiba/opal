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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
@Path("/authz/{resource:.*}")
public class AuthorizationResource {

  private final SubjectAclService subjectAclService;

  @PathParam("resource")
  private String resource;

  @Autowired
  public AuthorizationResource(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @GET
  public Iterable<Opal.Acl> get(@QueryParam("type") SubjectAclService.SubjectType type) {
    return Iterables.transform(subjectAclService.getNodePermissions("magma", getNode(), type), PermissionsToAclFunction.INSTANCE);
  }

  @POST
  public Opal.Acl add(@QueryParam("subject") String subject, @QueryParam("type") SubjectAclService.SubjectType type, @QueryParam("perm") String permission) {
    subjectAclService.addSubjectPermission("magma", getNode(), type.subjectFor(subject), permission);
    return PermissionsToAclFunction.INSTANCE.apply(subjectAclService.getSubjectPermissions("magma", getNode(), type.subjectFor(subject)));
  }

  @DELETE
  public Opal.Acl delete(@QueryParam("subject") String subject, @QueryParam("type") SubjectAclService.SubjectType type) {
    subjectAclService.deleteSubjectPermissions("magma", getNode(), type.subjectFor(subject));
    return PermissionsToAclFunction.INSTANCE.apply(subjectAclService.getSubjectPermissions("magma", getNode(), type.subjectFor(subject)));
  }

  private String getNode() {
    return '/' + resource;
  }
}
