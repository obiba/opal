package org.obiba.opal.core.runtime.jdbc;

import javax.annotation.Nonnull;

import org.hibernate.SessionFactory;
import org.obiba.magma.Disposable;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

public class DatabaseSessionFactoryProvider implements SessionFactoryProvider, Disposable {

  private String datasourceName;

  private String databaseName;

  // need to be transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  @Autowired
  private transient DatabaseRegistry databaseRegistry;

  // Public constructor for XStream de-ser.
  @SuppressWarnings("UnusedDeclaration")
  public DatabaseSessionFactoryProvider() {

  }

  @SuppressWarnings("ConstantConditions")
  public DatabaseSessionFactoryProvider(@Nonnull String datasourceName, @Nonnull DatabaseRegistry databaseRegistry,
      @Nonnull String databaseName) {
    Preconditions.checkArgument(datasourceName != null);
    Preconditions.checkArgument(databaseRegistry != null);
    Preconditions.checkArgument(databaseName != null);
    this.datasourceName = datasourceName;
    this.databaseName = databaseName;
    this.databaseRegistry = databaseRegistry;
  }

  @Override
  public SessionFactory getSessionFactory() {
    return databaseRegistry.getSessionFactory(databaseName, datasourceName);
  }

  @Override
  public void dispose() {
    databaseRegistry.unregister(databaseName, datasourceName);
  }
}
