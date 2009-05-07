/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client;

import java.util.List;

import org.obiba.opal.cli.client.command.Command;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;

/**
 * Interface for CLI clients.
 */
public interface CliClient {

  /**
   * Returns the name of the client.
   * 
   * @return name of client
   */
  public String getName();

  /**
   * Prints usage information for the client.
   */
  public void printUsage();

  /**
   * Returns a list of commands available.
   * 
   * @return list of commands available (the commands are listed by name as indicated by the
   * <code>CommandLineInterface</code> annotation in the associated options interface)
   */
  public List<String> availableCommands();

  /**
   * Sets the command to be executed by the client.
   * 
   * @param cmdline command line
   * @throws IllegalArgumentException if the command line specifies an invalid command
   * @throws ArgumentValidationException if the command line specifies invalid command options
   */
  public <T> void setCommand(String[] cmdline) throws IllegalArgumentException, ArgumentValidationException;

  /**
   * Sets the command to be executed by the client.
   * 
   * @param command command
   */
  public <T> void setCommand(Command<T> command);

  /**
   * Executes the current command (i.e., the command last set with the <code>setCommand</code> method).
   * 
   * @throws IllegalStateException if no command has been set
   */
  public void executeCommand();
}
