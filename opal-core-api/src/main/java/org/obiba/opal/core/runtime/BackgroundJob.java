/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime;

/**
 * A generic background job that will be run at opal startup.
 */
public interface BackgroundJob extends Runnable {

  /**
   * Name identifying the job.
   * @return
   */
  String getName();

  /**
   * description of the job.
   * @return
   */
  String getDescription();

  /**
   * Get the thread priority for this job.
   * @return
   */
  int getPriority();

  /**
   * Percentage of the progress of the job.
   * @return
   */
  int getProgress();

  /**
   * Description of the current state of the job.
   * @return
   */
  String getProgressStatus();

}
