/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import jakarta.validation.constraints.NotNull;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.database.Database;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * The database a project owns: an H2 file Opal creates with the project and deletes with it, as opposed to one an
 * operator registered beforehand and whose lifetime is not Opal's business.
 * <p>
 * Everything about internal storage lives here, so that {@code ProjectService} can create and delete a project's
 * storage without knowing what H2 is.
 */
public interface ProjectDatabaseService {

  /**
   * Reserved (see {@code DatabaseRegistry.create}), so that a project can never fail to be created because someone
   * once registered a database of that name.
   */
  String INTERNAL_PREFIX = "_project_";

  /**
   * Whether the project stores its data in a database it owns. A test on the name, not a query: it is asked for every
   * project in every listing. The prefix is reserved and the unique constraint on the owner column backs it, so the
   * cheap test and the authoritative column cannot disagree.
   */
  boolean isInternal(@NotNull Project project);

  /**
   * The database owned by this project, if there is one.
   */
  Optional<Database> getInternalDatabase(@NotNull String projectName);

  /**
   * The database owned by this project, created if missing. Idempotent: a project recreated after an archiving
   * deletion gets its data back rather than a second database.
   *
   * @throws ConflictingProjectDatabaseException if another project's database would be the same folder
   */
  @NotNull
  Database ensureInternalDatabase(@NotNull String projectName);

  /**
   * Close the database owned by this project, unregister it and delete its files. Silent when there is none.
   */
  void deleteInternalDatabase(@NotNull String projectName);

  /**
   * Finish the removals a previous run began, retrying the last step of a project deletion. A failure is logged and
   * left for the next start.
   */
  void deletePendingFolders();

  /**
   * Folders under {@code data/h2} that no database row points at, so that a failed deletion can be reported and
   * retried. Never deleted: an operator who moved a file in by hand, or a bug in this feature, must not cost anyone
   * their data.
   */
  Collection<File> listOrphanFolders();
}
