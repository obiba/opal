/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import jakarta.annotation.Nullable;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.json.JSONObject;
import org.obiba.core.util.FileUtil;
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
import org.obiba.magma.views.View;
import org.obiba.magma.views.support.AllClause;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.event.ValueTableAddedEvent;
import org.obiba.opal.core.magma.QueryWhereClause;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.ProjectsState;
import org.obiba.opal.core.service.ProjectsState.State;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.r.datasource.RExportDatasource;
import org.obiba.opal.r.magma.RFileSymbolWriter;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.spi.datasource.DatasourceService;
import org.obiba.opal.spi.datasource.DatasourceUsage;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.datasource.RDatasourceFactory;
import org.obiba.opal.spi.r.datasource.RDatasourceService;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.obiba.opal.shell.commands.CommandResultCode.SUCCESS;

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

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

  @Autowired
  ApplicationContext applicationContext;

  @Autowired
  private DataExportService dataExportService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private TransactionTemplate txTemplate;

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private EventBus eventBus;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  private Map<String, String> entityIdMap;

  @Value("${org.obiba.magma.entityIdNames}")
  private String defaultEntityIdNames;

  @Value("${org.obiba.magma.entityIdName}")
  private String defaultEntityIdName;
  @NotNull
  private final FileDatasourceFactory fileDatasourceFactory;

  private String destinationDatasourceName;

  private Set<ValueTable> valueTables;

  public CopyCommand() {
    fileDatasourceFactory = new MultipleFileCsvDatasourceFactory();
    fileDatasourceFactory.setNext(new SingleFileCsvDatasourceFactory()) //
        .setNext(new ExcelDatasourceFactory()) //
        .setNext(new FsDatasourceFactory()) //
        .setNext(new RHavenDatasourceFactory())
        .setNext(new NullDatasourceFactory());
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public int execute() {
    int errorCode = CommandResultCode.CRITICAL_ERROR; // initialize as non-zero (error)

    Stopwatch stopwatch = Stopwatch.createStarted();

    final ProjectsState projectsState = applicationContext.getBean(ProjectsState.class);
    projectsState.updateProjectState(options.getDestination(), State.BUSY);

    if (validateOptions()) {
      Datasource destinationDatasource = null;

      try {
        destinationDatasource = getDestinationDatasource();

        Set<ValueTable> tables = getValueTables();
        for (ValueTable table : tables) {
          if (destinationDatasource.getName().equals(table.getDatasource().getName()) &&
              destinationDatasource.hasValueTable(table.getName())) {
            if (tables.size() > 1 || tables.size() == 1 && !options.isName())
              throw new IllegalArgumentException("Cannot copy a table into itself: " + table.getName());
          }
        }
        getShell().printf("Copying tables [%s] to %s.\n", getTableNames(), destinationDatasource.getName());
        dataExportService
            .exportTablesToDatasource(options.isUnit() ? options.getUnit() : null, tables, destinationDatasource,
                buildDatasourceCopier(destinationDatasource), !options.getNonIncremental(), new CopyProgressListener(tables.size()));
        Disposables.dispose(destinationDatasource);
        getShell().printf("Successfully copied all tables.\n");
        errorCode = CommandResultCode.SUCCESS;
      } catch (Exception e) {
        if (!Strings.isNullOrEmpty(e.getMessage())) getShell().printf("%s\n", e.getMessage());
        //noinspection UseOfSystemOutOrSystemErr
        e.printStackTrace(System.err);
      } finally {
        if (options.isOut()) {
          if (options.getOutFormat().equalsIgnoreCase("jdbc") && !Strings.isNullOrEmpty(destinationDatasourceName)) {
            databaseRegistry.unregister(options.getOut(), destinationDatasourceName);
          }
        }
      }
    }

    if (errorCode != SUCCESS) {
      getShell().printf("Copy failed.\n");
      log.info("Copy failed in {}", stopwatch.stop());
    } else {
      getShell().printf("Copy done.\n");
      log.info("Copy succeed in {}", stopwatch.stop());
    }

    projectsState.updateProjectState(options.getDestination(), State.READY);
    return errorCode;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("copy");

    if (options != null) {
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

  //
  // Private methods
  //

  /**
   * Map of entity ID names by entity type.
   * @return
   */
  private Map<String, String> getEntityIdMap() {
    if (entityIdMap == null) {
      entityIdMap = Maps.newHashMap();

      String entityIdNames = null;
      if (options.isEntityIdNames())
        entityIdNames = options.getEntityIdNames();
      if (Strings.isNullOrEmpty(entityIdNames))
        entityIdNames = defaultEntityIdNames;

      if (!Strings.isNullOrEmpty(entityIdNames))
        Splitter.on(",").split(entityIdNames).forEach(token -> {
          String[] entry = token.trim().split("=");
          if (entry.length == 2)
            entityIdMap.put(entry[0].trim(), entry[1].trim());
          else
            entityIdMap.put("Participant", token.trim());
        });
    }
    return entityIdMap;
  }

  public String getDefaultEntityIdName() {
    if (options.isEntityIdNames() && !options.getEntityIdNames().contains("="))
      return options.getEntityIdNames();
    return defaultEntityIdName;
  }

  private String getTableNames() {
    List<String> names = Lists.newArrayList();

    if (options.isSource()) {
      for (ValueTable table : getDatasourceByName(options.getSource()).getValueTables()) {
        names.add(table.getName());
      }
    }

    if (options.getTables() != null) {
      for (String name : options.getTables()) {
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

  private DatasourceCopier.Builder buildDatasourceCopier(Datasource destinationDatasource) {
    // build a datasource copier according to options
    DatasourceCopier.Builder builder;
    builder = options.getNoValues()
        ? DatasourceCopier.Builder.newCopier().dontCopyValues()
        : dataExportService.newCopier(destinationDatasource);

    if (options.getNoVariables()) {
      builder.dontCopyMetadata();
    }

    if (options.isMultiplex()) {
      builder.withMultiplexingStrategy(new JavascriptMultiplexingStrategy(options.getMultiplex()));
    }

    if (options.isTransform()) {
      builder.withVariableTransformer(new JavascriptVariableTransformer(options.getTransform()));
    }

    builder.copyNullValues(options.getCopyNullValues());

    return builder;
  }

  private Datasource getDestinationDatasource() throws IOException {
    Datasource destinationDatasource;
    if (options.isDestination()) {
      destinationDatasource = getDatasourceByName(options.getDestination());
    } else {
      destinationDatasource = createDestinationDatasource();
    }
    return destinationDatasource;
  }

  private Datasource createDestinationDatasource() throws IOException {
    Datasource destinationDatasource;

    if (options.getOutFormat().equalsIgnoreCase("jdbc")) {
      Database database = databaseRegistry.getDatabase(options.getOut());
      destinationDatasource = databaseRegistry.createDatasourceFactory(DATE_FORMAT.format(new Date()), database).create();
      destinationDatasourceName = destinationDatasource.getName();
    } else if (opalRuntime.hasServicePlugin(options.getOutFormat())) {
      destinationDatasource = createDestinationPluginDatasource();
    } else {
      destinationDatasource = fileDatasourceFactory.createDatasource(getOutputFile());
    }

    if (destinationDatasource == null) {
      throw new IllegalArgumentException("Unknown output datasource type");
    }

    Initialisables.initialise(destinationDatasource);
    return destinationDatasource;
  }

  private Datasource createDestinationPluginDatasource() {
    DatasourceService datasourceService = (DatasourceService) opalRuntime.getServicePlugin(options.getOutFormat());
    if (!datasourceService.getUsages().contains(DatasourceUsage.EXPORT)) return null;

    datasourceService.setOpalFileSystemPathResolver(path -> {
      try {
        FileObject fileObject = opalFileSystemService.getFileSystem().getRoot().resolveFile(path);
        // check security
        if (fileObject.exists()) {
          if (!fileObject.isWriteable()) {
            throw new IllegalArgumentException("File cannot be written: " + path);
          }
        } else if (!fileObject.getParent().isWriteable()) {
          throw new IllegalArgumentException("File cannot be written: " + path);
        }
        return opalFileSystemService.getFileSystem().getLocalFile(fileObject);
      } catch (FileSystemException e) {
        throw new IllegalArgumentException("Failed resolving file path: " + path);
      }
    });

    JSONObject parameters = new JSONObject(options.getOut());
    if (datasourceService instanceof RDatasourceService) {
      final RServerSession rSession = opalRSessionManager.newSubjectRSession();
      rSession.setExecutionContext("Export");
      RSessionHandler rSessionHandler = new RSessionHandler() {
        @Override
        public ROperationTemplate getSession() {
          return rSession;
        }

        @Override
        public void onDispose() {
          opalRSessionManager.removeRSession(rSession.getId());
        }
      };
      ((RDatasourceService) datasourceService).setRSessionHandler(rSessionHandler);
      // ugly: no need to request creation of a datasource
      RDatasourceFactory rDatasourceFactory = (RDatasourceFactory) datasourceService.createDatasourceFactory(DatasourceUsage.EXPORT, parameters);
      RExportDatasource ds = new RExportDatasource(rDatasourceFactory.create().getName(), rSessionHandler, rDatasourceFactory.createSymbolWriter());
      ds.setMultilines(options.getMultilines());
      ds.setEntityIdNames(getEntityIdMap());
      ds.setEntityIdName(getDefaultEntityIdName());
      return ds;
    } else {
      return datasourceService.createDatasourceFactory(DatasourceUsage.EXPORT, parameters).create();
    }
  }

  private Set<ValueTable> getValueTables() {
    if (valueTables != null) return valueTables;
    Map<String, ValueTable> tablesByName = Maps.newHashMap();

    if (options.isSource()) {
      for (ValueTable table : getDatasourceByName(options.getSource()).getValueTables()) {
        tablesByName.put(table.getDatasource().getName() + "." + table.getName(), table);
      }
    }

    if (options.getTables() != null) {
      for (String name : options.getTables()) {
        if (!tablesByName.containsKey(name)) {
          tablesByName.put(name, MagmaEngineTableResolver.valueOf(name).resolveTable());
        }
      }
    }

    applyQueryOption(tablesByName);
    applyNameOption(tablesByName);

    valueTables = ImmutableSet.copyOf(tablesByName.values());
    return valueTables;
  }

  private void applyNameOption(Map<String, ValueTable> tablesByName) {
    if (tablesByName.size() == 1 && options.isName()) {
      String originalName = tablesByName.keySet().iterator().next();
      if (originalName.equals(options.getDestination() + "." + options.getName()))
        throw new IllegalArgumentException("Cannot copy a table into itself: " + originalName);
      tablesByName.put(originalName, new RenameValueTable(options.getName(), tablesByName.get(originalName)));
    }
  }

  private void applyQueryOption(Map<String, ValueTable> tablesByName) {
    if (!options.isQuery()) return;

    // make views with query where clause
    Map<String, View> viewsByName = Maps.newHashMap();
    for (Map.Entry<String, ValueTable> entry : tablesByName.entrySet()) {
      ValueTable table = entry.getValue();
      QueryWhereClause queryWhereClause = applicationContext.getBean("searchQueryWhereClause", QueryWhereClause.class);
      queryWhereClause.setQuery(options.getQuery());
      queryWhereClause.setValueTable(table);
      Initialisables.initialise(queryWhereClause);
      View view = View.Builder.newView(table.getName(), table).select(new AllClause()).where(queryWhereClause).build();
      viewsByName.put(entry.getKey(), view);
    }

    // replace original tables by corresponding views
    for (Map.Entry<String, View> entry : viewsByName.entrySet()) {
      tablesByName.put(entry.getKey(), entry.getValue());
    }
  }

  private Datasource getDatasourceByName(String datasourceName) {
    if (MagmaEngine.get().hasDatasource(datasourceName))
      return MagmaEngine.get().getDatasource(datasourceName);
    if (MagmaEngine.get().hasTransientDatasource(datasourceName))
      return MagmaEngine.get().getTransientDatasourceInstance(datasourceName);
    throw new NoSuchDatasourceException(datasourceName);
  }

  private boolean validateOptions() {
    return validateSourceOrTables() && validateSource() && validateDestination() &&
        validateTables() && validateSwitches();
  }

  private boolean validateSourceOrTables() {
    if (!options.isSource() && options.getTables() == null) {
      getShell().printf("%s\n", "Neither source nor table name(s) are specified.");
      return false;
    }
    return true;
  }

  private boolean validateSwitches() {
    if (options.getNoValues() && options.getNoVariables()) {
      getShell().printf("Must at least copy variables or values.\n");
      return false;
    }
    return true;
  }

  private boolean validateTables() {
    if (options.getTables() != null) {
      for (String tableName : options.getTables()) {
        MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
        try {
          resolver.resolveTable();
        } catch (NoSuchDatasourceException e) {
          getShell().printf("'%s' refers to an unknown datasource: '%s'.\n", tableName, resolver.getDatasourceName());
          return false;
        } catch (NoSuchValueTableException e) {
          getShell().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(),
              resolver.getDatasourceName());
          return false;
        }
      }
    }
    return true;
  }

  private boolean validateSource() {
    if (options.isSource()) {
      try {
        getDatasourceByName(options.getSource());
      } catch (NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        return false;
      }
    }
    return true;
  }

  private boolean validateDestination() {
    if (!options.isDestination() && !options.isOut()) {
      getShell().printf("Must provide either the 'destination' option or the 'out' option.\n");
      return false;
    }
    if (options.isDestination() && options.isOut()) {
      getShell().printf("The 'destination' option and the 'out' option are mutually exclusive.\n");
      return false;
    }
    if (options.isDestination()) {
      try {
        getDatasourceByName(options.getDestination());
      } catch (NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the decoded local file
   * NOTE: newer VFS versions encode the file path, decode path to remove escape characters.
   * @param path
   * @return new File instance
   * @throws UnsupportedEncodingException
   */
  private File getDecodedFile(String path) throws UnsupportedEncodingException {
    // Remove the possible escape characters that will cause errors when creating folders
    OpalGeneralConfig config = opalGeneralConfigService.getConfig();
    return new File(URLDecoder.decode(path, config.getDefaultCharacterSet()));
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
    } catch (FileSystemException e) {
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
    if (directory != null) {
      directory.createFolder();
    }

    if (Strings.isNullOrEmpty(outputFile.getName().getExtension())) {
      outputFile.createFolder();
    }

    if ("xls".equals(outputFile.getName().getExtension())) {
      getShell()
          .printf("WARNING: Writing to an Excel 97 spreadsheet. These are limited to 256 columns and 65536 rows " +
              "which may not be sufficient for writing large tables.\nUse an 'xlsx' extension to use Excel 2007 format " +
              "which supports 16K columns.\n");
    }
    return outputFile;
  }

  private void appendOption(StringBuilder sb, String option, boolean optionSpecified, String value) {
    if (optionSpecified) {
      sb.append(" --");
      sb.append(option);
      sb.append(' ');
      sb.append(value);
    }
  }

  private void appendFlag(StringBuilder sb, String flag, boolean value) {
    if (value) {
      sb.append(" --");
      sb.append(flag);
    }
  }

  private void appendUnparsedList(StringBuilder sb, Iterable<String> unparsedList) {
    for (String unparsed : unparsedList) {
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
      if (ds == null && next != null) {
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
      ds.addValueTable(table.getName(), variablesFile, dataFile, table.getEntityType());
      ds.setVariablesHeader(table.getName(), CsvUtil.getCsvVariableHeader(table));
    }

    protected void createFileIfNotExists(File f) throws IOException {
      if (!f.exists() && !f.createNewFile()) {
        throw new IllegalArgumentException("Unable to create the file: " + f);
      }
    }

  }

  class MultipleFileCsvDatasourceFactory extends CsvDatasourceFactory {

    @Nullable
    @Override
    protected Datasource internalCreateDatasource(FileObject outputFile) throws IOException {
      if (outputFile.getType() == FileType.FOLDER) {
        return getMultipleFileCsvDatasource(getDecodedFile(getLocalFile(outputFile).getPath()));
      }
      return null;
    }

    private Datasource getMultipleFileCsvDatasource(File directory) throws IOException {
      CsvDatasource ds = new CsvDatasource(directory.getName());
      ds.setMultilines(options.getMultilines());
      ds.setEntityIdNames(getEntityIdMap());
      ds.setEntityIdName(getDefaultEntityIdName());
      for (ValueTable table : getValueTables()) {
        File tableDir = new File(directory, table.getName());
        if (tableDir.exists() || tableDir.mkdir()) {
          File variablesFile = null;
          File dataFile = null;
          if (!options.getNoVariables()) {
            createFileIfNotExists(variablesFile = new File(tableDir, CsvDatasource.VARIABLES_FILE));
          }
          if (!options.getNoValues()) {
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
      if ("csv".equals(outputFile.getName().getExtension())) {
        return getSingleFileCsvDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }

    private Datasource getSingleFileCsvDatasource(String name, File csvFile) throws IOException {
      CsvDatasource ds = new CsvDatasource(name);
      ds.setMultilines(options.getMultilines());
      ds.setEntityIdNames(getEntityIdMap());
      ds.setEntityIdName(getDefaultEntityIdName());
      // one table only
      Set<ValueTable> tables = getValueTables();
      if (tables.size() > 1) {
        throw new IllegalArgumentException(
            "Only one table expected when writing to a CSV file. Provide a directory instead for copying several tables.");
      }

      if (!options.getNoVariables() && !options.getNoValues()) {
        throw new IllegalArgumentException(
            "Writing both variables and values in the same CSV file is not supported. Provide a directory instead.");
      }
      if (!options.getNoVariables()) {
        createFileIfNotExists(csvFile);
        addCsvValueTable(ds, tables.iterator().next(), csvFile, null);
      } else if (!options.getNoValues()) {
        createFileIfNotExists(csvFile);
        addCsvValueTable(ds, tables.iterator().next(), null, csvFile);
      }

      return ds;
    }

  }

  class ExcelDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(FileObject outputFile) {
      if (outputFile.getName().getExtension().startsWith("xls")) {
        return new ExcelDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }
  }

  class FsDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(FileObject outputFile) {
      if (outputFile.getName().getExtension().startsWith("zip")) {
        return new FsDatasource(outputFile.getName().getBaseName(), getLocalFile(outputFile));
      }
      return null;
    }
  }

  class NullDatasourceFactory extends FileDatasourceFactory {
    @Override
    public Datasource internalCreateDatasource(FileObject outputFile) {
      if ("/dev/null".equals(outputFile.getName().getPath())) {
        return new NullDatasource("/dev/null");
      }
      return null;
    }
  }

  class RHavenDatasourceFactory extends FileDatasourceFactory {
    @Nullable
    @Override
    protected Datasource internalCreateDatasource(FileObject outputFile) throws IOException {
      String ext = outputFile.getName().getExtension();
      if ("sav".equals(ext) || "zsav".equals(ext) || "sas7bdat".equals(ext) || "dta".equals(ext) || "xpt".equals(ext) || "rds".equals(ext)) {
        final RServerSession rSession = opalRSessionManager.newSubjectRSession();
        rSession.setExecutionContext("Export");
        RSessionHandler sessionHandler = new RSessionHandler() {
          @Override
          public ROperationTemplate getSession() {
            return rSession;
          }

          @Override
          public void onDispose() {
            opalRSessionManager.removeRSession(rSession.getId());
          }
        };
        List<File> outFiles = Lists.newArrayList();
        File outFile = getDecodedFile(getLocalFile(outputFile).getPath());
        FileUtil.delete(outFile);
        if (getValueTables().size() == 1) {
          outFiles.add(outFile);
        } else {
          String outputPath = outFile.getAbsolutePath().replaceAll("\\." + ext + "$", "");
          getValueTables().forEach(vt -> {
            outFiles.add(new File(outputPath, vt.getName() + "." + ext));
          });
        }
        getValueTables().size();
        RExportDatasource ds = new RExportDatasource(outputFile.getName().getBaseName(), sessionHandler, new RFileSymbolWriter(sessionHandler, outFiles));
        ds.setMultilines(options.getMultilines());
        ds.setEntityIdNames(getEntityIdMap());
        ds.setEntityIdName(getDefaultEntityIdName());
        return ds;
      }
      return null;
    }
  }

  private class CopyProgressListener implements DatasourceCopierProgressListener {

    private int currentPercentComplete = -1;

    private final int tableCount;

    private final Set<String> tables = Sets.newLinkedHashSet();

    private List<String> tablesWithPermission = Lists.newArrayList();

    private CopyProgressListener(int tableCount) {
      this.tableCount = tableCount;
    }

    @Override
    public void status(String table, long entitiesCopied, long entitiesToCopy, int percentComplete) {
      tables.add(table);
      int globalPercentComplete = ((tables.size() - 1) * 100 + percentComplete) / tableCount;
      if (globalPercentComplete != currentPercentComplete) {
        getShell().progress(table, entitiesCopied, entitiesToCopy, globalPercentComplete);
        currentPercentComplete = globalPercentComplete;
      }
      if (!tablesWithPermission.contains(table)) {
        String node = "/datasource/" + options.getDestination() + "/table/" + table;
        subjectAclService.addSubjectPermission("opal", node, SubjectAcl.SubjectType.USER.subjectFor(getOwner()), Opal.AclAction.TABLE_ALL.name());
        tablesWithPermission.add(table);
      }
      if (percentComplete == 100) {
        eventBus.post(new ValueTableAddedEvent(options.getDestination(), table));
      }
    }
  }
}
