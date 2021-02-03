/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import org.obiba.opal.core.runtime.App;

import java.util.List;

/**
 * Management of the cluster inner R services.
 */
public interface RServerClusterService extends RServerService {

  /**
   * Get the Apps associated to the cluster items.
   *
   * @return
   */
  List<App> getApps();

  /**
   * Start the R server associated to the App.
   *
   * @param app
   */
  void start(App app);

  /**
   * Stop the R server associated to the App.
   *
   * @param app
   */
  void stop(App app);

  /**
   * Check that the R server associated to the App is running.
   *
   * @param app
   * @return
   */
  boolean isRunning(App app);

  /**
   * Get the state of the R server associated to the App.
   *
   * @param app
   * @return
   */
  RServerState getState(App app);

  /**
   * Check for an empty cluster of R servers.
   *
   * @return
   */
  default boolean isEmpty() {
    return getApps().isEmpty();
  }

}
