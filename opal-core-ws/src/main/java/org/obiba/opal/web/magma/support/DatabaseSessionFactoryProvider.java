package org.obiba.opal.web.magma.support;

import javax.annotation.Nullable;

import org.hibernate.SessionFactory;
import org.obiba.magma.Disposable;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

public class DatabaseSessionFactoryProvider implements SessionFactoryProvider, Disposable {

  private String datasourceName;

  private String databaseName;

  // need to be transient because of XML serialization
  @SuppressWarnings("TransientFieldInNonSerializableClass")
  @Autowired
  private transient JdbcDataSourceRegistry jdbcDataSourceRegistry;

  // Public constructor for XStream de-ser.
  @SuppressWarnings("UnusedDeclaration")
  public DatabaseSessionFactoryProvider() {

  }

  public DatabaseSessionFactoryProvider(@Nullable String datasourceName,
      @Nullable JdbcDataSourceRegistry jdbcDataSourceRegistry, @Nullable String databaseName) {
    Preconditions.checkArgument(datasourceName != null);
    Preconditions.checkArgument(jdbcDataSourceRegistry != null);
    Preconditions.checkArgument(databaseName != null);
    this.datasourceName = datasourceName;
    this.databaseName = databaseName;
    this.jdbcDataSourceRegistry = jdbcDataSourceRegistry;
  }

  @Override
  public SessionFactory getSessionFactory() {
    Preconditions.checkNotNull(databaseName);
    Preconditions.checkNotNull(jdbcDataSourceRegistry);
    return jdbcDataSourceRegistry.getSessionFactory(databaseName, datasourceName);
  }

  @Override
  public void dispose() {
    jdbcDataSourceRegistry.unregister(databaseName, datasourceName);
  }
}
