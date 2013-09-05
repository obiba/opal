/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.project.domain.Project;

/**
 * Service to manage projects.
 */
public interface ProjectService {

  List<Project> getProjects();

  boolean hasProject(String name);

  void removeProject(String name);

  void addOrReplaceProject(Project project);

  Project getOrCreateProject(Datasource ds);

  Project getProject(String name);

  /**
   * Get project directory, create it if it does not exist.
   *
   * @param name
   * @return
   * @throws NoSuchFunctionalUnitException
   * @throws FileSystemException
   */
  FileObject getProjectDirectory(String name) throws NoSuchFunctionalUnitException, FileSystemException;

  String getProjectDirectoryPath(String name);

}
