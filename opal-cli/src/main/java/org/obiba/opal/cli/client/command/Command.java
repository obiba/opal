/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import org.obiba.opal.cli.client.CliClient;

/**
 * Interface for command classes.
 */
public interface Command<T> {

  /**
   * Sets the client executing this command.
   * 
   * Some commands may require this reference.
   * 
   * @param client client executing the command
   */
  public void setClient(CliClient client);

  /**
   * Returns the client executing this command.
   * 
   * @return client
   */
  public CliClient getClient();

  /**
   * Sets the command's options.
   * 
   * @param options options
   */
  public void setOptions(T options);

  /**
   * Executes the command.
   * 
   * Note that commands must not contain any business logic. All business logic should reside in a service method called
   * by the command to perform its work.
   * 
   * @throws IllegalStateException if options are required but have not been set
   * @throws IllegalArgumentException if options invalid (mutually inconsistent)
   */
  public void execute();
}
