/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime;

/**
 * A generic background task that will be run at opal startup.
 */
public interface BackgroundJob extends Runnable {

  /**
   * Name identifying the task.
   *
   * @return
   */
  String getName();

  /**
   * description of the task.
   *
   * @return
   */
  String getDescription();

  /**
   * Get the thread priority for this task.
   *
   * @return
   */
  int getPriority();

  /**
   * Percentage of the progress of the task.
   *
   * @return
   */
  int getProgress();

  /**
   * Description of the current state of the task.
   *
   * @return
   */
  String getProgressStatus();

}
