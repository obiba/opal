/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.web.model.Opal;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Map;

import static org.obiba.opal.web.model.Opal.BookmarkDto.ResourceType;

public class Dtos {

  private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private Dtos() {
  }

  public static SubjectCredentials fromDto(Opal.SubjectCredentialsDto dto) {
    SubjectCredentials.Builder builder = SubjectCredentials.Builder.create() //
        .name(dto.getName()) //
        .enabled(dto.getEnabled()) //
        .groups(Sets.newHashSet(dto.getGroupsList()));
    switch (dto.getAuthenticationType()) {
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
    switch (subjectCredentials.getAuthenticationType()) {
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
    if (nbFragments >= 2) {
      builder.addLinks(Opal.LinkDto.newBuilder().setRel(toUri(fragments[0], fragments[1])).setLink(fragments[1]));
      builder.setType(ResourceType.PROJECT);
    }
    if (nbFragments >= 4) {
      builder.addLinks(Opal.LinkDto.newBuilder().setRel(toUri(fragments[0], fragments[1], fragments[2], fragments[3]))
          .setLink(fragments[3]));
      builder.setType(ResourceType.TABLE);
    }
    if (nbFragments == 6) {
      builder.addLinks(Opal.LinkDto.newBuilder().setRel(bookmark.getResource()).setLink(fragments[5]));
      builder.setType(ResourceType.VARIABLE);
    }
    return builder.build();
  }

  public static Opal.IDProviderDto asDto(OIDCConfiguration configuration) {
    Opal.IDProviderDto.Builder builder = Opal.IDProviderDto.newBuilder();
    builder.setName(configuration.getName());
    builder.setEnabled(false);
    for (Map.Entry<String, String> entry : configuration.getCustomParams().entrySet()) {
      if ("label".equals(entry.getKey()))
        builder.setLabel(entry.getValue());
      else if ("providerUrl".equals(entry.getKey()))
        builder.setProviderUrl(entry.getValue());
      else if ("groups".equals(entry.getKey()))
        builder.setGroups(entry.getValue());
      else if ("enabled".equals(entry.getKey())) {
        try {
          builder.setEnabled(Boolean.parseBoolean(entry.getValue()));
        } catch (Exception e) {
          // ignore
        }
      } else
        builder.addParameters(Opal.ParameterDto.newBuilder().setKey(entry.getKey()).setValue(entry.getValue()));
    }
    builder.setClientId(configuration.getClientId());
    builder.setSecret(configuration.getSecret());
    builder.setDiscoveryURI(configuration.getDiscoveryURI());
    builder.setScope(configuration.getScope());
    builder.setUseNonce(configuration.isUseNonce());
    builder.setConnectTimeout(configuration.getConnectTimeout());
    builder.setReadTimeout(configuration.getReadTimeout());
    return builder.build();
  }

  public static OIDCConfiguration fromDto(Opal.IDProviderDto dto) {
    OIDCConfiguration configuration = new OIDCConfiguration(dto.getName());
    configuration.setClientId(dto.getClientId());
    configuration.setSecret(dto.getSecret());
    configuration.setDiscoveryURI(dto.getDiscoveryURI());
    configuration.setScope(dto.getScope());
    configuration.setUseNonce(dto.getUseNonce());
    if (dto.hasConnectTimeout()) {
      configuration.setConnectTimeout(dto.getConnectTimeout());
    }
    if (dto.hasReadTimeout()) {
      configuration.setReadTimeout(dto.getReadTimeout());
    }
    if (dto.hasGroups()) {
      configuration.getCustomParams().put("groups", dto.getGroups());
    }
    if (dto.hasLabel()) {
      configuration.getCustomParams().put("label", dto.getLabel());
    }
    if (dto.hasProviderUrl()) {
      configuration.getCustomParams().put("providerUrl", dto.getProviderUrl());
    }
    configuration.getCustomParams().put("enabled", "" + dto.getEnabled());
    if (dto.getParametersCount()>0) {
      dto.getParametersList().forEach(parameterDto -> configuration.getCustomParams().put(parameterDto.getKey(), parameterDto.getValue()));
    }
    return configuration;
  }

  public static Opal.AuthProviderDto asSummaryDto(OIDCConfiguration configuration) {
    Opal.AuthProviderDto.Builder builder = Opal.AuthProviderDto.newBuilder();
    builder.setName(configuration.getName());
    String label = configuration.getCustomParam("label");
    if (!Strings.isNullOrEmpty(label)) {
      builder.setLabel(label);
    }
    String providerUrl = configuration.getCustomParam("providerUrl");
    if (!Strings.isNullOrEmpty(providerUrl)) {
      builder.setProviderUrl(providerUrl);
    }
    return builder.build();
  }

  private static String toUri(String... fragments) {
    StringBuilder sb = new StringBuilder();
    for (String fragment : fragments) {
      sb.append("/").append(fragment);
    }
    return sb.toString();
  }
}
