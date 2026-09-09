/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.*;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.event.ValueTableAddedEvent;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.event.ValueTableEvent;
import org.obiba.opal.core.event.VariableDeletedEvent;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.event.ResourceProvidersServiceStartedEvent;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Strings.nullToEmpty;

@Component
public class ProjectsServiceImpl implements ProjectService {

  private static final String PROJECTS_DIR = "projects";

  private static final Logger log = LoggerFactory.getLogger(ProjectsServiceImpl.class);

  private final OpalFileSystemService opalFileSystemService;

  private final ProjectRepository projectRepository;

  private final DatabaseRegistry databaseRegistry;

  private final ProjectDatabaseService projectDatabaseService;

  private final ProjectsKeyStoreService projectsKeyStoreService;

  private final IdentifiersTableService identifiersTableService;

  private final ViewManager viewManager;

  private final TransactionTemplate transactionTemplate;

  private final EventBus eventBus;

  private final ResourceReferenceService resourceReferenceService;

  private final ProjectsState projectsState;

  private final DatasourceLoaderService datasourceLoaderService;

  @Autowired
  public ProjectsServiceImpl(OpalFileSystemService opalFileSystemService,
                             ProjectRepository projectRepository,
                             DatabaseRegistry databaseRegistry,
                             ProjectDatabaseService projectDatabaseService,
                             ProjectsKeyStoreService projectsKeyStoreService,
                             IdentifiersTableService identifiersTableService,
                             ViewManager viewManager,
                             TransactionTemplate transactionTemplate,
                             EventBus eventBus, ResourceReferenceService resourceReferenceService,
                             ProjectsState projectsState, DatasourceLoaderService datasourceLoaderService) {
    this.opalFileSystemService = opalFileSystemService;
    this.projectRepository = projectRepository;
    this.databaseRegistry = databaseRegistry;
    this.projectDatabaseService = projectDatabaseService;
    this.projectsKeyStoreService = projectsKeyStoreService;
    this.identifiersTableService = identifiersTableService;
    this.viewManager = viewManager;
    this.transactionTemplate = transactionTemplate;
    this.eventBus = eventBus;
    this.resourceReferenceService = resourceReferenceService;
    this.projectsState = projectsState;
    this.datasourceLoaderService = datasourceLoaderService;
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {
  }

  @Override
  public void initialize() {
    // finish what an interrupted project deletion began, and say what is left over that nothing refers to
    projectDatabaseService.deletePendingFolders();
    reportOrphanProjectDatabaseFolders();

    // In the @PostConstruct there is no way to ensure that all the post processing is already done,
    // so (indeed) there can be no Transactions. The only way to ensure that that is working is by using a TransactionTemplate.
    // Add all project datasources to MagmaEngine
    for (Project project : getProjects()) {
      if (!project.isArchived()) {
        try {
          registerDatasource(project);
        } catch (Exception e) {
          log.error("Failed initializing project: {}", project.getName(), e);
        }
      }
    }
  }

  @Override
  public Iterable<Project> getProjects() {
    return projectRepository.findAll();
  }

  @Override
  public boolean hasProject(@NotNull String name) {
    try {
      getProject(name);
      return true;
    } catch (NoSuchProjectException e) {
      return false;
    }
  }

  @Override
  public void delete(@NotNull String name, boolean archive) throws NoSuchProjectException, FileSystemException {
    Project project = getProject(name);
    boolean internal = projectDatabaseService.isInternal(project);

    projectRepository.deleteByKey(project);

    Datasource datasource = project.getDatasource();

    // call tables listeners
    for (ValueTable valueTable : datasource.getValueTables()) {
      eventBus.post(new ValueTableDeletedEvent(valueTable));
    }

    // disconnect datasource
    MagmaEngine.get().removeDatasource(datasource);
    viewManager.unregisterDatasource(datasource.getName());

    if (!archive) {
      // remove all views
      viewManager.removeAllViews(datasource.getName());
      if (internal) {
        // the file is about to go, and dropping its tables one by one would be minutes of work for no effect
        projectDatabaseService.deleteInternalDatabase(name);
      } else if (datasource.canDrop()) {
        // an operator declared this database, so only what the project put in it goes
        datasource.drop();
      }
      // remove project folder
      deleteFolder(getProjectDirectory(project));
      // remove keystore
      projectsKeyStoreService.deleteKeyStore(project);
    }
    // an archived deletion keeps the data: the database the project owned stays, referred to by nobody, and an
    // operator can see it in the databases page and delete it there
  }

  @NotNull
  @Override
  public ProjectStorage getStorage(@NotNull Project project) {
    if(projectDatabaseService.isInternal(project)) return ProjectStorage.internal();
    return project.hasDatabase() ? ProjectStorage.registered(project.getDatabase()) : ProjectStorage.none();
  }

  @Override
  @Transactional(propagation = Propagation.NEVER)
  public void save(@NotNull final Project project) throws ConstraintViolationException {
    save(project, ProjectStorage.unchanged());
  }

  @Override
  @Transactional(propagation = Propagation.NEVER)
  public void save(@NotNull final Project project, @NotNull ProjectStorage storage)
      throws ConstraintViolationException {
    String name = project.getName();
    Project original = projectRepository.findByName(name).orElse(null);
    String originalDb = original == null ? "" : nullToEmpty(original.getDatabase());
    boolean wasInternal = original != null && projectDatabaseService.isInternal(original);
    String newDb = nullToEmpty(storageDatabaseName(project, original, storage));
    boolean storageChanges = original != null && !newDb.equals(originalDb);

    // both refusals run before anything is created, so a refused save leaves nothing behind. Only when the storage is
    // actually being set: a project is saved again on every table change, and that is not the moment to query for it.
    if (storage.kind() == ProjectStorage.Kind.REGISTERED && (original == null || storageChanges)) {
      assertNotOwnedByAnotherProject(name, storage.databaseName());
    }
    if (storageChanges) assertHasNoData(name);

    boolean ownedDatabaseExisted = projectDatabaseService.getInternalDatabase(name).isPresent();
    if (storage.kind() == ProjectStorage.Kind.INTERNAL) projectDatabaseService.ensureInternalDatabase(name);
    project.setDatabase(newDb.isEmpty() ? null : newDb);

    try {
      if (original == null) {
        registerDatasource(project);
      } else if (storageChanges) {
        dropDatasource(name);
        databaseRegistry.unregister(originalDb, name);
        registerDatasource(project);
      }

      synchronized (this) {
        projectRepository.upsert(project);
        try {
          getProjectDirectory(project);
        } catch (FileSystemException e) {
          log.warn("Project's directory is not accessible: {}", name, e);
        }
      }
    } catch (RuntimeException e) {
      // a database created here, and not there before, goes away again: a failed creation leaves no folder behind
      if (storage.kind() == ProjectStorage.Kind.INTERNAL && !ownedDatabaseExisted) {
        projectDatabaseService.deleteInternalDatabase(name);
      }
      throw e;
    }

    // leaving internal storage: the database the project owned is empty by now, and nothing else can use it
    if (storageChanges && storage.kind() != ProjectStorage.Kind.INTERNAL && wasInternal) {
      projectDatabaseService.deleteInternalDatabase(name);
    }
  }

  /**
   * The name of the database the project would hold. A project-owned one is named after the project, so asking for it
   * costs nothing and does not create it: the refusals below run first.
   */
  private String storageDatabaseName(Project project, @Nullable Project original, ProjectStorage storage) {
    return switch (storage.kind()) {
      // a project being created says what it holds; one being saved again keeps what it held
      case UNCHANGED -> original == null ? project.getDatabase() : original.getDatabase();
      case NONE -> null;
      case REGISTERED -> storage.databaseName();
      case INTERNAL -> ProjectDatabaseService.INTERNAL_PREFIX + project.getName();
    };
  }

  /**
   * Changing where a project stores its data drops what it holds, and for a database the project owns it deletes a
   * file. The administration UI has always refused it on a project with tables; nothing on the server did.
   */
  private void assertHasNoData(String projectName) {
    if (!MagmaEngine.get().hasDatasource(projectName)) return;
    if (!MagmaEngine.get().getDatasource(projectName).getValueTables().isEmpty()) {
      throw InvalidProjectStorageException.hasData(projectName);
    }
  }

  /**
   * Now that project-owned databases are listed, a client can name one: two projects in one of them is precisely what
   * internal storage exists to prevent. Checked here because the UI filter that hides them is cosmetic.
   */
  private void assertNotOwnedByAnotherProject(String projectName, String databaseName) {
    if (nullToEmpty(databaseName).isEmpty() || !databaseRegistry.hasDatabase(databaseName)) return;
    String owner = databaseRegistry.getDatabase(databaseName).getOwnerProject();
    if (owner != null && !owner.equals(projectName)) {
      throw InvalidProjectStorageException.ownedByAnotherProject(projectName, databaseName, owner);
    }
  }

  private void dropDatasource(String projectName) {
    if (!MagmaEngine.get().hasDatasource(projectName)) return;
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        Datasource datasource = MagmaEngine.get().getDatasource(projectName);
        MagmaEngine.get().removeDatasource(datasource);
        viewManager.unregisterDatasource(datasource.getName());
        if (datasource.canDrop()) {
          try {
            datasource.drop();
          } catch (Exception e) {
            log.warn("Project's datasource drop failed: {}", projectName, e);
          }
        }
      }
    });
  }

  /**
   * A folder under the H2 folder that no database refers to: the leftovers of an archiving deletion whose row an
   * operator has since removed, or a bug in this feature. Reported, never deleted - it may be somebody's data.
   */
  private void reportOrphanProjectDatabaseFolders() {
    try {
      for (File folder : projectDatabaseService.listOrphanFolders()) {
        log.warn("Orphan project database folder, no database refers to it: {} ({})", folder.getAbsolutePath(),
            FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(folder)));
      }
    } catch (Exception e) {
      log.warn("Cannot list the project database folders: {}", e.getMessage());
    }
  }

  @NotNull
  @Override
  public Project getProject(@NotNull String name) throws NoSuchProjectException {
    return projectRepository.findByName(name).orElseThrow(() -> new NoSuchProjectException(name));
  }

  @Override
  public List<ResourceReference> getResourceReferences(Project project) {
    return resourceReferenceService.getResourceReferences(project.getName());
  }

  @Override
  public int getProjectFilesCount(Project project) {
    try {
      FileObject projectDir = getProjectDirectory(project);
      return projectDir.findFiles(new FileSelector() {
        @Override
        public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
          return !fileSelectInfo.getFile().isFolder();
        }

        @Override
        public boolean traverseDescendants(FileSelectInfo fileInfo) throws Exception {
          return true;
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
          return true;
        }
      }).length;
    } catch (FileSystemException e) {
      log.warn("Unable to count files for project: {}", project.getName(), e);
      return 0;
    }
  }

  @NotNull
  @Override
  public FileObject getProjectDirectory(@NotNull Project project)
      throws NoSuchIdentifiersMappingException, FileSystemException {
    FileObject projectDir = opalFileSystemService.getFileSystem().getRoot().resolveFile(PROJECTS_DIR)
        .resolveFile(project.getName());
    projectDir.createFolder();
    return projectDir;
  }

  @NotNull
  @Override
  public String getProjectDirectoryPath(@NotNull Project project) {
    try {
      FileObject fo = getProjectDirectory(project);
      return fo.getURL().getPath().substring(2);
    } catch (FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getProjectState(Project project) {
    return projectsState.getProjectState(project.getName());
  }

  /**
   * Create DatasourceFactory and add it to MagmaEngine
   *
   * @param project
   * @return
   */
  @NotNull
  private void registerDatasource(@NotNull final Project project) {
    projectsState.updateProjectState(project.getName(), ProjectsState.State.LOADING);
    datasourceLoaderService.registerDatasource(project);
  }

  private void deleteFolder(FileObject folder) throws FileSystemException {
    if (!folder.isWriteable()) return;

    for (FileObject file : folder.getChildren()) {
      if (file.getType() == FileType.FOLDER) {
        deleteFolder(file);
      } else if (file.isWriteable()) {
        file.delete();
      }
    }
    if (folder.getChildren().length == 0) {
      folder.delete();
    }
  }

  // Keep track of each ValueTable change and save Project to update last updated date.

  @Subscribe
  public void onValueTable(ValueTableEvent event) {
    try {
      String datasourceName = event instanceof ValueTableAddedEvent ?
          ((ValueTableAddedEvent) event).getDatasourceName() : event.getValueTable().getDatasource().getName();
      Project project = getProject(datasourceName);
      save(project);
    } catch (NoSuchProjectException e) {
      // ignore
    } catch (Exception e) {
      log.warn(e.getMessage());
    }
  }

  @Subscribe
  public void onValueTableDeleted(ValueTableDeletedEvent event) {
    if (identifiersTableService.hasDatasource() && identifiersTableService.getDatasource().equals(event.getValueTable().getDatasource())) {
      removeProjectsIdentifiersMappingByEntityType(event.getValueTable());
    }
  }

  @Subscribe
  public void onVariableDeleted(VariableDeletedEvent event) {
    if (identifiersTableService.hasDatasource() && identifiersTableService.getDatasource().equals(event.getValueTable().getDatasource())) {
      removeProjectsIdentifiersMappingByMapping(event.getVariable());
    }
  }

  @Subscribe
  public synchronized void onResourceProvidersServiceStarted(ResourceProvidersServiceStartedEvent event) {
    log.info("Resource providers ready, scanning for views to initialise...");
    Callable<Object> callable = getSubject().associateWith(() -> {
      initViewsInError();
      return null;
    });
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(callable);
  }

  //
  // Private methods
  //

  /**
   * Authenticate as background task user, needed to list secured datasources.
   *
   * @return
   */
  private Subject getSubject() {
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager()
          .authenticate(BackgroundJobServiceAuthToken.INSTANCE).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void initViewsInError() {
    getProjects().forEach(project -> {
          if (ProjectsState.State.READY.name().equals(getProjectState(project))) {
            project.getDatasource()
                .getValueTables().stream()
                .filter(ValueTable::isView)
                .filter(view -> ValueTableStatus.ERROR.equals(view.getStatus()))
                .forEach(view -> {
                  log.info("Initialise {}.{}", project.getName(), view.getName());
                  try {
                    viewManager.initView(project.getName(), view.getName());
                  } catch (Exception e) {
                    log.error("{}", e.getMessage());
                  }
                });
          }
        }
    );
  }

  private void removeProjectsIdentifiersMappingByEntityType(ValueTable valueTable) {
    getProjects().forEach(project -> {
      project.removeIdentifiersMappingByEntityType(valueTable.getEntityType());
      save(project);
    });
  }

  private void removeProjectsIdentifiersMappingByMapping(Variable variable) {
    getProjects().forEach(project -> {
      project.removeIdentifiersMappingByMapping(variable.getEntityType(), variable.getName());
      save(project);
    });
  }

}
