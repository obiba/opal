package org.obiba.opal.web.security;

import java.text.SimpleDateFormat;

import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.web.model.Opal;

import com.google.common.collect.Sets;

public class Dtos {

  private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

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

  public static Opal.SubjectProfileDto asDto(SubjectProfile profile) {
    return Opal.SubjectProfileDto.newBuilder() //
        .setPrincipal(profile.getPrincipal()) //
        .setRealm(profile.getRealm()) //
        .setCreated(ISO_8601.format(profile.getCreated())) //
        .setLastUpdate(ISO_8601.format(profile.getUpdated())) //
        .build();
  }

  public static Opal.BookmarkDto asDto(Bookmark bookmark) {
    return Opal.BookmarkDto.newBuilder() //
        .setResource(bookmark.getResource()) //
        .setCreated(ISO_8601.format(bookmark.getCreated())) //
        .build();
  }

}
