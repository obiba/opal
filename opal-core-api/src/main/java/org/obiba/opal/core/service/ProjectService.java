/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.domain.Project;

/**
 * Service to manage projects.
 */
public interface ProjectService extends SystemService {

  Iterable<Project> getProjects();

  @NotNull
  Project getProject(@NotNull String name) throws NoSuchProjectException;

  boolean hasProject(@NotNull String name);

  void save(@NotNull Project project) throws ConstraintViolationException;

  void delete(@NotNull String name, boolean archive) throws NoSuchProjectException, FileSystemException;

  /**
   * Get project directory, create it if it does not exist.
   *
   * @param name
   * @return
   * @throws NoSuchIdentifiersMappingException
   * @throws FileSystemException
   */
  @NotNull
  FileObject getProjectDirectory(@NotNull Project project) throws NoSuchIdentifiersMappingException, FileSystemException;

  @NotNull
  String getProjectDirectoryPath(@NotNull Project project);

}
