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

import java.util.List;

import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.shell.CommandJob;

/**
 * Service for {@link CommandJob} operations.
 */
public interface CommandJobService extends Service {

  /**
   * Assigns an id to a command job and submits it for asynchronous execution.
   *
   * @param commandJob the submitted command job
   * @param owner the command job's owner
   * @return the command job's id
   */
  Integer launchCommand(CommandJob commandJob, Subject owner);

  /**
   * Assigns an id to a command job and submits it for asynchronous execution.
   * <p/>
   * The command job's owner is set to the current user.
   *
   * @param commandJob the submitted command job
   * @return the command job's id
   */
  Integer launchCommand(CommandJob commandJob);

  /**
   * Returns the specified command job.
   *
   * @param id command job id
   * @return the command job (<code>null</code> if none)
   */
  CommandJob getCommand(Integer id);

  /**
   * Returns the history of launched commands.
   *
   * @return history of launched commands
   */
  List<CommandJob> getHistory();

  /**
   * Cancels the specified command job.
   *
   * @param id command job id
   * @throws NoSuchCommandJobException if the specified command job does not exist
   * @throws IllegalStateException if the command job is not in a "cancellable" state (either NOT_STARTED or
   * IN_PROGRESS) and therefore cannot be cancelled
   */
  void cancelCommand(Integer id) throws NoSuchCommandJobException, IllegalStateException;

  /**
   * Deletes the specified command job.
   *
   * @param id command job id
   * @throws NoSuchCommandJobException if the specified command job does not exist
   * @throws IllegalStateException if the command job is in a "running" state (IN_PROGRESS or CANCEL_PENDING) and
   * therefore cannot be deleted
   */
  void deleteCommand(Integer id) throws NoSuchCommandJobException, IllegalStateException;

  /**
   * Deletes all completed commands (i.e., commands in the SUCCEEDED, FAILED or CANCELED state).
   */
  void deleteCompletedCommands();

}
