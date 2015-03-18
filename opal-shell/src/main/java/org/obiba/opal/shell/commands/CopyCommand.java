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
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.RenameValueTable;
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
import org.obiba.opal.core.magma.QueryWhereClause;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Provides ability to copy Magma tables to an existing datasource or a file based datasource.
 */
@CommandUsage(
    description = "Copy tables to an existing destination datasource or to a specified file. " +
        "The tables can be explicitly named and/or be the ones from a specified source datasource. " +
        "The variables can be optionally processed: dispatched in another table and/or renamed.",
    syntax = "Syntax: copy [--unit UNIT] [--source NAME] (--destination NAME | --out FILE) [--multiplex SCRIPT] " +
        "[--transform SCRIPT] [--name NAME] [--non-incremental] [--no-values | --no-variables] [--copy-null] [TABLE_NAME...]")
public class CopyCommand extends AbstractOpalRuntimeDependentCommand<CopyCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(CopyCommand.class);

  @Autowired
  ApplicationContext applicationContext;

  @Autowired
  private DataExportService dataExportService;

  @NotNull
  private final FileDatasourceFactory fileDatasourceFactory;

  public CopyCommand() {
    fileDatasourceFactory = new MultipleFileCsvDatasourceFactory();
    fileDatasourceFactory.setNext(new SingleFileCsvDatasourceFactory()) //
        .setNext(new ExcelDatasourceFactory()) //
        .setNext(new FsDatasourceFactory()) //
        .setNext(new NullDatasourceFactory());
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public int execute() {
    int errorCode = CommandResultCode.CRITICAL_ERROR; // initialize as non-zero (error)

    if(validateOptions()) {
      Datasource destinationDatasource = null;

      try {
        destinationDatasource = getDestinationDatasource();
        Set<ValueTable> tables = getValueTables();
        for(ValueTable table : tables) {
          if(destinationDatasource.getName().equals(table.getDatasource().getName()) &&
              destinationDatasource.hasValueTable(table.getName())) {
            if(tables.size() > 1 || tables.size() == 1 && !options.isName())
              throw new IllegalArgumentException("Cannot copy a table into itself: " + table.getName());
          }
        }
        getShell().printf("Copying tables [%s] to %s.\n", getTableNames(), destinationDatasource.getName());
        dataExportService
            .exportTablesToDatasource(options.isUnit() ? options.getUnit() : null, tables, destinationDatasource,
                buildDatasourceCopier(destinationDatasource), !options.getNonIncremental(), new CopyProgressListener());
        getShell().printf("Successfully copied all tables.\n");
        errorCode = CommandResultCode.SUCCESS;
      } catch(Exception e) {
        if(!Strings.isNullOrEmpty(e.getMessage())) getShell().printf("%s\n", e.getMessage());
        //noinspection UseOfSystemOutOrSystemErr
        e.printStackTrace(System.err);
      } finally {
        if(options.isOut()) {
          Disposables.silentlyDispose(destinationDatasource);
        }
      }
    }

    return errorCode;
  }

  private String getTableNames() {
    List<String> names = Lists.newArrayList();

    if(options.isSource()) {
      for(ValueTable table : getDatasourceByName(options.getSource()).getValueTables()) {
        names.add(table.getName());
      }
    }

    if(options.getTables() != null) {
      for(String name : options.getTables()) {
        names.add(name);
      }
    }

    return Joiner.on(", ").join(Iterables.transform(names, new Function<String, String>() {

      @Override
      public String apply(String input) {
        return input.substring(input.indexOf('.') + 1);
      }
    }));
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
      appendFlag(sb, "copy-null", options.getCopyNullValues());
      appendUnparsedList(sb, options.getTables());
    }

    return sb.toString();
  }

  private DatasourceCopier.Builder buildDatasourceCopier(Datasource destinationDatasource) {
    // build a datasource copier according to options
    DatasourceCopier.Builder builder;
    builder = options.getNoValues()
        ? DatasourceCopier.Builder.newCopier().dontCopyValues()
        : dataExportService.newCopier(destinationDatasource);

    if(options.getNoVariables()) {
      builder.dontCopyMetadata();
    }

    if(options.isMultiplex()) {
      builder.withMultiplexingStrategy(new JavascriptMultiplexingStrategy(options.getMultiplex()));
    }

    if(options.isTransform()) {
      builder.withVariableTransformer(new JavascriptVariableTransformer(options.getTransform()));
    }

    builder.copyNullValues(options.getCopyNullValues());

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
    Map<String, ValueTable> tablesByName = new HashMap<>();

    if(options.isSource()) {
      for(ValueTable table : getDatasourceByName(options.getSource()).getValueTables()) {
        tablesByName.put(table.getDatasource().getName() + "." + table.getName(), table);
      }
    }

    if(options.getTables() != null) {
      for(String name : options.getTables()) {
        if(!tablesByName.containsKey(name)) {
          tablesByName.put(name, MagmaEngineTableResolver.valueOf(name).resolveTable());
        }
      }
    }

    applyNameOption(tablesByName);


    return ImmutableSet.copyOf(tablesByName.values());
  }

  private void applyNameOption(Map<String, ValueTable> tablesByName) {
    if(tablesByName.size() == 1 && options.isName()) {
      String originalName = tablesByName.keySet().iterator().next();
      if(originalName.equals(options.getDestination() + "." + options.getName()))
        throw new IllegalArgumentException("Cannot copy a table into itself: " + originalName);
      tablesByName.put(originalName, new RenameValueTable(options.getName(), tablesByName.get(originalName)));
    }
  }

  private void applyQueryOption(Map<String, ValueTable> tablesByName) {
    //if (!options.isQuery()) return;

    QueryWhereClause queryWhereClause = applicationContext.getBean("searchQueryWhereClause", QueryWhereClause.class);
    //queryWhereClause.setQuery(options.getQuery());

  }

  private Datasource getDatasourceByName(String datasourceName) {
    return MagmaEngine.get().getDatasource(datasourceName);
  }

  private boolean validateOptions() {
    return validateSourceOrTables() && validateSource() && validateDestination() &&
        validateTables() && validateSwitches();
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
          getShell().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(),
              resolver.getDatasourceName());
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
   * @return A FileObject representing the output file.
   * @throws FileSystemException
   */
  private FileObject resolveOutputFileAndCreateParentFolders() throws FileSystemException {
    FileObject outputFile;
    outputFile = getFile(options.getOut());

    // Create the parent directory, if it doesn't already exist.
    FileObject directory = outputFile.getParent();
    if(directory != null) {
      directory.createFolder();
    }

    if(Strings.isNullOrEmpty(outputFile.getName().getExtension())) {
      outputFile.createFolder();
    }

    if("xls".equals(outputFile.getName().getExtension())) {
      getShell()
          .printf("WARNING: Writing to an Excel 97 spreadsheet. These are limited to 256 columns and 65536 rows " +
              "which may not be sufficient for writing large tables.\nUse an 'xlsx' extension to use Excel 2007 format " +
              "which supports 16K columns.\n");
    }
    return outputFile;
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

  private void appendUnparsedList(StringBuffer sb, Iterable<String> unparsedList) {
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

    public FileDatasourceFactory setNext(@SuppressWarnings("ParameterHidesMemberVariable") FileDatasourceFactory next) {
      this.next = next;
      return next;
    }

    /**
     * Create a datasource and if null, ask to the next factory in the chain to do it.
     *
     * @param outputFile
     * @return null if no datasource could be created along the chain from this factory.
     * @throws IOException
     */
    @Nullable
    public Datasource createDatasource(FileObject outputFile) throws IOException {
      Datasource ds = internalCreateDatasource(outputFile);
      if(ds == null && next != null) {
        ds = next.createDatasource(outputFile);
      }
      return ds;
    }

    /**
     * Create a datasource if applicable or return null.
     *
     * @param outputFile
     * @return null if parameters are not applicable.
     * @throws IOException
     */
    @Nullable
    abstract protected Datasource internalCreateDatasource(FileObject outputFile) throws IOException;
  }

  abstract class CsvDatasourceFactory extends FileDatasourceFactory {

    protected void addCsvValueTable(CsvDatasource ds, ValueTable table, @Nullable File variablesFile,
        @Nullable File dataFile) {
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

    @Nullable
    @Override
    protected Datasource internalCreateDatasource(FileObject outputFile) throws IOException {
      if(outputFile.getType() == FileType.FOLDER) {
        return getMultipleFileCsvDatasource(getLocalFile(outputFile));
      }
      return null;
    }

    private Datasource getMultipleFileCsvDatasource(File directory) throws IOException {
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

    private Datasource getSingleFileCsvDatasource(String name, File csvFile) throws IOException {
      CsvDatasource ds = new CsvDatasource(name);

      // one table only
      Set<ValueTable> tables = getValueTables();
      if(tables.size() > 1) {
        throw new IllegalArgumentException(
            "Only one table expected when writing to a CSV file. Provide a directory instead for copying several tables.");
      }

      if(!options.getNoVariables() && !options.getNoValues()) {
        throw new IllegalArgumentException(
            "Writing both variables and values in the same CSV file is not supported. Provide a directory instead.");
      }
      if(!options.getNoVariables()) {
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
    public Datasource internalCreateDatasource(FileObject outputFile) {
      if(outputFile.getName().getExtension().startsWith("xls")) {
        return new ExcelDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }
  }

  class FsDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(FileObject outputFile) {
      if(outputFile.getName().getExtension().startsWith("zip")) {
        return new FsDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }
  }

  class NullDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(FileObject outputFile) {
      if("/dev/null".equals(outputFile.getName().getPath())) {
        return new NullDatasource("/dev/null");
      }
      return null;
    }
  }

  private class CopyProgressListener implements DatasourceCopierProgressListener {

    private int currentPercentComplete = -1;

    @Override
    public void status(String message, long entitiesCopied, long entitiesToCopy, int percentComplete) {
      if(percentComplete != currentPercentComplete) {
        getShell().progress(message, entitiesCopied, entitiesToCopy, percentComplete);
        currentPercentComplete = percentComplete;
      }
    }
  }
}
