/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.system.subject;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType.USER;

@Component
@Scope("request")
@Path("/system/subject-profile/{principal}")
public class SubjectProfileResource {

  @PathParam("principal")
  private String principal;

  @Autowired
  private SubjectAclService subjectAclService;
  
  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private ApplicationContext applicationContext;

  @GET
  public Response get() {
    return Response.ok().entity(Dtos.asDto(subjectProfileService.getProfile(principal))).build();
  }

  @DELETE
  public Response delete() {
    subjectProfileService.deleteProfile(principal);
    subjectAclService.deleteSubjectPermissions(USER.subjectFor(principal));
    return Response.ok().build();
  }

  @Path("/bookmarks")
  public BookmarksResource getBookmarks() {
    BookmarksResource resource = applicationContext.getBean(BookmarksResource.class);
    resource.setPrincipal(principal);
    return resource;
  }

}