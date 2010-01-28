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

  public void quit();

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

}
