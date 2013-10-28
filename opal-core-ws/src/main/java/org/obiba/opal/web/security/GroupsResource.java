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
import org.obiba.opal.core.service.impl.UserService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/groups")
public class GroupsResource {

  @Autowired
  private UserService userService;

  @GET
  public List<Opal.GroupDto> getGroups() {
    return Lists.newArrayList(Iterables.transform(userService.listGroups(), new Function<Group, Opal.GroupDto>() {
      @Override
      public Opal.GroupDto apply(Group group) {
        return Dtos.asDto(group);
      }
    }));
  }

  @POST
  public Response createGroup(Opal.GroupDto groupDto) {

    if(userService.getGroup(groupDto.getName()) != null) {
      return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    Group group = new Group();
    group.setName(groupDto.getName());
    userService.save(group);

    return Response.ok().build();
  }

}