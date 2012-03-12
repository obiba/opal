package org.obiba.opal.web.magma.support;

import org.hibernate.SessionFactory;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

public class DatabaseSessionFactoryProvider implements SessionFactoryProvider {

  private String databaseName;

  @Autowired
  private transient JdbcDataSourceRegistry jdbcDataSourceRegistry;

  // Public ctor for XStream de-ser.
  public DatabaseSessionFactoryProvider() {

  }

  public DatabaseSessionFactoryProvider(JdbcDataSourceRegistry jdbcDataSourceRegistry, String databaseName) {
    Preconditions.checkArgument(jdbcDataSourceRegistry != null);
    Preconditions.checkArgument(databaseName != null);
    this.databaseName = databaseName;
    this.jdbcDataSourceRegistry = jdbcDataSourceRegistry;
  }

  @Override
  public SessionFactory getSessionFactory() {
    Preconditions.checkNotNull(databaseName);
    Preconditions.checkNotNull(jdbcDataSourceRegistry);
    return jdbcDataSourceRegistry.getSessionFactory(databaseName);
  }
}
