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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.csv.support.CsvUtil;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.nil.NullDatasource;
import org.obiba.magma.js.support.JavascriptMultiplexingStrategy;
import org.obiba.magma.js.support.JavascriptVariableTransformer;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

/**
 * Provides ability to copy Magma tables to an existing datasource or a file based datasource.
 */
@CommandUsage(description = "Copy tables to an existing destination datasource or to a specified file. The tables can be explicitly named and/or be the ones from a specified source datasource. The variables can be optionally processed: dispatched in another table and/or renamed.", syntax = "Syntax: copy [--unit UNIT] [--source NAME] (--destination NAME | --out FILE) [--multiplex SCRIPT] [--transform SCRIPT] [--non-incremental] [--no-values | --no-variables] [TABLE_NAME...]")
public class CopyCommand extends AbstractOpalRuntimeDependentCommand<CopyCommandOptions> {

  @Autowired
  private ExportService exportService;

  private FileDatasourceFactory fileDatasourceFactory;

  private static final Logger log = LoggerFactory.getLogger(CopyCommand.class);

  public void setExportService(ExportService exportService) {
    this.exportService = exportService;
  }

  public CopyCommand() {
    super();

    this.fileDatasourceFactory = new MultipleFileCsvDatasourceFactory();
    fileDatasourceFactory.setNext(new SingleFileCsvDatasourceFactory())//
    .setNext(new ExcelDatasourceFactory())//
    .setNext(new FsDatasourceFactory()) //
    .setNext(new NullDatasourceFactory());
  }

  public int execute() {
    int errorCode = 1; // initialize as non-zero (error)

    if(validateOptions()) {
      Datasource destinationDatasource = null;

      try {
        destinationDatasource = getDestinationDatasource();
        Set<ValueTable> tables = getValueTables();
        getShell().printf("Copying %d tables to %s.\n", tables.size(), destinationDatasource.getName());
        exportService.exportTablesToDatasource(options.isUnit() ? options.getUnit() : null, tables, destinationDatasource, buildDatasourceCopier(destinationDatasource), !options.getNonIncremental());
        getShell().printf("Successfully copied all tables.\n");
        errorCode = 0; // success!
      } catch(Exception e) {
        getShell().printf("%s\n", e.getMessage());
        e.printStackTrace(System.err);
      } finally {
        if(options.isOut() && destinationDatasource != null) {
          Disposables.silentlyDispose(destinationDatasource);
        }
      }
    }

    return errorCode;
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

  private Datasource getDestinationDatasource() throws IOException {
    Datasource destinationDatasource;
    if(options.isDestination()) {
      destinationDatasource = getDatasourceByName(options.getDestination());
    } else {
      destinationDatasource = fileDatasourceFactory.createDatasource(getOutputFile());
      if(destinationDatasource == null) {
        throw new IllegalArgumentException("Unknown output datasource type");
      }
      Initialisables.initialise(destinationDatasource);
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

  //
  // File datasource factory classes
  //

  abstract class FileDatasourceFactory {
    protected FileDatasourceFactory next;

    public FileDatasourceFactory setNext(FileDatasourceFactory next) {
      this.next = next;
      return next;
    }

    /**
     * Create a datasource and if null, ask to the next factory in the chain to do it.
     * @param outputFile
     * @return null if no datasource could be created along the chain from this factory.
     * @throws IOException
     */
    public Datasource createDatasource(final FileObject outputFile) throws IOException {
      Datasource ds = internalCreateDatasource(outputFile);
      if(ds == null && next != null) {
        ds = next.createDatasource(outputFile);
      }
      return ds;
    }

    /**
     * Create a datasource if applicable or return null.
     * @param outputFile
     * @return null if parameters are not applicable.
     * @throws IOException
     */
    abstract protected Datasource internalCreateDatasource(final FileObject outputFile) throws IOException;
  }

  abstract class CsvDatasourceFactory extends FileDatasourceFactory {

    protected void addCsvValueTable(CsvDatasource ds, ValueTable table, File variablesFile, File dataFile) {
      ds.addValueTable(table.getName(), variablesFile, dataFile);
      ds.setVariablesHeader(table.getName(), CsvUtil.getCsvVariableHeader(table));
    }

    protected void createFileIfNotExists(File f) throws IOException {
      if(!f.exists() && !f.createNewFile()) {
        throw new IllegalArgumentException("Unable to create the file: " + f);
      }
    }

  }

  class MultipleFileCsvDatasourceFactory extends CsvDatasourceFactory {

    @Override
    protected Datasource internalCreateDatasource(FileObject outputFile) throws IOException {
      if(outputFile.getType().equals(FileType.FOLDER)) {
        return getMultipleFileCsvDatasource(getLocalFile(outputFile));
      }
      return null;
    }

    private CsvDatasource getMultipleFileCsvDatasource(final File directory) throws IOException {
      CsvDatasource ds = new CsvDatasource(directory.getName());
      for(ValueTable table : getValueTables()) {
        File tableDir = new File(directory, table.getName());
        if(tableDir.exists() || tableDir.mkdir()) {
          File variablesFile = null;
          File dataFile = null;
          if(!options.getNoVariables()) {
            createFileIfNotExists(variablesFile = new File(tableDir, CsvDatasource.VARIABLES_FILE));
          }
          if(!options.getNoValues()) {
            createFileIfNotExists(dataFile = new File(tableDir, CsvDatasource.DATA_FILE));
          }
          addCsvValueTable(ds, table, variablesFile, dataFile);
        } else {
          throw new IllegalArgumentException("Unable to create the directory: " + tableDir);
        }
      }
      return ds;
    }
  }

  class SingleFileCsvDatasourceFactory extends CsvDatasourceFactory {

    @Override
    protected Datasource internalCreateDatasource(FileObject outputFile) throws IOException {
      if(outputFile.getName().getExtension().startsWith("csv")) {
        return getSingleFileCsvDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }

    private CsvDatasource getSingleFileCsvDatasource(String name, final File csvFile) throws IOException {
      CsvDatasource ds = new CsvDatasource(name);

      // one table only
      Set<ValueTable> tables = getValueTables();
      if(tables.size() > 1) {
        throw new IllegalArgumentException("Only one table expected when writting to a CSV file. Provide a directory instead for copying several tables.");
      }

      if(!options.getNoVariables() && !options.getNoValues()) {
        throw new IllegalArgumentException("Writting both variables and values in the same CSV file is not supported. Provide a directory instead.");
      } else if(!options.getNoVariables()) {
        createFileIfNotExists(csvFile);
        addCsvValueTable(ds, tables.iterator().next(), csvFile, null);
      } else if(!options.getNoValues()) {
        createFileIfNotExists(csvFile);
        addCsvValueTable(ds, tables.iterator().next(), null, csvFile);
      }

      return ds;
    }

  }

  class ExcelDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(final FileObject outputFile) {
      if(outputFile.getName().getExtension().startsWith("xls")) {
        return new ExcelDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }
  }

  class FsDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(final FileObject outputFile) {
      if(outputFile.getName().getExtension().startsWith("zip")) {
        return new FsDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }
  }

  class NullDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(final FileObject outputFile) {
      if(outputFile.getName().getPath().equals("/dev/null")) {
        return new NullDatasource("/dev/null");
      }
      return null;
    }
  }
}
