package org.obiba.opal.web.security;

import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.web.model.Opal;

import com.google.common.collect.Sets;

public class Dtos {

  private Dtos() {}

  public static SubjectCredentials fromDto(Opal.SubjectCredentialsDto dto) {
    SubjectCredentials.Builder builder = SubjectCredentials.Builder.create() //
        .name(dto.getName()) //
        .enabled(dto.getEnabled()) //
        .groups(Sets.newHashSet(dto.getGroupsList()));
    switch(dto.getType()) {
      case USER:
        builder.type(SubjectCredentials.Type.USER);
        break;
      case APPLICATION:
        builder.type(SubjectCredentials.Type.APPLICATION);
        break;
    }
    // dont't copy password or certificate
    return builder.build();
  }

  public static Opal.SubjectCredentialsDto asDto(SubjectCredentials subjectCredentials) {
    Opal.SubjectCredentialsDto.Builder builder = Opal.SubjectCredentialsDto.newBuilder() //
        .setName(subjectCredentials.getName()) //
        .setEnabled(subjectCredentials.isEnabled()) //
        .addAllGroups(subjectCredentials.getGroups());
    switch(subjectCredentials.getType()) {
      case USER:
        builder.setType(Opal.SubjectCredentialsType.USER);
        break;
      case APPLICATION:
        builder.setType(Opal.SubjectCredentialsType.APPLICATION);
        break;
    }
    // dont't copy password or certificate
    return builder.build();
  }

  public static Opal.GroupDto asDto(Group group) {
    return Opal.GroupDto.newBuilder() //
        .setName(group.getName()) //
        .addAllSubjectCredentials(group.getSubjectCredentials()) //
        .build();
  }

}
