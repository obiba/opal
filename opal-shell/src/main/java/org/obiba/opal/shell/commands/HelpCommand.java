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

import org.obiba.opal.shell.commands.options.HelpCommandOptions;

/**
 * Help command.
 * 
 * Note: This command provides help on the client itself (basic syntax), not context-sensitive help on specific
 * commands. The latter should be provided by each command's "--help" option.
 */
@CommandUsage(description = "Displays the list of available commands for Opal.")
public class HelpCommand extends AbstractCommand<HelpCommandOptions> {
  //
  // AbstractCommand Methods
  //

  public int execute() {
    getShell().printUsage();
    return 0; // success;
  }
}
