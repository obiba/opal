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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.shell.commands.options.ListCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Lists the metadata (variables, categories and attributes) for one or more tables. The output of this command is written to an Excel file.", syntax = "Syntax: list [--outputFile FILE] TABLE [TABLE...]")
public class ListCommand extends AbstractOpalRuntimeDependentCommand<ListCommandOptions> {

  @Autowired
  private ExportService exportService;

  private static final Logger log = LoggerFactory.getLogger(ListCommand.class);

  public void execute() {

    List<String> tableNames = options.getTables();

    // Make sure that each table exist, before launching the export.
    if(validateTableNames(tableNames)) {
      File outputFile = getOuputFile();
      Datasource outputDatasource = new ExcelDatasource(outputFile.getName(), outputFile);
      MagmaEngine.get().addDatasource(outputDatasource);
      try {
        // Create a DatasourceCopier that will copy only the metadata and export.
        DatasourceCopier metaDataCopier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
        exportService.exportTablesToDatasource(tableNames, outputDatasource.getName(), metaDataCopier, false);
        getShell().printf("Variables written to %s\n", outputFile.getName());
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
      getShell().printf("At least one table to export must be specified.\nType 'list --help' for command usage.\n");
      return false;
      // One or more table to validate.
    } else {
      boolean isValid = true;
      for(String tableName : tableNames) {
        MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
        try {
          resolver.resolveTable();
        } catch(NoSuchDatasourceException e) {
          getShell().printf("'%s' refers to an unknown datasource: '%s'.\n", tableName, resolver.getDatasourceName());
          isValid = false;
        } catch(NoSuchValueTableException e) {
          getShell().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(), resolver.getDatasourceName());
          isValid = false;
        }
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

      if(options.getOutputFile().getName().endsWith("xls")) {
        System.console().printf("WARNING: Writing to an Excel 97 spreadsheet. These are limited to 256 columns which may not be sufficient for writing large tables.\nUse an 'xlsx' extension to use Excel 2007 format which supports 16K columns.\n");
      }
      return options.getOutputFile();
    } else {

      // Generate a file name automatically when not specified by user.
      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
      return new File("variables-" + dateFormatter.format(new Date()) + ".xlsx");
    }

  }
}
