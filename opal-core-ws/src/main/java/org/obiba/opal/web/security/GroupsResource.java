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

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.impl.UserService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@SuppressWarnings("UnusedDeclaration")
@Component
@Scope("request")
@Path("/groups")
public class GroupsResource extends AbstractUserGroupResource {

  @Autowired
  public GroupsResource(UserService userService) {
    super(userService);
  }

  @GET
  public List<Opal.GroupDto> getGroups() {

    List<Group> groups = userService.getGroups();

    List<Opal.GroupDto> groupDtos = Lists.newArrayList();

    for(Group g : groups) {
      groupDtos.add(toDto(g));
    }

    return groupDtos;
  }

  @POST
  public Response createGroup(Opal.GroupDto groupDto) {

    Group g = new Group();
    g.setName(groupDto.getName());

    if(userService.getGroupWithName(groupDto.getName()) != null) {
      return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    userService.createGroup(g);
    return Response.ok().build();
  }

  private Opal.GroupDto toDto(Group group) {

    List<String> users = Lists.newArrayList();
    if(group.getUsers() != null) {
      for(User u : group.getUsers()) {
        users.add(u.getName());
      }
    }

    return Opal.GroupDto.newBuilder().setName(group.getName()).addAllUsers(users).build();
  }

}