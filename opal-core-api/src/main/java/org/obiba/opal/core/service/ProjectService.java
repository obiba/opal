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

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.ResourceReference;

import java.util.List;

/**
 * Service to manage projects.
 */
public interface ProjectService extends SystemService {

  Iterable<Project> getProjects();

  @NotNull
  Project getProject(@NotNull String name) throws NoSuchProjectException;

  boolean hasProject(@NotNull String name);

  /**
   * What a project's data is stored in: nothing, a database an operator registered, or one the project owns.
   */
  record ProjectStorage(@NotNull Kind kind, @Nullable String databaseName) {

    public enum Kind {
      /**
       * Not said, so not touched. What a payload that predates internal storage amounts to, and what saving a project
       * for any other reason - a table was added, an identifiers mapping was removed - asks for.
       */
      UNCHANGED, NONE, REGISTERED, INTERNAL
    }

    public static ProjectStorage unchanged() {
      return new ProjectStorage(Kind.UNCHANGED, null);
    }

    public static ProjectStorage none() {
      return new ProjectStorage(Kind.NONE, null);
    }

    public static ProjectStorage registered(@Nullable String databaseName) {
      return new ProjectStorage(Kind.REGISTERED, databaseName);
    }

    public static ProjectStorage internal() {
      return new ProjectStorage(Kind.INTERNAL, null);
    }
  }

  /**
   * The storage a project has. Authoritative, unlike the name it holds: a database registered before
   * {@code _project_} was reserved may carry that prefix and belong to no project.
   */
  @NotNull
  ProjectStorage getStorage(@NotNull Project project);

  /**
   * Save a project, leaving its storage as it is.
   */
  void save(@NotNull Project project) throws ConstraintViolationException;

  /**
   * Save a project and give it the storage asked for: create the database it owns, or delete the one it is leaving.
   *
   * @throws InvalidProjectStorageException if the project holds data, or if the database named belongs to another
   * project
   */
  void save(@NotNull Project project, @NotNull ProjectStorage storage) throws ConstraintViolationException;

  void delete(@NotNull String name, boolean archive) throws NoSuchProjectException, FileSystemException;

  /**
   * Get project directory, create it if it does not exist.
   *
   * @param project
   * @return
   * @throws NoSuchIdentifiersMappingException
   * @throws FileSystemException
   */
  @NotNull
  FileObject getProjectDirectory(@NotNull Project project) throws NoSuchIdentifiersMappingException, FileSystemException;

  @NotNull
  String getProjectDirectoryPath(@NotNull Project project);

  /**
   * Get the number of files in the project directory, recursively.
   *
   * @param project
   * @return
   */
  int getProjectFilesCount(Project project);

  @NotNull
  String getProjectState(@NotNull Project project);

  List<ResourceReference> getResourceReferences(Project project);

  void initialize();
}
