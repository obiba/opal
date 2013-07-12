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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.service.impl.UserService;
import org.obiba.opal.core.user.Group;
import org.obiba.opal.core.user.User;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@SuppressWarnings("UnusedDeclaration")
@Component
@Scope("request")
@Path("/")
public class UserResource {

  private final UserService userService;

  @Autowired
  public UserResource(UserService userService) {
    this.userService = userService;
  }

  @GET
  @Path("/users")
  public List<Opal.UserDto> getUsers() {

    List<User> users = userService.getUsers();

    List<Opal.UserDto> userDtos = Lists.newArrayList();

    if(users != null) {
      for(User u : users) {
        userDtos.add(toDto(u));
      }
    }
    return userDtos;
  }

  @POST
  @Path("/users")
  public Response createUser(Opal.UserDto userDto) {

    if(userService.getUserWithName(userDto.getName()) != null) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if(!userDto.hasPassword()) {
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    User u = new User();
    u.setName(userDto.getName());
    u.setEnabled(userDto.getEnabled());
    u.setPassword(User.digest(userDto.getPassword()));
    for(String g : userDto.getGroupsList()) {
      Group group = userService.getGroupWithName(g);

      if(group == null) {
        // Create group
        Group newGroup = new Group();
        newGroup.setName(g);
        userService.createGroup(newGroup);

        group = newGroup;
      }

      u.addGroup(group);
    }

    userService.createOrUpdateUser(u);
    return Response.ok().build();
  }

  @GET
  @Path("/user/{name}")
  public Response getUser(@PathParam("name") String name) {
    User user = userService.getUserWithName(name);

    if(user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok().entity(toDto(user)).build();

  }

  @PUT
  @Path("/user/{name}")
  public Response updateUser(@PathParam("name") String name, Opal.UserDto userDto) {

    // Do not allow to change username
    if(!name.equals(userDto.getName())) {
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    User u = userService.getUserWithName(name);
    if(u.getGroups() != null) {
      u.clearGroups();
    }

    u.setEnabled(userDto.getEnabled());

    if(userDto.hasPassword()) {
      u.setPassword(User.digest(userDto.getPassword()));
    }

    for(String g : userDto.getGroupsList()) {
      Group group = userService.getGroupWithName(g);

      if(group == null) {
        // Create group
        Group newGroup = new Group();
        newGroup.setName(g);
        userService.createGroup(newGroup);
        group = newGroup;
      }

      u.addGroup(group);
    }

    userService.createOrUpdateUser(u);
    return Response.ok().build();
  }

  @DELETE
  @Path("/user/{name}")
  public Response deleteUser(@PathParam("name") String name) {

    User u = userService.getUserWithName(name);

    // Delete user
    if(u != null) {
      userService.deleteUser(u);
    }

    return Response.ok().build();
  }

  private Opal.UserDto toDto(User user) {

    List<String> groups = Lists.newArrayList();
    if(user.getGroups() != null) {
      for(Group g : user.getGroups()) {
        groups.add(g.getName());
      }
    }

    return Opal.UserDto.newBuilder().setName(user.getName()).setEnabled(user.getEnabled()).addAllGroups(groups).build();
  }
}