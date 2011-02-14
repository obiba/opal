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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/{resource:.*}/authz")
public class AuthorizationResource {

  private final SubjectAclService subjectAclService;

  @PathParam("resource")
  private String resource;

  @Autowired
  public AuthorizationResource(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @GET
  @Produces("text/plain")
  public Response get(@Context UriInfo uriInfo) {
    return Response.ok(subjectAclService.getNodePermissions("magma", getNode(uriInfo))).build();
  }

  @PUT
  @Produces("text/plain")
  public Response add(@Context UriInfo uriInfo, @QueryParam("subject") String subject, @QueryParam("perm") String permission) {
    subjectAclService.addSubjectPermission("magma", getNode(uriInfo), subject, permission);
    return Response.created(uriInfo.getRequestUri()).build();
  }

  private String getNode(UriInfo uriInfo) {
    return '/' + resource;
  }

}
