package org.obiba.opal.core.runtime.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.obiba.opal.core.domain.database.Database;

public interface DatabaseRegistry {

  Iterable<Database> list();

  <T extends Database> Iterable<T> list(@Nonnull Class<T> databaseClass);

  Iterable<Database> list(@Nullable String type);

  Database getDatabase(@Nonnull String name);

  void addOrReplaceDatabase(@Nonnull Database database)
      throws DuplicateDatabaseNameException, MultipleIdentifiersDatabaseException, CannotChangeDatabaseNameException;

  void deleteDatabase(@Nonnull Database database);

  DataSource getDataSource(@Nonnull String name, @Nullable String usedByDatasource);

  SessionFactory getSessionFactory(@Nonnull String name, @Nullable String usedByDatasource);

  void unregister(@Nonnull String databaseName, @Nullable String usedByDatasource);

  @Nullable
  Database getIdentifiersDatabase();
}
