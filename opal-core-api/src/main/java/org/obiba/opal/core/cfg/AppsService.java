/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import org.obiba.opal.core.domain.AppsConfig;
import org.obiba.opal.core.domain.RockAppConfig;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.service.SystemService;

import java.util.List;

public interface AppsService extends SystemService {

  /**
   * Register an app. Send an event to inform app users.
   *
   * @param app
   */
  void registerApp(App app);

  /**
   * Unregister an app. Send an event to inform app users.
   *
   * @param app
   */
  void unregisterApp(App app);

  /**
   * Get the apps registered in the system.
   *
   * @return
   */
  List<App> getApps();

  /**
   * Get apps of a type registered in the system.
   *
   * @param type
   * @return
   */
  List<App> getApps(String type);

  /**
   * Get the app from id.
   *
   * @param id
   * @return
   */
  App getApp(String id);

  /**
   * Get if it has the app from id.
   *
   * @param id
   * @return
   */
  boolean hasApp(String id);

  /**
   * Check self-registration token.
   *
   * @param value
   */
  void checkToken(String value);

  /**
   * Check self-registrating server passes black and white lists.
   *
   * @param server
   */
  void checkSelfRegistrationRules(String server);

  /**
   * Get the apps management settings.
   *
   * @return
   */
  AppsConfig getAppsConfig();

  /**
   * Update the apps management settings.
   *
   * @param config
   */
  void updateAppsConfig(AppsConfig config);

  /**
   * Reset to default configuration.
   */
  void resetConfig();

  /**
   * Get the configuration corresponding to the Rock app.
   *
   * @param app
   */
  RockAppConfig getRockAppConfig(App app);

}
