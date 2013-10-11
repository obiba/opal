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
  public Response createUser(Opal.UserDto dto) {
    User user = fromDto(dto);
    user.setPassword(User.digest(dto.getPassword()));
    userService.createOrUpdateUser(user);
    return Response.ok().build();
  }
}