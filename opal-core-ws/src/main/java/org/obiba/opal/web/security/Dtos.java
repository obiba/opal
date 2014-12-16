package org.obiba.opal.web.security;

import java.text.SimpleDateFormat;

import org.obiba.opal.core.domain.security.*;
import org.obiba.opal.web.model.Opal;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;

import static org.obiba.opal.web.model.Opal.BookmarkDto.ResourceType;

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
    Opal.BookmarkDto.Builder builder = Opal.BookmarkDto.newBuilder() //
        .setResource(bookmark.getResource()) //
        .setCreated(ISO_8601.format(bookmark.getCreated()));

    String[] fragments = StringUtils.tokenizeToStringArray(bookmark.getResource(), "/");
    int nbFragments = fragments.length;
    if(nbFragments >= 2) {
      builder.addLinks(Opal.LinkDto.newBuilder().setRel(toUri(fragments[0], fragments[1])).setLink(fragments[1]));
      builder.setType(ResourceType.PROJECT);
    }
    if(nbFragments >= 4) {
      builder.addLinks(Opal.LinkDto.newBuilder().setRel(toUri(fragments[0], fragments[1], fragments[2], fragments[3]))
          .setLink(fragments[3]));
      builder.setType(ResourceType.TABLE);
    }
    if(nbFragments == 6) {
      builder.addLinks(Opal.LinkDto.newBuilder().setRel(bookmark.getResource()).setLink(fragments[5]));
      builder.setType(ResourceType.VARIABLE);
    }
    return builder.build();
  }

    public static Opal.AuthClientDto asDto(String name, String redirectUrl) {
        Opal.AuthClientDto.Builder builder = Opal.AuthClientDto.newBuilder();
        builder.setName(name);
        if (redirectUrl != null) {
            builder.setRedirectUrl(redirectUrl);
        }
        return builder.build();
    }

  private static String toUri(String... fragments) {
    StringBuilder sb = new StringBuilder();
    for(String fragment : fragments) {
      sb.append("/").append(fragment);
    }
    return sb.toString();
  }
}
