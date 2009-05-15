/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.impl;

import org.obiba.opal.cli.client.AbstractCliClient;
import org.obiba.opal.cli.client.command.DecryptCommand;
import org.obiba.opal.cli.client.command.HelpCommand;
import org.obiba.opal.cli.client.command.VersionCommand;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;

/**
 * OpalKey CLI client.
 */
public class OpalKeyClient extends AbstractCliClient {
  //
  // Constants
  //

  private static final String CLIENT_NAME = "opalkey";

  //
  // AbstractCliClient Methods
  //

  @Override
  public String getName() {
    return CLIENT_NAME;
  }

  protected void initAvailableCommands() {
    addAvailableCommand(HelpCommand.class);
    addAvailableCommand(VersionCommand.class);
    addAvailableCommand(DecryptCommand.class);
  }

  //
  // Methods
  //

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    OpalKeyClient client = new OpalKeyClient();

    try {
      client.setCommand(args);
      client.executeCommand();
    } catch(IllegalArgumentException ex) {
      client.printUsage();
    } catch(ArgumentValidationException ex) {
      System.err.println(ex.getMessage());
    }
  }
}