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

import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.cli.client.command.options.ExportCommandOptions;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides ability to copy Magma tables to an existing datasource or an Excel file.
 */
@CommandUsage(description = "Exports tables to an existing datasource or to the specified Excel file.", syntax = "Syntax: export (--destination NAME | --out FILE) TABLE_NAME...")
public class ExportCommand extends AbstractOpalRuntimeDependentCommand<ExportCommandOptions> {

  @Autowired
  private ExportService exportService;

  public void execute() {
    if(options.getTables() != null) {
      if(validateOptions()) {
        try {
          if(options.isDestination()) {
            exportService.exportTablesToDatasource(options.getTables(), options.getDestination(), !options.getNonIncremental());
          } else if(options.isOut()) {
            if(options.getOut().canWrite() == false) {
              System.console().printf("Cannot write to file %s\n", options.getOut().getName());
            } else {
              exportService.exportTablesToExcelFile(options.getTables(), options.getOut(), !options.getNonIncremental());
            }
          }
        } catch(ExportException e) {
          System.console().printf("%s\n", e.getMessage());
          System.err.println(e);
        } catch(NoSuchDatasourceException e) {
          System.console().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        } catch(NoSuchValueTableException e) {
          System.console().printf("%s\n", e.getMessage());
          System.err.println(e);
        } catch(UnsupportedOperationException e) {
          System.console().printf("%s\n", e.getMessage());
        }
      }
    } else {
      System.console().printf("%s\n", "No table name(s) specified.");
    }
  }

  private boolean validateOptions() {
    boolean validated = true;
    if(!options.isDestination() && !options.isOut()) {
      System.console().printf("Must provide either the 'destination' option or the 'out' option.\n");
      validated = false;
    }
    if(options.isDestination() && options.isOut()) {
      System.console().printf("The 'destination' option and the 'out' option are mutually exclusive.\n");
      validated = false;
    }
    return validated;
  }

}
