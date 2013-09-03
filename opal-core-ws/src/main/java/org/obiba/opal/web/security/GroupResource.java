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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@SuppressWarnings("UnusedDeclaration")
@Component
@Scope("request")
@Path("/group/{name}")
public class GroupResource extends AbstractUserGroupResource {

  @PathParam("name")
  private String name;

  @Autowired
  GroupResource(UserService userService) {
    super(userService);
  }

  @GET
  public Response getGroup() {

    Group group = userService.getGroupWithName(name);
    if(group == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok().entity(asDto(group)).build();
  }

  @DELETE
  public Response deleteGroup() {
    Group group = userService.getGroupWithName(name);
    if(group != null && !group.getUsers().isEmpty()) {
      // cannot delete a group with users
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    if(group != null) {
      userService.deleteGroup(group);
    }

    return Response.ok().build();
  }
}