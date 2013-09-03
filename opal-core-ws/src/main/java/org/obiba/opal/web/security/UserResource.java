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
public class UserResource extends AbstractUserGroupResource {

  @PathParam("name")
  private String name;

  @Autowired
  public UserResource(UserService userService) {
    super(userService);
  }

  @GET
  public Response getUser() {
    User user = userService.getUserWithName(name);

    if(user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok().entity(asDto(user)).build();

  }

  @PUT
  public Response updateUser(Opal.UserDto userDto) {

    if(!name.equals(userDto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    User user = fromDto(userDto);

    if(userDto.hasPassword()) {
      user.setPassword(User.digest(userDto.getPassword()));
    }

    userService.createOrUpdateUser(user);

    return Response.ok().build();
  }

  @DELETE
  public Response deleteUser() {

    User u = userService.getUserWithName(name);

    // Delete user
    if(u != null) {
      userService.deleteUser(u);
    }

    // TODO: Clear Acl permission

    return Response.ok().build();
  }
}