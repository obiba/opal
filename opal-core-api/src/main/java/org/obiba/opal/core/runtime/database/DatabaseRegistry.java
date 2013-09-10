package org.obiba.opal.core.runtime.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.service.SystemService;

public interface DatabaseRegistry extends SystemService {

  Iterable<Database> list();

  <T extends Database> Iterable<T> list(@Nonnull Class<T> databaseClass);

  Iterable<Database> list(@Nullable String type);

  @Nullable
  Database getDatabase(@Nonnull String name);

  void addOrReplaceDatabase(@Nonnull Database database)
      throws MultipleIdentifiersDatabaseException, DatabaseAlreadyExistsException;

  void deleteDatabase(@Nonnull Database database) throws CannotDeleteDatabaseWithDataException;

  DataSource getDataSource(@Nonnull String name, @Nullable String usedByDatasource);

  SessionFactory getSessionFactory(@Nonnull String name, @Nullable String usedByDatasource);

  void unregister(@Nonnull String databaseName, @Nullable String usedByDatasource);

  @Nullable
  Database getIdentifiersDatabase();

  Datasource createStorageMagmaDatasource(String datasourceName, Database database);
}
