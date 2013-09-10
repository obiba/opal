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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.impl.UserAlreadyExistsException;
import org.obiba.opal.web.model.Opal;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/users")
public class UsersResource extends AbstractUserGroupResource {

  @GET
  public List<Opal.UserDto> getUsers() {

    //TODO: Use TimestampedResponses.evaluate(request, ...); ?
    Iterable<User> users = userService.list();

    List<Opal.UserDto> userDtos = Lists.newArrayList();

    if(users != null) {
      for(User u : users) {
        userDtos.add(asDto(u));
      }
    }
    return userDtos;
  }

  @POST
  public Response createUser(Opal.UserDto userDto) {

    if(userService.getUserWithName(userDto.getName()) != null) {
      return Response.status(Response.Status.CONFLICT).build();
    }

    if(!userDto.hasPassword()) {
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    User u = fromDto(userDto);
    u.setPassword(User.digest(userDto.getPassword()));

    try {
      userService.createOrUpdateUser(u);
    } catch(UserAlreadyExistsException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return Response.ok().build();
  }
}