package org.obiba.opal.core.service.database;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.hibernate.SessionFactory;
import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.service.SystemService;

public interface DatabaseRegistry extends SystemService {

  Iterable<Database> list();

  Iterable<Database> listSqlDatabases();

  Iterable<Database> listMongoDatabases();

  Iterable<Database> list(@Nullable Database.Usage usage);

  boolean hasDatabases(@Nullable Database.Usage usage);

  @NotNull
  Database getDatabase(@NotNull String name) throws NoSuchDatabaseException;

  void save(@NotNull Database database) throws ConstraintViolationException, MultipleIdentifiersDatabaseException;

  void delete(@NotNull Database database) throws CannotDeleteDatabaseWithDataException;

  DataSource getDataSource(@NotNull String name, @Nullable String usedByDatasource);

  SessionFactory getSessionFactory(@NotNull String name, @Nullable String usedByDatasource);

  void unregister(@NotNull String databaseName, @Nullable String usedByDatasource);

  boolean hasIdentifiersDatabase();

  @NotNull
  Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException;

  @NotNull
  DatasourceFactory createDataSourceFactory(@NotNull String datasourceName, @NotNull Database database);

  @Nullable
  Database getDefaultStorageDatabase();
}
