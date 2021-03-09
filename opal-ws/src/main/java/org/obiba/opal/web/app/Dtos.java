/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.app;

import com.google.common.base.Strings;
import org.obiba.opal.core.domain.AppConfig;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.domain.AppsConfig;
import org.obiba.opal.core.domain.RockAppConfig;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.web.model.Apps;

import java.util.stream.Collectors;

public class Dtos {

  public static Apps.AppDto asDto(App app) {
    Apps.AppDto.Builder builder = Apps.AppDto.newBuilder()
        .setId(app.getId())
        .setName(Strings.isNullOrEmpty(app.getName()) ? app.getId() : app.getName())
        .setType(app.getType());

    if (app.hasCluster())
        builder.setCluster(app.getCluster());

    builder.addAllTags(app.getTags());

    if (app.hasServer())
        builder.setServer(app.getServer());
    return builder.build();
  }

  public static Apps.AppsConfigDto asDto(AppsConfig config) {
    return Apps.AppsConfigDto.newBuilder()
        .setToken(config.getToken())
        .addAllRockConfigs(config.getRockAppConfigs().stream()
            .map(Dtos::asDto)
            .collect(Collectors.toList()))
        .build();
  }

  public static Apps.RockAppConfigDto asDto(RockAppConfig config) {
    Apps.RockAppConfigDto.Builder builder = Apps.RockAppConfigDto.newBuilder().setHost(config.getHost());
    if (config.hasAdministratorCredentials())
      builder.setAdministratorCredentials(asDto(config.getAdministratorCredentials()));
    if (config.hasManagerCredentials())
      builder.setManagerCredentials(asDto(config.getManagerCredentials()));
    if (config.hasUserCredentials())
      builder.setUserCredentials(asDto(config.getUserCredentials()));
    return builder.build();
  }

  public static Apps.AppCredentialsDto asDto(AppCredentials credentials) {
    return Apps.AppCredentialsDto.newBuilder()
        .setName(credentials.getUser())
        .setPassword(credentials.getPassword())
        .build();
  }

  public static App fromDto(Apps.AppDto dto) {
    App app = new App();
    if (dto.hasId()) app.setId(dto.getId());
    app.setName(dto.getName());
    app.setType(dto.getType());
    if (dto.hasCluster()) app.setCluster(dto.getCluster());
    if (dto.hasServer()) app.setServer(dto.getServer());
    if (dto.getTagsCount()>0) app.setTags(dto.getTagsList());
    return app;
  }

  public static AppsConfig fromDto(Apps.AppsConfigDto dto) {
    AppsConfig config = new AppsConfig();
    config.setToken(dto.getToken());
    config.setRockAppConfigs(dto.getRockConfigsList().stream()
        .map(Dtos::fromDto)
        .collect(Collectors.toList()));
    return config;
  }

  public static RockAppConfig fromDto(Apps.RockAppConfigDto dto) {
    RockAppConfig config = new RockAppConfig(dto.getHost());
    if (dto.hasAdministratorCredentials())
      config.setAdministratorCredentials(fromDto(dto.getAdministratorCredentials()));
    if (dto.hasManagerCredentials())
      config.setManagerCredentials(fromDto(dto.getManagerCredentials()));
    if (dto.hasUserCredentials())
      config.setUserCredentials(fromDto(dto.getUserCredentials()));
    return config;
  }

  public static AppCredentials fromDto(Apps.AppCredentialsDto dto) {
    return new AppCredentials(dto.getName(), dto.getPassword());
  }
}
