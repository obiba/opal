/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import com.google.common.base.Strings;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.opal.core.service.security.IDProvidersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Get only the "enabled" OpenID Connect configurations.
 */
@Component
public class AuthConfigurationProvider implements OIDCConfigurationProvider {

  private final IDProvidersService idProvidersService;

  @Autowired
  public AuthConfigurationProvider(IDProvidersService idProvidersService) {
    this.idProvidersService = idProvidersService;
  }

  @Override
  public Collection<OIDCConfiguration> getConfigurations() {
    return idProvidersService.getConfigurations().stream()
        .filter(conf -> {
          try {
            return Boolean.parseBoolean(conf.getCustomParam("enabled"));
          } catch (Exception e) {
            return false;
          }
        })
        .collect(Collectors.toList());
  }

  @Override
  public OIDCConfiguration getConfiguration(String name) {
    if (Strings.isNullOrEmpty(name)) return null;
    return getConfigurations().stream().filter(conf -> name.equals(conf.getName())).findFirst().orElse(null);
  }
}
