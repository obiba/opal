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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.impl.UserService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/user/{name}")
public class UserResource {

  @PathParam("name")
  private String name;

  @Autowired
  private UserService userService;

  @GET
  public Response getUser() {
    //TODO: Use TimestampedResponses.evaluate(request, ...); ?
    User user = userService.getUser(name);
    if(user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok().entity(Dtos.asDto(user)).build();
  }

  @PUT
  public Response updateUser(Opal.UserDto dto) {
    if(!name.equals(dto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    User user = Dtos.fromDto(dto);
    if(dto.hasPassword()) {
      user.setPassword(User.digest(dto.getPassword()));
    }
    userService.save(user);
    return Response.ok().build();
  }

  @DELETE
  public Response deleteUser() {
    User user = userService.getUser(name);
    if(user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    userService.deleteUser(user);
    return Response.ok().build();
  }
}