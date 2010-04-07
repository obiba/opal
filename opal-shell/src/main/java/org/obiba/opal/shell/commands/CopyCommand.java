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

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.js.support.JavascriptMultiplexingStrategy;
import org.obiba.magma.js.support.JavascriptVariableTransformer;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

/**
 * Provides ability to copy Magma tables to an existing datasource or an Excel file.
 */
@CommandUsage(description = "Copy tables to an existing destination datasource or to a specified Excel file. The tables can be explicitly named and/or be the ones from a specified source datasource. The variables can be optionally processed: dispatched in another table and/or renamed.", syntax = "Syntax: copy [--source NAME] (--destination NAME | --out FILE) [--multiplex SCRIPT] [--transform SCRIPT] [--non-incremental] [--no-values | --no-variables] [TABLE_NAME...]")
public class CopyCommand extends AbstractOpalRuntimeDependentCommand<CopyCommandOptions> {

  @Autowired
  private ExportService exportService;

  private static final Logger log = LoggerFactory.getLogger(CopyCommand.class);

  public void setExportService(ExportService exportService) {
    this.exportService = exportService;
  }

  public void execute() {
    if(options.getTables() != null || options.isSource()) {
      if(validateOptions()) {
        Datasource destinationDatasource = null;

        try {
          destinationDatasource = getDestinationDatasource();
          exportService.exportTablesToDatasource(getValueTables(), destinationDatasource, buildDatasourceCopier(destinationDatasource), !options.getNonIncremental());

        } catch(ExportException e) {
          getShell().printf("%s\n", e.getMessage());
          e.printStackTrace(System.err);
        } catch(Exception e) {
          getShell().printf("%s\n", e.getMessage());
          e.printStackTrace(System.err);
        } finally {
          if(options.isOut() && destinationDatasource != null) {
            try {
              MagmaEngine.get().removeDatasource(destinationDatasource);
            } catch(RuntimeException e) {

            }
          }
        }
      }
    } else {
      getShell().printf("%s\n", "Neither source nor table name(s) are specified.");
    }
  }

  private DatasourceCopier buildDatasourceCopier(Datasource destinationDatasource) {
    // build a datasource copier according to options
    DatasourceCopier.Builder builder;
    if(options.getNoValues()) {
      builder = DatasourceCopier.Builder.newCopier().dontCopyValues();
    } else {
      // get a builder with logging facilities
      builder = exportService.newCopier(destinationDatasource);
    }

    if(options.getNoVariables()) {
      builder.dontCopyMetadata();
    }

    if(options.isMultiplex()) {
      builder.withMultiplexingStrategy(new JavascriptMultiplexingStrategy(options.getMultiplex()));
    }

    if(options.isTransform()) {
      builder.withVariableTransformer(new JavascriptVariableTransformer(options.getTransform()));
    }
    return builder.build();
  }

  private Datasource getDestinationDatasource() {
    Datasource destinationDatasource;
    if(options.isDestination()) {
      destinationDatasource = getDatasourceByName(options.getDestination());
    } else {
      FileObject outputFile = getOuputFile();
      destinationDatasource = new ExcelDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      MagmaEngine.get().addDatasource(destinationDatasource);
    }
    return destinationDatasource;
  }

  private Set<ValueTable> getValueTables() {
    HashMap<String, ValueTable> names = new HashMap<String, ValueTable>();

    if(options.isSource()) {
      for(ValueTable table : getDatasourceByName(options.getSource()).getValueTables()) {
        names.put(table.getDatasource().getName() + "." + table.getName(), table);
      }
    }

    if(options.getTables() != null) {
      for(String name : options.getTables()) {
        if(!names.containsKey(name)) {
          names.put(name, MagmaEngineTableResolver.valueOf(name).resolveTable());
        }
      }
    }

    return ImmutableSet.copyOf(names.values());
  }

  private Datasource getDatasourceByName(String datasourceName) {
    return MagmaEngine.get().getDatasource(datasourceName);
  }

  private boolean validateOptions() {
    boolean validated = validateDestination();
    validated = validateSource(validated);
    validated = validateTables(validated);
    validated = validateSwitches(validated);
    return validated;
  }

  private boolean validateSwitches(boolean validated) {
    if(options.getNoValues() && options.getNoVariables()) {
      getShell().printf("Must at least copy variables or values.\n");
      validated = false;
    }
    return validated;
  }

  private boolean validateTables(boolean validated) {
    if(options.getTables() != null) {
      for(String tableName : options.getTables()) {
        MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
        try {
          resolver.resolveTable();
        } catch(NoSuchDatasourceException e) {
          getShell().printf("'%s' refers to an unknown datasource: '%s'.\n", tableName, resolver.getDatasourceName());
          validated = false;
        } catch(NoSuchValueTableException e) {
          getShell().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(), resolver.getDatasourceName());
          validated = false;
        }
      }
    }
    return validated;
  }

  private boolean validateSource(boolean validated) {
    if(options.isSource()) {
      try {
        getDatasourceByName(options.getSource());
      } catch(NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        validated = false;
      }
    }
    return validated;
  }

  private boolean validateDestination() {
    boolean validated = true;
    if(!options.isDestination() && !options.isOut()) {
      getShell().printf("Must provide either the 'destination' option or the 'out' option.\n");
      validated = false;
    }
    if(options.isDestination() && options.isOut()) {
      getShell().printf("The 'destination' option and the 'out' option are mutually exclusive.\n");
      validated = false;
    }
    if(options.isDestination()) {
      try {
        getDatasourceByName(options.getDestination());
      } catch(NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        validated = false;
      }
    }
    return validated;
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
      return resolveOutputFileAndCreateParentFolders();
    } catch(FileSystemException e) {
      log.error("There was an error accessing the output file", e);
      throw new RuntimeException("There was an error accessing the output file", e);
    }
  }

  /**
   * Resolves the output file based on the command parameter. Creates the necessary parent folders (when required).
   * 
   * @return A FileObject representing the ouput file.
   * @throws FileSystemException
   */
  private FileObject resolveOutputFileAndCreateParentFolders() throws FileSystemException {
    FileObject outputFile = getFileSystemRoot().resolveFile(options.getOut());

    // Create the parent directory, if it doesn't already exist.
    FileObject directory = outputFile.getParent();
    if(directory != null) {
      directory.createFolder();
    }

    if(outputFile.getName().getExtension().equals("xls")) {
      getShell().printf("WARNING: Writing to an Excel 97 spreadsheet. These are limited to 256 columns and 65536 rows which may not be sufficient for writing large tables.\nUse an 'xlsx' extension to use Excel 2007 format which supports 16K columns.\n");
    }
    return outputFile;
  }

}
