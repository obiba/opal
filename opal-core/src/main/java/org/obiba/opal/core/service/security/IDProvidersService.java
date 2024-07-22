/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import org.obiba.oidc.OIDCConfiguration;

import java.io.IOException;
import java.util.Collection;

/**
 * Management of the persisted OpenID Connect configurations.
 */
public interface IDProvidersService {

  /**
   * Get the OIDC configurations living in the application.
   *
   * @return
   */
  Collection<OIDCConfiguration> getConfigurations();

  /**
   * Throws {@link org.obiba.opal.core.service.security.DuplicateIDProviderException} if {@link org.obiba.oidc.OIDCConfiguration} with name exists.
   *
   * @return
   */
  void ensureUniqueConfiguration(String name) throws DuplicateIDProviderException;

  /**
   * Get the OIDC configuration from name.
   *
   * @return
   */
  OIDCConfiguration getConfiguration(String name);

  /**
   * Create or update the provided configuration.
   *
   * @param configuration
   */
  void saveConfiguration(OIDCConfiguration configuration) throws IOException;

  /**
   * Delete the configuration with the given name, ignored if it does not exists.
   *
   * @param name
   */
  void deleteConfiguration(String name);

  /**
   * Enable or disable a configuration.
   *
   * @param name
   * @param enable
   */
  void enableConfiguration(String name, boolean enable) throws IOException;

}
