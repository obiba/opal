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
