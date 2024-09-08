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

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.commons.vfs2.FileObject;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.csv.support.CsvUtil;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.ProjectsState;
import org.obiba.opal.core.service.ProjectsState.State;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.shell.commands.options.BackupCommandOptions;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.project.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@CommandUsage(description = "Backup a project's data.", syntax = "Syntax: backup --project PROJECT --archive FILE [--viewsAsTables BOOL] [--override BOOL]")
public class BackupCommand extends AbstractBackupRestoreCommand<BackupCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(BackupCommand.class);

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DataExportService dataExportService;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private ProjectsState projectsState;

  @Autowired
  private ViewDtos viewDtos;

  @Autowired
  private ResourceReferenceService resourceReferenceService;

  private File archiveFolder;

  @Override
  public int execute() {
    int errorCode = CommandResultCode.CRITICAL_ERROR; // initialize as non-zero (error)
    String projectName = getProjectName();
    String archivePath = getOptions().getArchive();
    String projectPath = "/projects/" + projectName;

    Stopwatch stopwatch = Stopwatch.createStarted();
    if (archivePath.equals(projectPath) || archivePath.startsWith(projectPath + "/")) {
      getShell().printf("Backup archive must not be in the project's folder");
    } else {
      log.debug("Backup of {} started", projectName);
      try {
        Project project = orientDbService.findUnique(new Project(projectName));
        projectsState.updateProjectState(projectName, State.BUSY);
        getArchiveFolder();
        if (project != null && MagmaEngine.get().hasDatasource(project.getName())) {
          backupTables();
          backupViews();
          backupResources();
          backupFiles();
        }
        errorCode = CommandResultCode.SUCCESS;
      } catch (Exception e) {
        if (!Strings.isNullOrEmpty(e.getMessage())) getShell().printf("%s\n", e.getMessage());
        log.error("Task failed", e);
      } finally {
        projectsState.updateProjectState(projectName, State.READY);
      }
    }

    if (errorCode == CommandResultCode.SUCCESS)
      log.info("Backup of {} done in {}", projectName, stopwatch.stop());
    else
      log.warn("Backup of {} failed in {}", projectName, stopwatch.stop());

    return errorCode;
  }

  @Override
  protected String getProjectName() {
    return getOptions().getProject();
  }

  @Override
  protected boolean isReadOnly() {
    return false;
  }

  @Override
  protected File getArchiveFolder() {
    if (archiveFolder != null) return archiveFolder;
    try {
      // Get the file specified on the command line.
      FileObject archiveFile = getFile(getOptions().getArchive());
      archiveFolder = getLocalFile(archiveFile);
      if (archiveFolder.exists()) {
        if (getOptions().getOverride()) {
          log.warn("Deleting the existing archive {}", getOptions().getArchive());
          FileUtil.delete(archiveFolder);
        } else {
          throw new RuntimeException("Backup archive already exists, use override option");
        }
      } else if (!archiveFolder.mkdirs()) {
        throw new RuntimeException("Backup archive folder creation failed");
      }
      return archiveFolder;
    } catch (IOException e) {
      log.error("There was an error accessing the archive folder", e);
      throw new RuntimeException("There was an error accessing the archive folder", e);
    }
  }

  //
  // Private methods
  //

  private void backupTables() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} tables started", getProjectName());
    Set<ValueTable> tables = getDatasource().getValueTables().stream()
        .filter(table -> getOptions().getViewsAsTables() || !table.isView())
        .collect(Collectors.toSet());

    if (tables.isEmpty()) {
      getShell().printf("No tables to backup.\n");
    } else {
      getShell().printf("Backup of " + tables.size() + " tables.\n");
      Datasource destinationDatasource = null;
      try {
        destinationDatasource = getDestinationDatasource(tables);
        DatasourceCopier.Builder builder = dataExportService.newCopier(destinationDatasource).dontCopyMetadata();
        dataExportService.exportTablesToDatasource(null, tables, destinationDatasource, builder, false, new BackupProgressListener(tables.size()));
      } catch (Exception e) {
        log.error("Tables backup failed", e);
        throw new RuntimeException("Tables backup failed", e);
      } finally {
        if (destinationDatasource != null)
          Disposables.dispose(destinationDatasource);
      }
      getShell().printf("Successful backup of all tables.\n");
    }
    log.info("Backup of {} tables done in {}", getProjectName(), stopwatch.stop());
  }

  private void backupViews() {
    if (getOptions().getViewsAsTables()) return;
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} views started", getProjectName());
    List<Magma.ViewDto> views = getDatasource().getValueTables().stream()
        .filter(ValueTable::isView)
        .map(valueTable -> viewDtos.asDto(viewManager.getView(valueTable.getDatasource().getName(), valueTable.getName()))
            .toBuilder().clearStatus().build())
        .collect(Collectors.toList());

    if (views.isEmpty()) {
      getShell().printf("No views to backup.\n");
    } else {
      getShell().printf("Backup of " + views.size() + " views.\n");
      File viewsFolder = getViewsFolder();
      if (!viewsFolder.exists()) viewsFolder.mkdirs();
      for (Magma.ViewDto viewDto : views) {
        String viewFileName = viewDto.getName() + ".json";
        File viewFile = new File(viewsFolder, viewFileName);
        try {
          writeDto(viewFile, viewDto);
        } catch (Exception e) {
          log.error("View backup failed: {}", viewFile.getAbsolutePath(), e);
          throw new RuntimeException("View backup failed", e);
        }
      }
      getShell().printf("Successful backup of all views.\n");
    }
    log.info("Backup of {} views done in {}", getProjectName(), stopwatch.stop());
  }

  private void backupResources() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} resources started", getProjectName());
    List<ResourceReference> resourceReferences = resourceReferenceService.getResourceReferences(getProjectName());
    if (resourceReferences.isEmpty()) {
      getShell().printf("No resources to backup.\n");
    } else {
      getShell().printf("Backup of " + resourceReferences.size() + " resources.\n");
      File resourcesFolder = getResourcesFolder();
      if (!resourcesFolder.exists()) resourcesFolder.mkdirs();
      for (ResourceReference ref : resourceReferences) {
        Projects.ResourceReferenceDto refDto = Dtos.asDto(ref, null, true);
        String refFileName = refDto.getName() + ".json";
        File refFile = new File(resourcesFolder, refFileName);
        try {
          writeDto(refFile, refDto);
        } catch (Exception e) {
          log.error("Resource backup failed: {}", refFile.getAbsolutePath(), e);
          throw new RuntimeException("Resource backup failed", e);
        }
      }
      getShell().printf("Successful backup of all resources.\n");
    }
    log.info("Backup of {} resources done in {}", getProjectName(), stopwatch.stop());
  }

  private void backupFiles() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} files started", getProjectName());
    File projectFolder = getProjectFolder();
    if (projectFolder.exists()) {
      File filesFolder = getFilesFolder();
      try {
        filesFolder.mkdirs();
        FileUtil.copyDirectory(projectFolder, filesFolder);
      } catch (IOException e) {
        log.error("Files backup failed: {}", filesFolder.getAbsolutePath(), e);
        throw new RuntimeException("Files backup failed", e);
      }
      getShell().printf("Successful backup of all files.\n");
    }
    log.info("Backup of {} files done in {}", getProjectName(), stopwatch.stop());
  }

  private void writeDto(File file, Message message) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(JsonFormat.printToString(message));
      writer.flush();
    }
  }

  private Datasource getDatasource() {
    return MagmaEngine.get().getDatasource(getProjectName());
  }

  private Datasource getDestinationDatasource(Set<ValueTable> tables) throws IOException {
    CsvDatasource destinationDatasource = new CsvDatasource("tables");
    for (ValueTable table : tables) {
      File tableFolder = getTableFolder(table.getName());
      Magma.TableDto.Builder tableDtoBuilder = transactionTemplate.execute(status -> org.obiba.opal.web.magma.Dtos.asDto(table, false));
      tableDtoBuilder.clearLink();
      StreamSupport.stream(table.getVariables().spliterator(), false)
          .map(org.obiba.opal.web.magma.Dtos::asDto)
          .forEach(tableDtoBuilder::addVariables);
      File tableFile = new File(getTableFolder(table.getName()), "table.json");
      try {
        writeDto(tableFile, tableDtoBuilder.build());
      } catch (Exception e) {
        log.error("Table backup failed: {}", tableFile.getAbsolutePath(), e);
        throw new RuntimeException("Table backup failed", e);
      }

      File dataFile = new File(tableFolder, CsvDatasource.DATA_FILE);
      dataFile.createNewFile();
      destinationDatasource.addValueTable(table.getName(), null, dataFile, table.getEntityType());
      destinationDatasource.setVariablesHeader(table.getName(), CsvUtil.getCsvVariableHeader(table));
    }
    Initialisables.initialise(destinationDatasource);
    return destinationDatasource;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("backup");
    sb.append(" --project ").append(options.getProject());
    sb.append(" --archive ").append(options.getArchive());

    if (options.isViewsAsTables()) {
      sb.append(" --viewsAsTables ").append(options.getViewsAsTables());
    }
    if (options.isOverride()) {
      sb.append(" --override ").append(options.getOverride());
    }

    return sb.toString();
  }


  private class BackupProgressListener implements DatasourceCopierProgressListener {

    private int currentPercentComplete = -1;

    private final int tableCount;

    private final Set<String> tables = Sets.newLinkedHashSet();

    private BackupProgressListener(int tableCount) {
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
    }
  }

}
