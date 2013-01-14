/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import org.obiba.opal.shell.OpalShell;

/**
 * Interface for command classes.
 */
public interface Command<T> {

  String getName();

  void setShell(OpalShell shell);

  /**
   * Returns the command's options.
   *
   * @return options
   */
  T getOptions();

  /**
   * Sets the command's options.
   *
   * @param options options
   */
  void setOptions(T options);

  /**
   * Executes the command.
   * <p/>
   * Note that commands must not contain any business logic. All business logic should reside in a service method called
   * by the command to perform its work.
   *
   * @return error code (<code>0</code> on success, non-zero on any type of error)
   * @throws IllegalStateException if options are required but have not been set
   * @throws IllegalArgumentException if options invalid (mutually inconsistent)
   */
  int execute();

}
