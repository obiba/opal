/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.obiba.opal.core.domain.*;
import org.obiba.opal.core.domain.ProjectsState.State;
import org.obiba.opal.core.service.*;
import org.obiba.opal.shell.commands.options.BackupCommandOptions;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.project.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@CommandUsage(description = "Backup a project's data.", syntax = "Syntax: backup --project PROJECT --archive FILE")
public class BackupCommand extends AbstractOpalRuntimeDependentCommand<BackupCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(BackupCommand.class);

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

  @Autowired
  private OpalAnalysisService analysisService;

  @Autowired
  private ReportTemplateService reportTemplateService;

  @Override
  public int execute() {
    int errorCode = CommandResultCode.CRITICAL_ERROR; // initialize as non-zero (error)
    String projectName = getOptions().getProject();
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
        File archiveFolder = getArchiveFolder();
        if (archiveFolder.exists()) {
          log.warn("Deleting the existing archive {}", archivePath);
          FileUtil.delete(archiveFolder);
        }
        if (project != null && MagmaEngine.get().hasDatasource(project.getName())) {
          backupTables();
          backupViews();
          backupResources();
          backupFiles();
          backupAnalyses();
          backupReports();
        }
        errorCode = CommandResultCode.SUCCESS;
      } catch (Exception e) {
        if (!Strings.isNullOrEmpty(e.getMessage())) getShell().printf("%s\n", e.getMessage());
        //noinspection UseOfSystemOutOrSystemErr
        e.printStackTrace(System.err);
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

  private void backupTables() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} tables started", getOptions().getProject());
    Set<ValueTable> tables = getDatasource().getValueTables().stream()
        .filter(table -> !table.isView())
        .collect(Collectors.toSet());

    if (tables.isEmpty()) {
      getShell().printf("No tables to backup.\n");
    } else {
      getShell().printf("Backup of " + tables.size() + " tables.\n");
      Datasource destinationDatasource = null;
      try {
        destinationDatasource = getDestinationDatasource(tables);
        DatasourceCopier.Builder builder = dataExportService.newCopier(destinationDatasource).dontCopyMetadata();
        dataExportService.exportTablesToDatasource(null, tables, destinationDatasource, builder, false, new BackupProgressListener());
      } catch (Exception e) {
        log.error("Tables backup failed", e);
        throw new RuntimeException("Tables backup failed", e);
      } finally {
        if (destinationDatasource != null)
          Disposables.dispose(destinationDatasource);
      }
      getShell().printf("Successful backup of all tables.\n");
    }
    log.info("Backup of {} tables done in {}", getOptions().getProject(), stopwatch.stop());
  }

  private void backupViews() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} views started", getOptions().getProject());
    List<Magma.ViewDto> views = getDatasource().getValueTables().stream()
        .filter(ValueTable::isView)
        .map(valueTable -> viewDtos.asDto(viewManager.getView(valueTable.getDatasource().getName(), valueTable.getName())))
        .collect(Collectors.toList());

    if (views.isEmpty()) {
      getShell().printf("No views to backup.\n");
    } else {
      getShell().printf("Backup of " + views.size() + " views.\n");
      File viewsFolder = new File(getArchiveFolder(), "views");
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
    log.info("Backup of {} views done in {}", getOptions().getProject(), stopwatch.stop());
  }

  private void backupResources() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} resources started", getOptions().getProject());
    List<ResourceReference> resourceReferences = resourceReferenceService.getResourceReferences(getOptions().getProject());
    if (resourceReferences.isEmpty()) {
      getShell().printf("No resources to backup.\n");
    } else {
      getShell().printf("Backup of " + resourceReferences.size() + " resources.\n");
      File resourcesFolder = new File(getArchiveFolder(), "resources");
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
    log.info("Backup of {} resources done in {}", getOptions().getProject(), stopwatch.stop());
  }

  private void backupFiles() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} files started", getOptions().getProject());
    File projectFolder = new File(getLocalFile(getFileSystemRoot()), "projects" + "/" + getOptions().getProject());
    if (projectFolder.exists()) {
      File filesFolder = new File(getArchiveFolder(), "files");
      try {
        filesFolder.mkdirs();
        FileUtil.copyDirectory(projectFolder, filesFolder);
      } catch (IOException e) {
        log.error("Files backup failed: {}", filesFolder.getAbsolutePath(), e);
        throw new RuntimeException("Files backup failed", e);
      }
      getShell().printf("Successful backup of all files.\n");
    }
    log.info("Backup of {} files done in {}", getOptions().getProject(), stopwatch.stop());
  }

  private void backupAnalyses() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} analyses started", getOptions().getProject());
    List<OpalAnalysis> analyses = StreamSupport.stream(analysisService.getAnalysesByDatasource(getOptions().getProject()).spliterator(), false)
        .collect(Collectors.toList());
    if (!analyses.isEmpty()) {
      File analysesFolder = new File(getArchiveFolder(), "analyses");
      analysesFolder.mkdirs();
      for (OpalAnalysis analysis : analyses) {
        Projects.OpalAnalysisDto analysisDto = Dtos.asDto(analysis).build();
        File analysisFile = new File(analysesFolder, analysis.getName());
        try {
          writeDto(analysisFile, analysisDto);
        } catch (IOException e) {
          log.error("Analysis backup failed: {}", analysisFile.getAbsolutePath(), e);
          throw new RuntimeException("Analysis backup failed", e);
        }
      }
      getShell().printf("Successful backup of all analyses.\n");
    }
    log.info("Backup of {} analyses done in {}", getOptions().getProject(), stopwatch.stop());
  }

  private void backupReports() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Backup of {} reports started", getOptions().getProject());
    List<ReportTemplate> reportTemplates = StreamSupport.stream(reportTemplateService.getReportTemplates(getOptions().getProject()).spliterator(), false)
        .collect(Collectors.toList());
    if (!reportTemplates.isEmpty()) {
      File reportsFolder = new File(getArchiveFolder(), "reports");
      reportsFolder.mkdirs();
      for (ReportTemplate reportTemplate : reportTemplates) {
        Opal.ReportTemplateDto reportTemplateDto = org.obiba.opal.web.reporting.Dtos.asDto(reportTemplate);
        File reportTemplateFile = new File(reportsFolder, reportTemplate.getName());
        try {
          writeDto(reportTemplateFile, reportTemplateDto);
        } catch (IOException e) {
          log.error("Report backup failed: {}", reportTemplateFile.getAbsolutePath(), e);
          throw new RuntimeException("Report backup failed", e);
        }
      }
      getShell().printf("Successful backup of all report templates.\n");
    }
    log.info("Backup of {} reports done in {}", getOptions().getProject(), stopwatch.stop());
  }

  private void writeDto(File file, Message message) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(JsonFormat.printToString(message));
      writer.flush();
    }
  }

  private Datasource getDatasource() {
    return MagmaEngine.get().getDatasource(getOptions().getProject());
  }

  private Datasource getDestinationDatasource(Set<ValueTable> tables) throws IOException {
    CsvDatasource destinationDatasource = new CsvDatasource("tables", getTablesFolder());
    for (ValueTable table : tables) {
      File tableFolder = getTableFolder(table.getName());
      Magma.TableDto.Builder tableDtoBuilder = org.obiba.opal.web.magma.Dtos.asDto(table, false);
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

  private File getTablesFolder() {
    File tablesFolder = new File(getArchiveFolder(), "tables");
    tablesFolder.mkdirs();
    return tablesFolder;
  }

  private File getTableFolder(String tableName) {
    File tableFolder = new File(getTablesFolder(), tableName);
    tableFolder.mkdirs();
    return tableFolder;
  }

  private File getArchiveFolder() {
    try {
      // Get the file specified on the command line.
      FileObject archiveFile = getFile(options.getArchive());
      archiveFile.createFolder();
      return getLocalFile(archiveFile);
    } catch (Exception e) {
      log.error("There was an error accessing the archive folder", e);
      throw new RuntimeException("There was an error accessing the archive folder", e);
    }
  }

  private class BackupProgressListener implements DatasourceCopierProgressListener {

    private int currentPercentComplete = -1;

    @Override
    public void status(String table, long entitiesCopied, long entitiesToCopy, int percentComplete) {
      if (percentComplete != currentPercentComplete) {
        getShell().progress(table, entitiesCopied, entitiesToCopy, percentComplete);
        currentPercentComplete = percentComplete;
      }
    }
  }

}
