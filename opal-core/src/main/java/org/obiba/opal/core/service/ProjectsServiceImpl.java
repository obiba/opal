/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.*;
import org.obiba.magma.datasource.nil.support.NullDatasourceFactory;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.ProjectsState;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.event.ValueTableAddedEvent;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.event.ValueTableEvent;
import org.obiba.opal.core.event.VariableDeletedEvent;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.base.Strings.nullToEmpty;

@Component
public class ProjectsServiceImpl implements ProjectService {

  private static final String PROJECTS_DIR = "projects";

  private static final Logger log = LoggerFactory.getLogger(ProjectsServiceImpl.class);

  private final OpalRuntime opalRuntime;

  private final OrientDbService orientDbService;

  private final DatabaseRegistry databaseRegistry;

  private final ProjectsKeyStoreService projectsKeyStoreService;

  private final IdentifiersTableService identifiersTableService;

  private final ViewManager viewManager;

  private final TransactionalThreadFactory transactionalThreadFactory;

  private final TransactionTemplate transactionTemplate;

  private final EventBus eventBus;

  private final ResourceReferenceService resourceReferenceService;

  private final ProjectsState projectsState;

  private final BlockingQueue<Project> datasourceLoadQueue = new LinkedBlockingQueue<>();

  private DatasourceLoader datasourceLoader;

  @Autowired
  public ProjectsServiceImpl(OpalRuntime opalRuntime,
                             OrientDbService orientDbService,
                             DatabaseRegistry databaseRegistry,
                             ProjectsKeyStoreService projectsKeyStoreService,
                             IdentifiersTableService identifiersTableService,
                             ViewManager viewManager,
                             TransactionalThreadFactory transactionalThreadFactory, TransactionTemplate transactionTemplate,
                             EventBus eventBus, ResourceReferenceService resourceReferenceService, ProjectsState projectsState) {
    this.opalRuntime = opalRuntime;
    this.orientDbService = orientDbService;
    this.databaseRegistry = databaseRegistry;
    this.projectsKeyStoreService = projectsKeyStoreService;
    this.identifiersTableService = identifiersTableService;
    this.viewManager = viewManager;
    this.transactionalThreadFactory = transactionalThreadFactory;
    this.transactionTemplate = transactionTemplate;
    this.eventBus = eventBus;
    this.resourceReferenceService = resourceReferenceService;
    this.projectsState = projectsState;
  }


  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(Project.class);

    startDatasourceLoaderThread();

    // In the @PostConstruct there is no way to ensure that all the post processing is already done,
    // so (indeed) there can be no Transactions. The only way to ensure that that is working is by using a TransactionTemplate.
    // Add all project datasources to MagmaEngine
    for (Project project : getProjects()) {
      if (!project.isArchived()) {
        try {
          registerDatasource(project);
        } catch (Exception e) {
          databaseRegistry.unregister(project.getDatabase(), project.getName());
          log.error("Failed initializing project: {}", project.getName(), e);
        }
      }
    }
  }

  @Override
  @PreDestroy
  public void stop() {
    terminateDatasourceLoaderThread();
  }

  @Override
  public Iterable<Project> getProjects() {
    return orientDbService.list(Project.class);
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

    orientDbService.delete(project);

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
      // remove datasource
      if (datasource.canDrop()) datasource.drop();
      // remove project folder
      deleteFolder(getProjectDirectory(project));
      // remove keystore
      projectsKeyStoreService.deleteKeyStore(project);
    }
  }

  @Override
  @Transactional(propagation = Propagation.NEVER)
  public void save(@NotNull final Project project) throws ConstraintViolationException {
    try {
      Project original = getProject(project.getName());
      String originalDb = nullToEmpty(original.getDatabase());
      String newDb = nullToEmpty(project.getDatabase());
      if (!newDb.equals(originalDb)) {
        if (MagmaEngine.get().hasDatasource(project.getName())) {
          transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
              Datasource datasource = MagmaEngine.get().getDatasource(project.getName());
              MagmaEngine.get().removeDatasource(datasource);
              viewManager.unregisterDatasource(datasource.getName());
              if (datasource.canDrop()) {
                try {
                  datasource.drop();
                } catch (Exception e) {
                  log.warn("Project's datasource drop failed: {}", project.getName(), e);
                }
              }
            }
          });
        }
        databaseRegistry.unregister(originalDb, project.getName());
        registerDatasource(project);
      }
    } catch (NoSuchProjectException e) {
      registerDatasource(project);
    }

    synchronized (this) {
      orientDbService.save(project, project);
    }
  }

  @NotNull
  @Override
  public Project getProject(@NotNull String name) throws NoSuchProjectException {
    Project project = orientDbService.findUnique(new Project(name));
    if (project == null) throw new NoSuchProjectException(name);
    return project;
  }

  @Override
  public List<ResourceReference> getResourceReferences(Project project) {
    return resourceReferenceService.getResourceReferences(project.getName());
  }

  @NotNull
  @Override
  public FileObject getProjectDirectory(@NotNull Project project)
      throws NoSuchIdentifiersMappingException, FileSystemException {
    FileObject projectDir = opalRuntime.getFileSystem().getRoot().resolveFile(PROJECTS_DIR)
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

  public static DatasourceFactory registerDatasource(final Project project, final TransactionTemplate transactionTemplate, final DatabaseRegistry databaseRegistry) {
    return transactionTemplate.execute(status -> {
      log.info("Datasource load start: {}", project.getName());
      DatasourceFactory dataSourceFactory;
      if (project.hasDatabase()) {
        Database database = databaseRegistry.getDatabase(project.getDatabase());
        dataSourceFactory = databaseRegistry.createDatasourceFactory(project.getName(), database);
      } else {
        dataSourceFactory = new NullDatasourceFactory();
        dataSourceFactory.setName(project.getName());
      }
      MagmaEngine.get().addDatasource(dataSourceFactory);
      log.info("Datasource load end: {} ({})", project.getName(), dataSourceFactory.getClass().getSimpleName());
      return dataSourceFactory;
    });
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
    datasourceLoadQueue.offer(project);
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
    if (identifiersTableService.getDatasource().equals(event.getValueTable().getDatasource())) {
      removeProjectsIdentifiersMappingByEntityType(event.getValueTable());
    }
  }

  @Subscribe
  public void onVariableDeleted(VariableDeletedEvent event) {
    if (identifiersTableService.getDatasource().equals(event.getValueTable().getDatasource())) {
      removeProjectsIdentifiersMappingByMapping(event.getVariable());
    }
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

  private void startDatasourceLoaderThread() {
    datasourceLoader = new DatasourceLoader();
    datasourceLoader.setName("Datasource Loader " + datasourceLoader);
    datasourceLoader.setPriority(Thread.MIN_PRIORITY);
    datasourceLoader.start();
  }

  public void terminateDatasourceLoaderThread() {
    try {
      if (datasourceLoader != null && datasourceLoader.isAlive()) datasourceLoader.interrupt();
    } catch (Exception e) {
      // ignore
    }
  }

  private class DatasourceLoader extends Thread {

    @Override
    public void run() {
      try {
        //noinspection InfiniteLoopStatement
        while (true) {
          load(datasourceLoadQueue.take());
        }
      } catch (InterruptedException ignored) {
        log.debug("Stopping datasource loader");
      }
    }

    private void load(Project project) {
      registerDatasource(project, transactionTemplate, databaseRegistry);
      projectsState.updateProjectState(project.getName(), ProjectsState.State.READY);
    }
  }
}
