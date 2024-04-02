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
import javax.validation.constraints.NotNull;
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

  @NotNull
  Database getDatabase(@NotNull String name) throws NoSuchDatabaseException;

  void create(@NotNull Database database) throws ConstraintViolationException, MultipleIdentifiersDatabaseException;

  void update(@NotNull Database database) throws ConstraintViolationException, MultipleIdentifiersDatabaseException;

  boolean hasDatasource(@NotNull Database database);

  boolean hasEntities(@NotNull Database database);

  void delete(@NotNull Database database)
      throws CannotDeleteDatabaseLinkedToDatasourceException, CannotDeleteDatabaseWithDataException;

  DataSource getDataSource(@NotNull String name, @Nullable String usedByDatasource);

  void unregister(@NotNull String databaseName, @Nullable String usedByDatasource);

  boolean hasIdentifiersDatabase();

  @NotNull
  Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException;

  @NotNull
  DatasourceFactory createDatasourceFactory(@NotNull String datasourceName, @NotNull Database database);

  @Nullable
  Database getDefaultStorageDatabase();
}
