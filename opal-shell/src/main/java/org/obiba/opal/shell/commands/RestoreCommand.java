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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.protobuf.ExtensionRegistry;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.commons.vfs2.FileObject;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.*;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.service.DataImportService;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.service.ProjectsState;
import org.obiba.opal.core.service.ProjectsState.State;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.shell.commands.options.RestoreCommandOptions;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.core.security.DatasourcePermissionConverter;
import org.obiba.opal.core.security.ProjectPermissionConverter;
import org.obiba.opal.core.security.ProjectResourceReferencePermissionConverter;
import org.obiba.opal.core.security.ProjectResourceReferencesPermissionConverter;
import org.obiba.opal.core.security.TablePermissionConverter;
import org.obiba.opal.core.security.VCFStorePermissionsConverter;
import org.obiba.opal.core.security.VariablePermissionConverter;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CommandUsage(description = "Restore a project's data and, optionally, its permissions.", syntax = "Syntax: restore --project PROJECT --archive FILE [--password PASSWORD] [--override BOOL] [--permissions BOOL]")
public class RestoreCommand extends AbstractBackupRestoreCommand<RestoreCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(RestoreCommand.class);

  /**
   * The permission nodes a project backup may hold: the project itself, its resources and VCF store, and its
   * datasource, tables, views and variables. Group 1 is the node type, group 3 what follows the project name.
   */
  private static final Pattern PROJECT_NODE = Pattern.compile(
      "^/(project|datasource)/([^/]+)(|/resources|/vcf-store|/resource/[^/]+|/(?:table|view)/[^/]+(?:/variable/.+)?)$");

  private static final String WORK_DIR = System.getProperty("OPAL_HOME") + File.separator + "work" + File.separator + "tmp";

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private ProjectsState projectsState;

  @Autowired
  private DataImportService dataImportService;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private ViewDtos viewDtos;

  @Autowired
  private ResourceReferenceService resourceReferenceService;

  @Autowired
  private SubjectAclService subjectAclService;

  private File archiveFolder;

  private File workDir;

  @Override
  public int execute() {
    int errorCode = CommandResultCode.CRITICAL_ERROR; // initialize as non-zero (error)
    String projectName = getOptions().getProject();
    File archiveFolder = getArchiveFolder();

    Stopwatch stopwatch = Stopwatch.createStarted();

    if (archiveFolder.exists()) {
      Project project = projectRepository.findByName(projectName).orElse(null);
      if (project == null) {
        getShell().printf("An empty project must be created before restoring its content");
      } else {
        log.debug("Restore of {} started", projectName);
        try {
          projectsState.updateProjectState(projectName, State.BUSY);
          restoreTables();
          restoreViews();
          restoreResources();
          restoreFiles();
          // last, as replacing a table removes its permissions
          if (getOptions().getPermissions()) restorePermissions();
          errorCode = CommandResultCode.SUCCESS;
        } catch (Exception e) {
          if (!Strings.isNullOrEmpty(e.getMessage())) getShell().printf("%s\n", e.getMessage());
          log.error("Task failed", e);
        } finally {
          projectsState.updateProjectState(projectName, State.READY);
        }
      }
    } else {
      getShell().printf("Backup archive to restore cannot be found");
    }

    if (errorCode == CommandResultCode.SUCCESS)
      log.info("Restore of {} done in {}", projectName, stopwatch.stop());
    else
      log.warn("Restore of {} failed in {}", projectName, stopwatch.stop());

    if (workDir != null) {
      try {
        FileUtil.delete(workDir);
      } catch (IOException e) {
        log.warn("Cannot delete temp directory: {}", workDir.getAbsolutePath(), e);
      }
    }

    return errorCode;
  }

  @Override
  protected File getArchiveFolder() {
    if (archiveFolder != null) return archiveFolder;
    try {
      // Get the file specified on the command line.
      FileObject archiveFile = getFile(getOptions().getArchive());
      File archive = getLocalFile(archiveFile);
      if (archive.isDirectory()) {
        archiveFolder = archive;
        return archiveFolder;
      } else if (archive.getName().endsWith(".zip")) {
        // uncompress in the opal's work directory
        workDir = new File(WORK_DIR, new Random().nextInt(20) + "");
        workDir.mkdirs();
        log.debug("Uncompresing archive {}", getOptions().getArchive());
        FileUtil.unzip(archive, workDir, getOptions().getPassword());
        File[] dirs = workDir.listFiles(file -> file.isDirectory());
        // supposed to be one directory
        if (dirs != null) {
          archiveFolder = dirs[0];
          return archiveFolder;
        }
        log.error("Project archive does not seem to be a valid backup folder");
      }
    } catch (Exception e) {
      log.error("There was an error accessing the archive folder", e);
      throw new RuntimeException("There was an error accessing the archive folder", e);
    }
    throw new RuntimeException("Not a valid project archive file, expecting a folder or a zip file: " + getOptions().getArchive());
  }

  @Override
  protected String getProjectName() {
    return getOptions().getProject();
  }

  @Override
  protected boolean isReadOnly() {
    return true;
  }

  //
  // Private methods
  //

  private void restoreTables() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Restore of {} tables started", getProjectName());
    File tablesFolder = getTablesFolder();
    if (tablesFolder.exists()) {
      File[] tableFolders = tablesFolder.listFiles(file -> file.isDirectory() && new File(file, "table.json").exists());
      if (tableFolders != null) {
        // restore meta data
        Map<String, File> tableFoldersMap = Maps.newHashMap();
        for (File tableFolder : tableFolders) {
          File tableDtoFile = new File(tableFolder, "table.json");
          try (FileReader reader = new FileReader(tableDtoFile)) {
            Magma.TableDto.Builder tableDtoBuilder = Magma.TableDto.newBuilder();
            JsonFormat.merge(reader, tableDtoBuilder);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
              @Override
              protected void doInTransactionWithoutResult(TransactionStatus status) {
                writeVariablesToTable(MagmaEngine.get().getDatasource(getProjectName()), tableDtoBuilder.build());
              }
            });
            tableFoldersMap.put(tableDtoBuilder.getName(), tableFolder);
          } catch (IOException e) {
            log.error("Table restore failed: {}", tableDtoFile.getAbsolutePath(), e);
            throw new RuntimeException("Table restore failed", e);
          }
        }
        // restore data
        Datasource sourceDatasource = null;
        try {
          sourceDatasource = getSourceDatasource(tableFoldersMap);
          dataImportService.importData(sourceDatasource.getValueTables(), getProjectName(), null, true, true,
              new ImportProgressListener(sourceDatasource.getValueTables().size()));
        } catch (Exception e) {
          log.error("Tables restore failed", e);
          throw new RuntimeException("Tables restore failed", e);
        } finally {
          if (sourceDatasource != null)
            Disposables.dispose(sourceDatasource);
        }
      }
    }
    log.info("Restore of {} tables done in {}", getProjectName(), stopwatch.stop());
  }

  private void restoreViews() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Restore of {} views started", getProjectName());
    File viewsFolder = getViewsFolder();
    if (viewsFolder.exists()) {
      File[] viewFiles = viewsFolder.listFiles(file -> !file.isDirectory() && file.getName().endsWith(".json"));
      if (viewFiles != null) {
        for (File viewDtoFile : viewFiles) {
          try (FileReader reader = new FileReader(viewDtoFile)) {
            Magma.ViewDto.Builder viewDtoBuilder = Magma.ViewDto.newBuilder();
            ExtensionRegistry registry = ExtensionRegistry.newInstance();
            Magma.registerAllExtensions(registry);
            JsonFormat.merge(reader, registry, viewDtoBuilder);
            ValueView view = viewDtos.fromDto(viewDtoBuilder.build());
            removeTableOrView(view.getName());
            viewManager.addView(getProjectName(), view, "Restored", null);
          } catch (IOException e) {
            log.error("View restore failed: {}", viewDtoFile.getAbsolutePath(), e);
            throw new RuntimeException("Table restore failed", e);
          }
        }
      }
    }
    log.info("Restore of {} views done in {}", getProjectName(), stopwatch.stop());
  }

  private void restoreResources() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Restore of {} resources started", getProjectName());
    File resourcesFolder = getResourcesFolder();
    if (resourcesFolder.exists()) {
      File[] resourceFiles = resourcesFolder.listFiles(file -> !file.isDirectory() && file.getName().endsWith(".json"));
      if (resourceFiles != null) {
        for (File resourceDtoFile : resourceFiles) {
          try (FileReader reader = new FileReader(resourceDtoFile)) {
            Projects.ResourceReferenceDto.Builder builder = Projects.ResourceReferenceDto.newBuilder();
            ExtensionRegistry registry = ExtensionRegistry.newInstance();
            Projects.registerAllExtensions(registry);
            JsonFormat.merge(reader, registry, builder);
            ResourceReference reference = org.obiba.opal.web.project.Dtos.fromDto(builder.build());
            reference.setProject(getProjectName());
            if (resourceReferenceService.hasResourceReference(reference.getProject(), reference.getName()) && !getOptions().getOverride()) {
              throw new RuntimeException("Resource already exists, use override option: " + reference.getName());
            }
            resourceReferenceService.save(reference);
          } catch (IOException e) {
            log.error("Resource restore failed: {}", resourceDtoFile.getAbsolutePath(), e);
            throw new RuntimeException("Resource restore failed", e);
          }
        }
      }
    }
    log.info("Restore of {} resources done in {}", getProjectName(), stopwatch.stop());
  }

  private void restoreFiles() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Restore of {} files started", getProjectName());
    File filesFolder = getFilesFolder();
    if (filesFolder.exists()) {
      try {
        FileUtil.copyDirectory(filesFolder, getProjectFolder());
      } catch (IOException e) {
        log.error("Files restore failed: {}", filesFolder.getAbsolutePath(), e);
        throw new RuntimeException("Files restore failed", e);
      }
    }
    log.info("Restore of {} files done in {}", getProjectName(), stopwatch.stop());
  }

  private void restorePermissions() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.debug("Restore of {} permissions started", getProjectName());
    File permissionsFile = getPermissionsFile();
    if (!permissionsFile.exists()) {
      getShell().printf("No permissions to restore.\n");
      return;
    }
    // read and check them all before writing any, so that a bad file changes nothing
    List<Opal.Acl> acls = Lists.newArrayList();
    try {
      JSONArray array = new JSONArray(Files.readString(permissionsFile.toPath(), StandardCharsets.UTF_8));
      for (int i = 0; i < array.length(); i++) {
        Opal.Acl.Builder builder = Opal.Acl.newBuilder();
        JsonFormat.merge(array.getJSONObject(i).toString(), builder);
        Opal.Acl acl = builder.build();
        String node = PERMISSIONS_DOMAIN.equals(acl.getDomain()) ? toProjectNode(acl.getResource(), getProjectName()) : null;
        if (node == null) {
          log.warn("Skipping permission outside of the project scope: {} {}", acl.getDomain(), acl.getResource());
        } else if (!isAllowed(node, acl.getActionsList())) {
          log.warn("Skipping permission not applicable to {}: {}", node, acl.getActionsList());
        } else if (!isRestoredItem(node)) {
          log.warn("Skipping permission on an item that is not in the project: {}", node);
        } else {
          acls.add(acl.toBuilder().setResource(node).build());
        }
      }
    } catch (Exception e) {
      log.error("Permissions restore failed: {}", permissionsFile.getAbsolutePath(), e);
      throw new RuntimeException("Permissions restore failed", e);
    }
    // added to the existing ones: replacing them could take away the access of the one restoring
    for (Opal.Acl acl : acls) {
      SubjectAcl.Subject subject = SubjectAcl.SubjectType.valueOf(acl.getSubject().getType().name())
          .subjectFor(acl.getSubject().getPrincipal());
      subjectAclService.addSubjectPermissions(PERMISSIONS_DOMAIN, acl.getResource(), subject, acl.getActionsList());
    }
    getShell().printf("Successful restore of " + acls.size() + " permissions.\n");
    log.info("Restore of {} permissions done in {}", getProjectName(), stopwatch.stop());
  }

  /**
   * Check that the table, view, variable or resource that a permission applies to is in the restored project.
   */
  private boolean isRestoredItem(String node) {
    if (node.startsWith("/datasource/")) return isDatasourceItem(node);
    String resourcePrefix = "/project/" + getProjectName() + "/resource/";
    if (node.startsWith(resourcePrefix))
      return resourceReferenceService.hasResourceReference(getProjectName(), node.substring(resourcePrefix.length()));
    return true;
  }

  /**
   * Re-target a backed up permission node to the project being restored.
   *
   * @param node the backed up node
   * @param project the name of the project being restored
   * @return the node in the restored project, or null if the node is not one a project backup may hold
   */
  static String toProjectNode(String node, String project) {
    Matcher matcher = PROJECT_NODE.matcher(node);
    if (!matcher.matches()) return null;
    return "/" + matcher.group(1) + "/" + project + matcher.group(3);
  }

  /**
   * Whether the permissions are ones that apply to the node. Permissions are converted to REST grants by name alone,
   * whatever their node, so a permission of another kind (SYSTEM_ALL for instance) must not be restored on a project node.
   *
   * @param node a project node, as returned by {@link #toProjectNode(String, String)}
   * @param permissions the permission names
   */
  static boolean isAllowed(String node, List<String> permissions) {
    Matcher matcher = PROJECT_NODE.matcher(node);
    if (!matcher.matches() || permissions.isEmpty()) return false;
    String suffix = matcher.group(3);
    Enum<?>[] allowed;
    if ("project".equals(matcher.group(1))) {
      if (suffix.isEmpty()) allowed = ProjectPermissionConverter.Permission.values();
      else if (suffix.equals("/resources")) allowed = ProjectResourceReferencesPermissionConverter.Permission.values();
      else if (suffix.equals("/vcf-store")) allowed = VCFStorePermissionsConverter.Permission.values();
      else allowed = ProjectResourceReferencePermissionConverter.Permission.values();
    } else {
      if (suffix.isEmpty()) allowed = DatasourcePermissionConverter.Permission.values();
      else if (suffix.matches("/(?:table|view)/[^/]+")) allowed = TablePermissionConverter.Permission.values();
      else allowed = VariablePermissionConverter.Permission.values();
    }
    Set<String> names = Arrays.stream(allowed).map(Enum::name).collect(Collectors.toSet());
    return names.containsAll(permissions);
  }

  private Datasource getSourceDatasource(Map<String, File> tableFoldersMap) {
    CsvDatasource sourceDatasource = new CsvDatasource("tables");
    Datasource datasource = MagmaEngine.get().getDatasource(getProjectName());
    for (String tableName : tableFoldersMap.keySet()) {
      sourceDatasource.addValueTable(datasource.getValueTable(tableName), new File(tableFoldersMap.get(tableName), CsvDatasource.DATA_FILE));
    }
    Initialisables.initialise(sourceDatasource);
    return sourceDatasource;
  }

  private void writeVariablesToTable(Datasource datasource, Magma.TableDto table) {
    removeTableOrView(table.getName());
    try (ValueTableWriter.VariableWriter variableWriter = datasource.createWriter(table.getName(), table.getEntityType())
        .writeVariables()) {
      for (Magma.VariableDto dto : table.getVariablesList()) {
        variableWriter.writeVariable(Dtos.fromDto(dto));
      }
    }
  }

  private void removeTableOrView(String name) {
    if (viewManager.hasView(getProjectName(), name)) {
      if (getOptions().getOverride()) {
        log.warn("Removing view {}", name);
        viewManager.removeView(getProjectName(), name);
      } else {
        throw new RuntimeException("View already exists, use override option: " + name);
      }
    } else {
      Datasource datasource = MagmaEngine.get().getDatasource(getProjectName());
      if (datasource.hasValueTable(name)) {
        if (getOptions().getOverride()) {
          log.warn("Dropping table {}", name);
          datasource.dropTable(name);
        } else {
          throw new RuntimeException("Table already exists, use override option: " + name);
        }
      }
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("restore");
    sb.append(" --project ").append(options.getProject());
    sb.append(" --archive ").append(options.getArchive());

    if (options.isPassword()) {
      sb.append(" --password *****");
    }
    if (options.isOverride()) {
      sb.append(" --override ").append(options.getOverride());
    }
    if (options.isPermissions()) {
      sb.append(" --permissions ").append(options.getPermissions());
    }

    return sb.toString();
  }

  private class ImportProgressListener implements DatasourceCopierProgressListener {

    private int currentPercentComplete = -1;

    private final int tableCount;

    private final Set<String> tables = Sets.newLinkedHashSet();

    public ImportProgressListener(int tableCount) {
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
