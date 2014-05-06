/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.datasource.nil.support.NullDatasourceFactory;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import static com.google.common.base.Strings.nullToEmpty;

@Component
public class ProjectsServiceImpl implements ProjectService {

  private static final String PROJECTS_DIR = "projects";

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Set<ValueTableUpdateListener> tableListeners;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(Project.class);

    // In the @PostConstruct there is no way to ensure that all the post processing is already done,
    // so (indeed) there can be no Transactions. The only way to ensure that that is working is by using a TransactionTemplate.
    // Add all project datasources to MagmaEngine
    for(Project project : getProjects()) {
      registerDatasource(project);
    }
  }

  @Override
  @PreDestroy
  public void stop() {}

  @Override
  public Iterable<Project> getProjects() {
    return orientDbService.list(Project.class);
  }

  @Override
  public boolean hasProject(@NotNull String name) {
    try {
      getProject(name);
      return true;
    } catch(NoSuchProjectException e) {
      return false;
    }
  }

  @Override
  public void delete(@NotNull String name, boolean archive) throws NoSuchProjectException, FileSystemException {
    Project project = getProject(name);

    orientDbService.delete(project);

    Datasource datasource = project.getDatasource();

    // call tables listeners
    if(tableListeners != null && !tableListeners.isEmpty()) {
      for(ValueTableUpdateListener listener : tableListeners) {
        for(ValueTable valueTable : datasource.getValueTables()) {
          listener.onDelete(valueTable);
        }
      }
    }

    // disconnect datasource
    MagmaEngine.get().removeDatasource(datasource);
    viewManager.unregisterDatasource(datasource.getName());

    if (!archive) {
      // remove all views
      viewManager.removeAllViews(datasource.getName());
      // remove datasource
      if(datasource.canDrop()) datasource.drop();
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
      if(!newDb.equals(originalDb)) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
          @Override
          protected void doInTransactionWithoutResult(TransactionStatus status) {
            Datasource datasource = MagmaEngine.get().getDatasource(project.getName());
            MagmaEngine.get().removeDatasource(datasource);
            viewManager.unregisterDatasource(datasource.getName());
            if(datasource.canDrop()) {
              datasource.drop();
            }
          }
        });
        databaseRegistry.unregister(originalDb, project.getName());
        registerDatasource(project);
      }
    } catch(NoSuchProjectException e) {
      registerDatasource(project);
    }
    orientDbService.save(project, project);
  }

  @NotNull
  @Override
  public Project getProject(@NotNull String name) throws NoSuchProjectException {
    Project project = orientDbService.findUnique(new Project(name));
    if(project == null) throw new NoSuchProjectException(name);
    return project;
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
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Create DatasourceFactory and add it to MagmaEngine
   *
   * @param project
   * @return
   */
  @NotNull
  private DatasourceFactory registerDatasource(@NotNull final Project project) {
    return transactionTemplate.execute(new TransactionCallback<DatasourceFactory>() {
      @Override
      public DatasourceFactory doInTransaction(TransactionStatus status) {
        DatasourceFactory dataSourceFactory = null;
        if(project.hasDatabase()) {
          Database database = databaseRegistry.getDatabase(project.getDatabase());
          dataSourceFactory = databaseRegistry.createDataSourceFactory(project.getName(), database);
        } else {
          dataSourceFactory = new NullDatasourceFactory();
          dataSourceFactory.setName(project.getName());
        }
        MagmaEngine.get().addDatasource(dataSourceFactory);
        return dataSourceFactory;
      }
    });
  }

  private void deleteFolder(FileObject folder) throws FileSystemException {
    if(!folder.isWriteable()) return;

    for(FileObject file : folder.getChildren()) {
      if(file.getType() == FileType.FOLDER) {
        deleteFolder(file);
      } else if(file.isWriteable()) {
        file.delete();
      }
    }
    if(folder.getChildren().length == 0) {
      folder.delete();
    }
  }
}
