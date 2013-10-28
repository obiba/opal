package org.obiba.opal.core.runtime.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.validation.ConstraintViolationException;

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

  @Nonnull
  Database getDatabase(@Nonnull String name) throws NoSuchDatabaseException;

  void saveDatabase(@Nonnull Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException;

  void deleteDatabase(@Nonnull Database database) throws CannotDeleteDatabaseWithDataException;

  DataSource getDataSource(@Nonnull String name, @Nullable String usedByDatasource);

  SessionFactory getSessionFactory(@Nonnull String name, @Nullable String usedByDatasource);

  void unregister(@Nonnull String databaseName, @Nullable String usedByDatasource);

  boolean hasIdentifiersDatabase();

  @Nonnull
  Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException;

  @Nonnull
  DatasourceFactory createDataSourceFactory(@Nonnull String datasourceName, @Nonnull Database database);

  @Nullable
  Database getDefaultStorageDatabase();
}
