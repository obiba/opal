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
    switch(dto.getAuthenticationType()) {
      case PASSWORD:
        builder.authenticationType(SubjectCredentials.AuthenticationType.PASSWORD);
        break;
      case CERTIFICATE:
        builder.authenticationType(SubjectCredentials.AuthenticationType.CERTIFICATE);
        break;
    }
    // don't copy password or certificate
    return builder.build();
  }

  public static Opal.SubjectCredentialsDto asDto(SubjectCredentials subjectCredentials) {
    Opal.SubjectCredentialsDto.Builder builder = Opal.SubjectCredentialsDto.newBuilder() //
        .setName(subjectCredentials.getName()) //
        .setEnabled(subjectCredentials.isEnabled()) //
        .addAllGroups(subjectCredentials.getGroups());
    switch(subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        builder.setAuthenticationType(Opal.SubjectCredentialsDto.AuthenticationType.PASSWORD);
        break;
      case CERTIFICATE:
        builder.setAuthenticationType(Opal.SubjectCredentialsDto.AuthenticationType.CERTIFICATE);
        break;
    }
    // don't copy password or certificate
    return builder.build();
  }

  public static Opal.GroupDto asDto(Group group) {
    return Opal.GroupDto.newBuilder() //
        .setName(group.getName()) //
        .addAllSubjectCredentials(group.getSubjectCredentials()) //
        .build();
  }

}
