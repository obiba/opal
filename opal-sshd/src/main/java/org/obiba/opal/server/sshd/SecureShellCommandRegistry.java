/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.sshd;

import org.obiba.opal.shell.AbstractCommandRegistry;
import org.obiba.opal.shell.commands.DecryptCommand;
import org.obiba.opal.shell.commands.ExitCommand;
import org.obiba.opal.shell.commands.ExportCommand;
import org.obiba.opal.shell.commands.HelpCommand;
import org.obiba.opal.shell.commands.ImportCommand;
import org.obiba.opal.shell.commands.KeyCommand;
import org.obiba.opal.shell.commands.ListCommand;
import org.obiba.opal.shell.commands.PublicCommand;
import org.obiba.opal.shell.commands.QuitCommand;
import org.obiba.opal.shell.commands.ShowCommand;
import org.obiba.opal.shell.commands.VersionCommand;
import org.obiba.opal.shell.commands.options.DecryptCommandOptions;
import org.obiba.opal.shell.commands.options.ExitCommandOptions;
import org.obiba.opal.shell.commands.options.ExportCommandOptions;
import org.obiba.opal.shell.commands.options.HelpCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;
import org.obiba.opal.shell.commands.options.ListCommandOptions;
import org.obiba.opal.shell.commands.options.PublicCommandOptions;
import org.obiba.opal.shell.commands.options.QuitCommandOptions;
import org.obiba.opal.shell.commands.options.ShowCommandOptions;
import org.obiba.opal.shell.commands.options.VersionCommandOptions;

/**
 * Available commands for a secure shell (ssh)
 */
public class SecureShellCommandRegistry extends AbstractCommandRegistry {

  public SecureShellCommandRegistry() {
    super();
    addAvailableCommand(HelpCommand.class, HelpCommandOptions.class);
    addAvailableCommand(QuitCommand.class, QuitCommandOptions.class);
    addAvailableCommand(ExitCommand.class, ExitCommandOptions.class);
    addAvailableCommand(ImportCommand.class, ImportCommandOptions.class);
    addAvailableCommand(KeyCommand.class, KeyCommandOptions.class);
    addAvailableCommand(PublicCommand.class, PublicCommandOptions.class);
    addAvailableCommand(DecryptCommand.class, DecryptCommandOptions.class);
    addAvailableCommand(VersionCommand.class, VersionCommandOptions.class);
    addAvailableCommand(ShowCommand.class, ShowCommandOptions.class);
    addAvailableCommand(ExportCommand.class, ExportCommandOptions.class);
    addAvailableCommand(ListCommand.class, ListCommandOptions.class);
  }

}