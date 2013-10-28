package org.obiba.opal.web.security;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.web.model.Opal;

import com.google.common.collect.Sets;

public class Dtos {

  private Dtos() {}

  public static User fromDto(Opal.UserDto dto) {
    return User.Builder.create() //
        .name(dto.getName()) //
        .enabled(dto.getEnabled()) //
        .groups(Sets.newHashSet(dto.getGroupsList())) //
        .build();
  }

  public static Opal.UserDto asDto(User user) {
    return Opal.UserDto.newBuilder() //
        .setName(user.getName()) //
        .setEnabled(user.getEnabled()) //
        .addAllGroups(user.getGroups()) //
        .build();
  }

  public static Opal.GroupDto asDto(Group group) {
    return Opal.GroupDto.newBuilder() //
        .setName(group.getName()) //
        .addAllUsers(group.getUsers()) //
        .build();
  }

}
