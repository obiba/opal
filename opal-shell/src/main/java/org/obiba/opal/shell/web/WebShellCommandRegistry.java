/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.web;

import org.obiba.opal.shell.AbstractCommandRegistry;
import org.obiba.opal.shell.commands.CopyCommand;
import org.obiba.opal.shell.commands.ImportCommand;
import org.obiba.opal.shell.commands.ReportCommand;
import org.obiba.opal.shell.commands.ValidateCommand;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.commands.options.ValidateCommandOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Available commands for Opal Web Services Shell.
 */
@Component
@Qualifier("web")
public class WebShellCommandRegistry extends AbstractCommandRegistry {

  public WebShellCommandRegistry() {

    addAvailableCommand(ImportCommand.class, ImportCommandOptions.class);
    addAvailableCommand(CopyCommand.class, CopyCommandOptions.class);
    addAvailableCommand("export", CopyCommand.class, CopyCommandOptions.class);
    addAvailableCommand(ReportCommand.class, ReportCommandOptions.class);
    addAvailableCommand(ValidateCommand.class, ValidateCommandOptions.class);
      
  }
}
