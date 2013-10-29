/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.ConstraintViolationException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.nil.support.NullDatasourceFactory;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.impl.OrientDbDocumentService;
import org.obiba.opal.project.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ProjectsConfigurationService implements ProjectService {

  public static final String PROJECTS_DIR = "projects";

  private static final String UNIQUE_NAME_INDEX = "name";

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private OrientDbDocumentService orientDbDocumentService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Override
  @PostConstruct
  public void start() {
    orientDbDocumentService.createUniqueStringIndex(Project.class, UNIQUE_NAME_INDEX);
    orientDbDocumentService.createUniqueStringIndex(Project.class, "title");

    // In the @PostConstruct there is no way to ensure that all the post processing is already done,
    // so (indeed) there can be no Transactions.
    // The only way to ensure that that is working is by using a TransactionTemplate.
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        // add all project datasources to MagmaEngine
        registerAllProjects();
      }
    });
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void registerAllProjects() {
    for(Project project : getProjects()) {
      registerDatasource(project);
    }
  }

  @Override
  @PreDestroy
  public void stop() {

  }

  @Override
  public Iterable<Project> getProjects() {
    return orientDbDocumentService.list(Project.class);
  }

  @Override
  public boolean hasProject(@Nonnull String name) {
    try {
      getProject(name);
      return true;
    } catch(NoSuchProjectException e) {
      return false;
    }
  }

  @Override
  public void delete(@Nonnull String name) throws NoSuchProjectException, FileSystemException {
    Project project = getProject(name);

    orientDbDocumentService.deleteUnique(Project.class, UNIQUE_NAME_INDEX, name);

    Datasource datasource = project.getDatasource();

    // disconnect datasource
    MagmaEngine.get().removeDatasource(datasource);
    // remove all views
    viewManager.removeAllViews(datasource.getName());
    // remove datasource
    if(datasource.canDrop()) datasource.drop();

    // remove project folder
    deleteFolder(getProjectDirectory(project));
  }

  @Override
  public void save(@Nonnull Project project) throws ConstraintViolationException {
    registerDatasource(project);
    orientDbDocumentService.save(project);
  }

  @Nonnull
  @Override
  public Project getProject(@Nonnull String name) throws NoSuchProjectException {
    Project project = orientDbDocumentService.findUnique(Project.class, UNIQUE_NAME_INDEX, name);
    if(project == null) throw new NoSuchProjectException(name);
    return project;
  }

  @Nonnull
  @Override
  public FileObject getProjectDirectory(@Nonnull Project project)
      throws NoSuchFunctionalUnitException, FileSystemException {
    FileObject projectDir = opalRuntime.getFileSystem().getRoot().resolveFile(PROJECTS_DIR)
        .resolveFile(project.getName());
    projectDir.createFolder();
    return projectDir;
  }

  @Nonnull
  @Override
  public String getProjectDirectoryPath(@Nonnull Project project) {
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
  @Nonnull
  private DatasourceFactory registerDatasource(@Nonnull Project project) {
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
