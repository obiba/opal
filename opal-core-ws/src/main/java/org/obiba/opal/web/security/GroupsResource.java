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
import org.obiba.opal.web.model.Opal;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/groups")
public class GroupsResource extends AbstractUserGroupResource {

  @GET
  public List<Opal.GroupDto> getGroups() {
    Iterable<Group> groups = userService.getGroups();
    List<Opal.GroupDto> groupDtos = Lists.newArrayList();
    for(Group group : groups) {
      groupDtos.add(toDto(group));
    }
    return groupDtos;
  }

  @POST
  public Response createGroup(Opal.GroupDto groupDto) {

    if(userService.getGroupWithName(groupDto.getName()) != null) {
      return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    Group group = new Group();
    group.setName(groupDto.getName());
    userService.createOrUpdateGroup(group);

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