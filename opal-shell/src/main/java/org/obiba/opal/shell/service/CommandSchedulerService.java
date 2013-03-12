/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.service;

import org.obiba.opal.shell.commands.Command;

/**
 * Service for scheduling execution of a {@link Command}.
 */
public interface CommandSchedulerService {

  /**
   * Registers a command with the service. Note that calling this method does not execute the command nor does it
   * schedule it for later execution.
   *
   * @param name the command's name
   * @param group the command's group
   * @param command the command
   */
  void addCommand(String name, String group, Command<?> command);

  /**
   * Unregisters a previously registered command. Once delete, the command may no longer be referenced by methods of
   * this service.
   *
   * @param name the command's name
   * @param group the command's group
   * @throws CommandSchedulerServiceException on any type of scheduler exception
   */
  void deleteCommand(String name, String group);

  /**
   * Schedules a previously added command. The command is referenced by its group and name.
   *
   * @param name the command's name
   * @param group the command's group
   * @param cronExpression the command's execution schedule, a cron expression (for details, see
   * {@link org.quartz.CronTrigger})
   * @throws CommandSchedulerServiceException on any type of scheduler exception
   */
  void scheduleCommand(String name, String group, String cronExpression);

  /**
   * Unschedules a command. Note that calling this method does not delete the command; it may still be referenced by
   * methods of this service.
   *
   * @param name the command's name
   * @param group the command's group
   * @throws CommandSchedulerServiceException on any type of scheduler exception
   */
  void unscheduleCommand(String name, String group);

  /**
   * Returns the command's schedule (a cron expression), assuming the command has been scheduled.
   *
   * @param name the command's name
   * @param group the command's group
   * @return the command's schedule (or <code>null</code> if not scheduled)
   * @throws CommandSchedulerServiceException on any type of scheduler exception
   */
  String getCommandSchedule(String name, String group);

}
