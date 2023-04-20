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

import org.obiba.opal.spi.r.RASyncOperationTemplate;

import java.io.File;
import java.util.Date;

public interface RServerSession extends RASyncOperationTemplate {

  String DEFAULT_CONTEXT = "default";

  String getId();

  void touch();

  String getUser();

  Date getCreated();

  Date getTimestamp();

  boolean isBusy();

  /**
   * Cumulated execution time.
   *
   * @return
   */
  long getTotalExecutionTimeMillis();

  /**
   * When busy, get commands execution time otherwise returns 0.
   *
   * @return
   */
  long getCurrentExecutionTimeMillis();

  void setExecutionContext(String executionContext);

  String getExecutionContext();

  void setProfile(RServerProfile profile);

  RServerProfile getProfile();

  String getRServerServiceName();

  /**
   * Check if the R session is not busy and has expired.
   *
   * @param timeout in minutes
   * @return
   */
  boolean hasExpired(long timeout);

  void close();

  boolean isClosed();

  /**
   * Get the {@link File} directory specific to this user's R session. Create it if it does not exist.
   *
   * @param saveId
   * @return
   */
  File getWorkspace(String saveId);

  void saveRSessionFiles(String saveId);
}
