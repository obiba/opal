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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.nul.NullDatasource;
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
@CommandUsage(description = "Copy tables to an existing destination datasource or to a specified Excel file. The tables can be explicitly named and/or be the ones from a specified source datasource. The variables can be optionally processed: dispatched in another table and/or renamed.", syntax = "Syntax: copy [--unit UNIT] [--source NAME] (--destination NAME | --out FILE) [--multiplex SCRIPT] [--transform SCRIPT] [--non-incremental] [--no-values | --no-variables] [TABLE_NAME...]")
public class CopyCommand extends AbstractOpalRuntimeDependentCommand<CopyCommandOptions> {

  @Autowired
  private ExportService exportService;

  private static final Logger log = LoggerFactory.getLogger(CopyCommand.class);

  public void setExportService(ExportService exportService) {
    this.exportService = exportService;
  }

  public void execute() {
    if(validateOptions()) {
      Datasource destinationDatasource = null;

      try {
        destinationDatasource = getDestinationDatasource();
        exportService.exportTablesToDatasource(options.isUnit() ? options.getUnit() : null, getValueTables(), destinationDatasource, buildDatasourceCopier(destinationDatasource), !options.getNonIncremental());

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
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("copy");

    if(options != null) {
      appendOption(sb, "unit", options.isUnit(), options.getUnit());
      appendOption(sb, "source", options.isSource(), options.getSource());
      appendOption(sb, "destination", options.isDestination(), options.getDestination());
      appendOption(sb, "out", options.isOut(), options.getOut());
      appendOption(sb, "multiplex", options.isMultiplex(), options.getMultiplex());
      appendOption(sb, "transform", options.isTransform(), options.getTransform());
      appendFlag(sb, "non-incremental", options.getNonIncremental());
      appendFlag(sb, "no-values", options.getNoValues());
      appendFlag(sb, "no-variables", options.getNoVariables());
      appendUnparsedList(sb, options.getTables());
    }

    return sb.toString();
  }

  private DatasourceCopier.Builder buildDatasourceCopier(Datasource destinationDatasource) {
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
    return builder;
  }

  private Datasource getDestinationDatasource() {
    Datasource destinationDatasource;
    if(options.isDestination()) {
      destinationDatasource = getDatasourceByName(options.getDestination());
    } else {
      FileObject outputFile = getOutputFile();
      if(outputFile.getName().getExtension().startsWith("xls")) {
        destinationDatasource = new ExcelDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      } else if(outputFile.getName().getExtension().startsWith("zip")) {
        destinationDatasource = new FsDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      } else if(outputFile.getName().getPath().equals("/dev/null")) {
        destinationDatasource = new NullDatasource("/dev/null");
      } else {
        throw new IllegalArgumentException("unknown output datasource type");
      }
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
    return validateUnit() && validateSourceOrTables() && validateSource() && validateDestination() && validateTables() && validateSwitches();
  }

  private boolean validateUnit() {
    if(options.isUnit() && getOpalRuntime().getFunctionalUnit(options.getUnit()) == null) {
      getShell().printf("Functional unit '%s' does not exist.\n", options.getUnit());
      return false;
    }
    return true;
  }

  private boolean validateSourceOrTables() {
    if(!options.isSource() && options.getTables() == null) {
      getShell().printf("%s\n", "Neither source nor table name(s) are specified.");
      return false;
    }
    return true;
  }

  private boolean validateSwitches() {
    if(options.getNoValues() && options.getNoVariables()) {
      getShell().printf("Must at least copy variables or values.\n");
      return false;
    }
    return true;
  }

  private boolean validateTables() {
    if(options.getTables() != null) {
      for(String tableName : options.getTables()) {
        MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
        try {
          resolver.resolveTable();
        } catch(NoSuchDatasourceException e) {
          getShell().printf("'%s' refers to an unknown datasource: '%s'.\n", tableName, resolver.getDatasourceName());
          return false;
        } catch(NoSuchValueTableException e) {
          getShell().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(), resolver.getDatasourceName());
          return false;
        }
      }
    }
    return true;
  }

  private boolean validateSource() {
    if(options.isSource()) {
      try {
        getDatasourceByName(options.getSource());
      } catch(NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        return false;
      }
    }
    return true;
  }

  private boolean validateDestination() {
    if(!options.isDestination() && !options.isOut()) {
      getShell().printf("Must provide either the 'destination' option or the 'out' option.\n");
      return false;
    }
    if(options.isDestination() && options.isOut()) {
      getShell().printf("The 'destination' option and the 'out' option are mutually exclusive.\n");
      return false;
    }
    if(options.isDestination()) {
      try {
        getDatasourceByName(options.getDestination());
      } catch(NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        return false;
      }
    }
    return true;
  }

  /**
   * Get the output file to which the metadata will be exported to.
   * 
   * @return The output file.
   * @throws FileSystemException
   */
  private FileObject getOutputFile() {
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
    FileObject outputFile;
    if(options.isUnit() && isRelativeFilePath(options.getOut())) {
      outputFile = getFileInUnitDirectory(options.getOut());
    } else {
      outputFile = getFile(options.getOut());
    }

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

  private FileObject getFileInUnitDirectory(String filePath) throws FileSystemException {
    FileObject unitDir = getOpalRuntime().getUnitDirectory(options.getUnit());
    return unitDir.resolveFile(filePath);
  }

  private boolean isRelativeFilePath(String filePath) {
    return !(new File(filePath).isAbsolute());
  }

  private void appendOption(StringBuffer sb, String option, boolean optionSpecified, String value) {
    if(optionSpecified) {
      sb.append(" --");
      sb.append(option);
      sb.append(' ');
      sb.append(value);
    }
  }

  private void appendFlag(StringBuffer sb, String flag, boolean value) {
    if(value) {
      sb.append(" --");
      sb.append(flag);
    }
  }

  private void appendUnparsedList(StringBuffer sb, List<String> unparsedList) {
    for(String unparsed : unparsedList) {
      sb.append(' ');
      sb.append(unparsed);
    }
  }
}
