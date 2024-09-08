/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.web;

import org.obiba.opal.shell.AbstractCommandRegistry;
import org.obiba.opal.shell.commands.*;
import org.obiba.opal.shell.commands.options.*;
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
    addAvailableCommand("analyse", AnalyseCommand.class, AnalyseCommandOptions.class);
    addAvailableCommand("reload", ReloadDatasourceCommand.class, ReloadDatasourceCommandOptions.class);
    addAvailableCommand("backup", BackupCommand.class, BackupCommandOptions.class);
    addAvailableCommand("restore", RestoreCommand.class, RestoreCommandOptions.class);
    addAvailableCommand(ImportVCFCommand.class, ImportVCFCommandOptions.class);
    addAvailableCommand(ExportVCFCommand.class, ExportVCFCommandOptions.class);
    addAvailableCommand("r-packages", RPackagesCommand.class, RPackagesCommandOptions.class);
    addAvailableCommand("r-package", RPackageCommand.class, RPackageCommandOptions.class);
  }
}
