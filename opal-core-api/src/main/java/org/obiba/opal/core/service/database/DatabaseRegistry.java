/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.database;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.service.SystemService;

import javax.sql.DataSource;

public interface DatabaseRegistry extends SystemService {

  Iterable<Database> list();

  Iterable<Database> listSqlDatabases();

  Iterable<Database> listMongoDatabases();

  Iterable<Database> list(@Nullable Database.Usage usage);

  boolean hasDatabases(@Nullable Database.Usage usage);

  boolean hasDatabase(@Nullable String name);

  @NotNull
  Database getDatabase(@NotNull String name) throws NoSuchDatabaseException;

  /**
   * Register a database an operator declared. Names beginning with an underscore are Opal's, and ownership is never
   * taken from what a client sent: {@link #createProjectOwned(Database)} is how a database gets an owner.
   */
  void create(@NotNull Database database) throws ConstraintViolationException, MultipleIdentifiersDatabaseException;

  /**
   * Register the database Opal created for a project. The counterpart of {@link #deleteProjectOwned(Database)}, and
   * the only way in: {@link #create(Database)} refuses an owner.
   */
  void createProjectOwned(@NotNull Database database) throws ConstraintViolationException;

  /**
   * @throws DatabaseOwnedByProjectException if the database belongs to a project that still exists
   */
  void update(@NotNull Database database) throws ConstraintViolationException, MultipleIdentifiersDatabaseException;

  boolean hasDatasource(@NotNull Database database);

  boolean hasEntities(@NotNull Database database);

  /**
   * Unregister a database. For one an operator declared this leaves whatever it holds where it is - Opal never deletes
   * what an operator declared. For one a project owns and no longer has - an archiving deletion left it behind - the
   * files go with the row, which is the one place a project-owned database is an operator's to remove.
   *
   * @throws DatabaseOwnedByProjectException if the database belongs to a project that still exists
   */
  void delete(@NotNull Database database)
      throws CannotDeleteDatabaseLinkedToDatasourceException, CannotDeleteDatabaseWithDataException;

  /**
   * Delete a database Opal owns: the row, and the files under the H2 folder. Called when its project is deleted, and
   * named apart from {@link #delete(Database)} rather than defeating that method's guard by an implicit ordering.
   */
  void deleteProjectOwned(@NotNull Database database);

  DataSource getDataSource(@NotNull String name, @Nullable String usedByDatasource);

  /**
   * The data sources that are open right now, and only those. A caller that periodically visits every database — the
   * H2 checkpointer is the one — must not be the reason a registered database gets opened: that would turn a
   * maintenance task into "hold a connection pool on every database an administrator ever declared".
   */
  Iterable<DataSource> getLoadedDataSources();

  void unregister(@NotNull String databaseName, @Nullable String usedByDatasource);

  boolean hasIdentifiersDatabase();

  @NotNull
  Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException;

  @NotNull
  DatasourceFactory createDatasourceFactory(@NotNull String datasourceName, @NotNull Database database);

  @Nullable
  Database getDefaultStorageDatabase();
}
