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

@Component
@Scope("request")
@Path("/group/{name}")
public class GroupResource {

  @PathParam("name")
  private String name;

  @Autowired
  private UserService userService;

  @GET
  public Response getGroup() {
    Group group = userService.getGroup(name);
    if(group == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok().entity(Dtos.asDto(group)).build();
  }

  @DELETE
  public Response deleteGroup() {
    Group group = userService.getGroup(name);
    if(group != null) {
      if(group.getUsers().isEmpty()) {
        userService.deleteGroup(group);
      } else {
        // cannot delete a group with users
        //TODO add message to explain what failed
        return Response.status(Response.Status.PRECONDITION_FAILED).build();
      }
    }
    return Response.ok().build();
  }
}