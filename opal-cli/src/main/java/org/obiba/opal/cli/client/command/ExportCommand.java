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

import org.obiba.opal.cli.client.command.options.ExportCommandOptions;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides ability to export Magma tables to an existing datasource or an Excel file.
 */
@CommandUsage(description = "Exports tables to an existing datasource or to the specified Excel file.", syntax = "Syntax: export (--destination NAME | --out FILE) TABLE_NAME...")
public class ExportCommand extends AbstractOpalRuntimeDependentCommand<ExportCommandOptions> {

  @Autowired
  private ExportService exportService;

  public void execute() {
    if(options.getTables() != null) {
      validateOptions();
      try {
        if(options.isDestination()) {
          exportService.exportTablesToDatasource(options.getTables(), options.getDestination());
        } else if(options.isOut()) {
          exportService.exportTablesToExcelFile(options.getTables(), options.getOut());
        }
      } catch(ExportException e) {
        System.err.println("Unrecoverable error during export: " + e.getMessage());
        throw e;
      }
    } else {
      throw new IllegalArgumentException("No input (specify one or more table names to export)");
    }
  }

  private void validateOptions() {
    if(!options.isDestination() && !options.isOut()) {
      throw new IllegalArgumentException("Must provide either the 'destination' option or the 'out' option.");
    }
    if(options.isDestination() && options.isOut()) {
      throw new IllegalArgumentException("The 'destination' option and the 'out' option are mutually exclusive.");
    }
  }

}