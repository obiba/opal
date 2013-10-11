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
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.project.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;

@Component
public class ProjectsConfigurationService implements ProjectService {

  public static final String PROJECTS_DIR = "projects";

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private ViewManager viewManager;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.registerEntityClass(Project.class);
    orientDbService.createUniqueStringIndex(Project.class, "name");

    // add all project datasources to MagmaEngine
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
    return orientDbService.list(Project.class);
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
  public void deleteProject(@Nonnull String name) throws NoSuchProjectException, FileSystemException {
    Project project = getProject(name);
    orientDbService.delete(project);

    Datasource datasource = project.getDatasource();

    // disconnect datasource
    MagmaEngine.get().removeDatasource(datasource);
    // remove all views
    viewManager.removeAllViews(datasource.getName());
    // remove datasource
    if(datasource.canDrop()) datasource.drop();

    // remove project folder
    deleteFolder(getProjectDirectory(name));
  }

  @Override
  public void createProject(@Nonnull Project project) throws ProjectAlreadyExistsException {
    try {
      orientDbService.save(project);
    } catch(ORecordDuplicatedException e) {
      throw new ProjectAlreadyExistsException(project.getName());
    }
    registerDatasource(project);
  }

  @Override
  public void updateProject(@Nonnull Project project) throws ProjectAlreadyExistsException {
    try {
      orientDbService.save(project);
    } catch(ORecordDuplicatedException e) {
      throw new ProjectAlreadyExistsException(project.getName());
    }
  }

  @Nonnull
  @Override
  public Project getProject(@Nonnull String name) throws NoSuchProjectException {
    Project project = orientDbService.uniqueResult("select from Project where name = ?", name);
    if(project == null) throw new NoSuchProjectException(name);
    return project;
  }

  @Nonnull
  @Override
  public FileObject getProjectDirectory(@Nonnull String name)
      throws NoSuchProjectException, NoSuchFunctionalUnitException, FileSystemException {
    // check project exists
    Project project = getProject(name);
    FileObject projectDir = opalRuntime.getFileSystem().getRoot().resolveFile(PROJECTS_DIR)
        .resolveFile(project.getName());
    projectDir.createFolder();
    return projectDir;
  }

  @Nonnull
  @Override
  public String getProjectDirectoryPath(@Nonnull String name) throws NoSuchProjectException {
    try {
      FileObject fo = getProjectDirectory(name);
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
