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

import org.obiba.opal.datashield.ListenCommand;
import org.obiba.opal.datashield.ListenCommandOptions;
import org.obiba.opal.shell.AbstractCommandRegistry;
import org.obiba.opal.shell.commands.CopyCommand;
import org.obiba.opal.shell.commands.DecryptCommand;
import org.obiba.opal.shell.commands.ExitCommand;
import org.obiba.opal.shell.commands.HelpCommand;
import org.obiba.opal.shell.commands.ImportCommand;
import org.obiba.opal.shell.commands.KeyCommand;
import org.obiba.opal.shell.commands.QuitCommand;
import org.obiba.opal.shell.commands.ShowCommand;
import org.obiba.opal.shell.commands.SplitCommand;
import org.obiba.opal.shell.commands.VersionCommand;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.shell.commands.options.DecryptCommandOptions;
import org.obiba.opal.shell.commands.options.ExitCommandOptions;
import org.obiba.opal.shell.commands.options.HelpCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;
import org.obiba.opal.shell.commands.options.QuitCommandOptions;
import org.obiba.opal.shell.commands.options.ShowCommandOptions;
import org.obiba.opal.shell.commands.options.SplitCommandOptions;
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
    addAvailableCommand(DecryptCommand.class, DecryptCommandOptions.class);
    addAvailableCommand(VersionCommand.class, VersionCommandOptions.class);
    addAvailableCommand(ShowCommand.class, ShowCommandOptions.class);
    addAvailableCommand(CopyCommand.class, CopyCommandOptions.class);
    addAvailableCommand(SplitCommand.class, SplitCommandOptions.class);

    addAvailableCommand(ListenCommand.class, ListenCommandOptions.class);
  }

}