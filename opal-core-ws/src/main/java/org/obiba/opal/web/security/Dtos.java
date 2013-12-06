package org.obiba.opal.web.security;

import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.SubjectCredentials;
import org.obiba.opal.web.model.Opal;

import com.google.common.collect.Sets;

public class Dtos {

  private Dtos() {}

  public static SubjectCredentials fromDto(Opal.UserDto dto) {
    return SubjectCredentials.Builder.create() //
        .name(dto.getName()) //
        .enabled(dto.getEnabled()) //
        .groups(Sets.newHashSet(dto.getGroupsList())) //
        .build();
  }

  public static Opal.UserDto asDto(SubjectCredentials subjectCredentials) {
    return Opal.UserDto.newBuilder() //
        .setName(subjectCredentials.getName()) //
        .setEnabled(subjectCredentials.isEnabled()) //
        .addAllGroups(subjectCredentials.getGroups()) //
        .build();
  }

  public static Opal.GroupDto asDto(Group group) {
    return Opal.GroupDto.newBuilder() //
        .setName(group.getName()) //
        .addAllUsers(group.getUsers()) //
        .build();
  }

}
