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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
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
      exportMetadata(tableNames, getOuputFile());
    }
  }

  /**
   * Export the tables metadata to the specified output file.
   * 
   * @param tableNames A list of table names to be exported.
   * @param outputFile The ouput file for the metadata.
   */
  private void exportMetadata(List<String> tableNames, FileObject outputFile) {
    Datasource outputDatasource = new ExcelDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
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

  /**
   * Validates that a corresponding table exist for each table name. Display feedback to the user for each invalid table
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
        if(!validateTableName(tableName)) {
          isValid = false;
        }
      }
      return isValid;
    }
  }

  /**
   * Validates that a corresponding table exist for one specific table name.
   * 
   * @param tableName The table name to validate.
   */
  private boolean validateTableName(String tableName) {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
    try {
      resolver.resolveTable();
      return true;
    } catch(NoSuchDatasourceException e) {
      getShell().printf("'%s' refers to an unknown datasource: '%s'.\n", tableName, resolver.getDatasourceName());
      return false;
    } catch(NoSuchValueTableException e) {
      getShell().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(), resolver.getDatasourceName());
      return false;
    }
  }

  /**
   * Get the output file to which the metadata will be exported to.
   * 
   * @return The output file.
   * @throws FileSystemException
   */
  private FileObject getOuputFile() {

    try {

      // Get the file specified on the command line.
      if(options.isOutputFile()) {
        return resolveOutputFileAndCreateParentFolders();

        // Generate a file name automatically when not specified by user.
      } else {
        return generateOutputFile();
      }

    } catch(FileSystemException e) {
      log.error("There was an error accessing the output file", e);
      throw new RuntimeException("There was an error accessing the output file", e);
    }

  }

  /**
   * Generates an output file based on the current system date an time.
   * 
   * @return A FileObject representing the ouput file.
   * @throws FileSystemException
   */
  private FileObject generateOutputFile() throws FileSystemException {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
    return getFileSystemRoot().resolveFile("variables-" + dateFormatter.format(new Date()) + ".xlsx");
  }

  /**
   * Resolves the output file based on the command parameter. Creates the necessary parent folders (when required).
   * 
   * @return A FileObject representing the ouput file.
   * @throws FileSystemException
   */
  private FileObject resolveOutputFileAndCreateParentFolders() throws FileSystemException {
    FileObject outputFile = getFileSystemRoot().resolveFile(options.getOutputFile());

    // Create the parent directory, if it doesn't already exist.
    FileObject directory = outputFile.getParent();
    if(directory != null) {
      directory.createFolder();
    }

    if(outputFile.getName().getExtension().equals("xls")) {
      System.console().printf("WARNING: Writing to an Excel 97 spreadsheet. These are limited to 256 columns which may not be sufficient for writing large tables.\nUse an 'xlsx' extension to use Excel 2007 format which supports 16K columns.\n");
    }
    return outputFile;
  }
}
