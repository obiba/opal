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
import org.obiba.opal.cli.client.command.ExtractCommand;
import org.obiba.opal.cli.client.command.HelpCommand;
import org.obiba.opal.cli.client.command.ImportCommand;
import org.obiba.opal.cli.client.command.UpgradeCommand;
import org.obiba.opal.cli.client.command.VersionCommand;
import org.obiba.opal.cli.client.command.options.ExtractCommandOptions;
import org.obiba.opal.cli.client.command.options.HelpCommandOptions;
import org.obiba.opal.cli.client.command.options.ImportCommandOptions;
import org.obiba.opal.cli.client.command.options.UpgradeCommandOptions;
import org.obiba.opal.cli.client.command.options.VersionCommandOptions;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;

/**
 * Opal CLI client.
 */
public class OpalClient extends AbstractCliClient {
  //
  // Constants
  //

  private static final String CLIENT_NAME = "opal";

  //
  // AbstractCliClient Methods
  //

  @Override
  public String getName() {
    return CLIENT_NAME;
  }

  protected void initAvailableCommands() {
    addAvailableCommand(HelpCommand.class, HelpCommandOptions.class);
    addAvailableCommand(VersionCommand.class, VersionCommandOptions.class);
    addAvailableCommand(ImportCommand.class, ImportCommandOptions.class);
    addAvailableCommand(ExtractCommand.class, ExtractCommandOptions.class);
    addAvailableCommand(UpgradeCommand.class, UpgradeCommandOptions.class);
    // addAvailableCommand(ReportCommand.class, ReportCommandOptions.class);
  }

  //
  // Methods
  //

  public static void main(String[] args) {
    OpalClient client = new OpalClient();

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