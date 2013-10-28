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

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolationException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.project.domain.Project;

/**
 * Service to manage projects.
 */
public interface ProjectService extends SystemService {

  Iterable<Project> getProjects();

  @Nonnull
  Project getProject(@Nonnull String name) throws NoSuchProjectException;

  boolean hasProject(@Nonnull String name);

  void save(@Nonnull Project project) throws ConstraintViolationException;

  void delete(@Nonnull String name) throws NoSuchProjectException, FileSystemException;

  /**
   * Get project directory, create it if it does not exist.
   *
   * @param name
   * @return
   * @throws NoSuchFunctionalUnitException
   * @throws FileSystemException
   */
  @Nonnull
  FileObject getProjectDirectory(@Nonnull Project project) throws NoSuchFunctionalUnitException, FileSystemException;

  @Nonnull
  String getProjectDirectoryPath(@Nonnull Project project);

}
