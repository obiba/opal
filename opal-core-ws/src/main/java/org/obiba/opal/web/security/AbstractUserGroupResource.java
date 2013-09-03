package org.obiba.opal.web.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.impl.UserService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

abstract class AbstractUserGroupResource {

  protected final UserService userService;

  @Autowired
  AbstractUserGroupResource(UserService userService) {
    this.userService = userService;
  }

  protected User fromDto(Opal.UserDto dto) {
    User user = userService.getUserWithName(dto.getName());

    if(user == null) {
      user = new User(dto.getName());
    }

    user.setEnabled(dto.getEnabled());

    // Remove groups that are not in userDto
    Collection<Group> toRemove = new HashSet<Group>();
    for(Group g : user.getGroups()) {
      if(!dto.getGroupsList().contains(g.getName())) {
        toRemove.add(g);
      }
    }
    for(Group g : toRemove) {
      user.removeGroup(g);
    }

    // Groups fetch or create
    for(String groupName : dto.getGroupsList()) {
      Group group = userService.getGroupWithName(groupName);
      if(group == null) {
        group = new Group(groupName);
        userService.createGroup(group);
      }

      user.addGroup(group);
    }

    return user;
  }

  protected Opal.UserDto asDto(User user) {

    List<String> groups = Lists.newArrayList();
    if(user.getGroups() != null) {
      for(Group g : user.getGroups()) {
        groups.add(g.getName());
      }
    }

    return Opal.UserDto.newBuilder().setName(user.getName()).setEnabled(user.getEnabled()).addAllGroups(groups).build();
  }

  protected Opal.GroupDto asDto(Group group) {

    List<String> users = Lists.newArrayList();
    if(group.getUsers() != null) {
      for(User u : group.getUsers()) {
        users.add(u.getName());
      }
    }

    return Opal.GroupDto.newBuilder().setName(group.getName()).addAllUsers(users).build();
  }
}
