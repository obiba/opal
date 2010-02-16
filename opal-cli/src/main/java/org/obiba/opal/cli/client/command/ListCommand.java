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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.cli.client.command.options.ListCommandOptions;
import org.obiba.opal.core.service.ExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "List the metadata (the variables and their associated categories) for one or multiples tables.  The output of this command is an Excel file containing the metadata.\n\nSyntax: list [--out FILE] TABLE [TABLE...]")
public class ListCommand extends AbstractOpalRuntimeDependentCommand<ListCommandOptions> {

  @Autowired
  private ExportService exportService;

  private static final Logger log = LoggerFactory.getLogger(ListCommand.class);

  public void execute() {

    List<String> tableNames = options.getTables();

    // Make sure that each table exist, before launching the export.
    if(validateTableNames(tableNames)) {

      // TODO: This is a temporary implementation that writes to a FsDatasource instead of an ExcelDatasource. We will
      // have to change this once ExcelDatasource is available.
      Datasource outputDatasource = new FsDatasource(getOuputFile().getName(), getOuputFile());

      try {

        MagmaEngine.get().addDatasource(outputDatasource);

        // Create a DatasourceCopier that will copy only the metadata and export.
        DatasourceCopier metaDataCopier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
        exportService.exportTablesToDatasource(tableNames, outputDatasource.getName(), metaDataCopier);

      } catch(Exception couldNotExecuteListCommand) {
        log.error("An error was encountered while executing the 'list' command.", couldNotExecuteListCommand);
        System.err.println("An error was encountered while executing the 'list' command.");

      } finally {
        try {
          MagmaEngine.get().removeDatasource(outputDatasource);
        } catch(Exception e) {
          log.warn("Could not remove the following datasource : {}", outputDatasource.getName(), e);
        }
      }

    }

  }

  /**
   * Validate that a corresponding table exist for each table name. Display feedback to the user for each invalid table
   * name.
   * 
   * @param tableNames A list of table name to validate.
   * @return True if all table names are valid, false otherwise.
   */
  private boolean validateTableNames(List<String> tableNames) {

    // No table.
    if(tableNames == null || tableNames.size() < 1) {
      System.err.println("You must specify at least one table to export.\nType 'list --help' for command usage.\n");
      return false;

      // One or more table to validate.
    } else {
      boolean isValid = true;
      for(String tableName : tableNames) {
        try {
          MagmaEngineTableResolver.valueOf(tableName).resolveTable();
        } catch(Exception e) {
          System.err.printf("'%s' does not exist or is not a valid table name.\n", tableName);
          isValid = false;
        }
      }

      if(!isValid) {
        System.err.println();
      }

      return isValid;
    }
  }

  /**
   * Get the output file to which the metadata will be exported to.
   * 
   * @return The output file.
   */
  private File getOuputFile() {

    // Get the file specified on the command line.
    if(options.isOutputFile()) {
      File outputFile = options.getOutputFile();

      // Create the parent directory, if it doesn't already exist.
      File directory = outputFile.getParentFile();
      if(directory != null && !directory.exists()) {
        directory.mkdirs();
      }

      return options.getOutputFile();

      // Generate the default file name if no file name was specified.
    } else {

      // Generate a file name automatically when not specified by user.
      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
      return new File("variables-" + dateFormatter.format(new Date()) + ".zip");
    }

  }
}
