package org.obiba.opal.web.magma.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.DialectFactory;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.datasource.hibernate.support.AnnotationConfigurationHelper;
import org.obiba.magma.hibernate.cfg.MagmaNamingStrategy;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import com.google.common.base.Preconditions;

public class DatabaseSessionFactoryProvider implements SessionFactoryProvider {

  private String databaseName;

  @Autowired
  private transient JdbcDataSourceRegistry jdbcDataSourceRegistry;

  @Autowired
  private transient TransactionManager txmgr;

  @Autowired
  @Qualifier("hibernate")
  private transient Properties hibernateProperties;

  @Autowired
  private transient ApplicationContext ac;

  // Public ctor for XStream de-ser.
  public DatabaseSessionFactoryProvider() {

  }

  public DatabaseSessionFactoryProvider(ApplicationContext ac, String databaseName) {
    Preconditions.checkArgument(ac != null);
    Preconditions.checkArgument(databaseName != null);
    this.ac = ac;
    this.ac.getAutowireCapableBeanFactory().autowireBean(this);
    this.databaseName = databaseName;
  }

  @Override
  public SessionFactory getSessionFactory() {
    Preconditions.checkNotNull(databaseName);
    Preconditions.checkNotNull(jdbcDataSourceRegistry);
    DataSource dataSource = jdbcDataSourceRegistry.getDataSource(databaseName);

    AnnotationSessionFactoryBean asfb = new AnnotationSessionFactoryBean() {
      @Override
      protected void executeSchemaScript(Connection con, String[] sql) throws SQLException {
        Statement stmt = con.createStatement();
        try {
          for(String sqlStmt : sql) {
            executeSchemaStatement(stmt, sqlStmt);
          }
        } finally {
          JdbcUtils.closeStatement(stmt);
        }
      }
    };
    asfb.setDataSource(dataSource);
    asfb.setJtaTransactionManager(txmgr);
    asfb.setHibernateProperties(hibernateProperties);
    asfb.getHibernateProperties().setProperty(Environment.DIALECT, determineDialect(dataSource).getClass().getName());
    asfb.setAnnotatedClasses(new AnnotationConfigurationHelper().getAnnotatedTypes().toArray(new Class[] {}));
    asfb.setNamingStrategy(new MagmaNamingStrategy());
    asfb.setExposeTransactionAwareSessionFactory(false);

    asfb = (AnnotationSessionFactoryBean) ac.getAutowireCapableBeanFactory().initializeBean(asfb, databaseName + "-session");

    try {
      asfb.validateDatabaseSchema();
    } catch(DataAccessException dae) {
      asfb.updateDatabaseSchema();
    }

    return asfb.getObject();
  }

  private Dialect determineDialect(DataSource dataSource) {
    JdbcTemplate template = new JdbcTemplate(dataSource);
    return template.execute(new ConnectionCallback<Dialect>() {

      @Override
      public Dialect doInConnection(Connection connection) throws SQLException, DataAccessException {
        return DialectFactory.buildDialect(new Properties(), connection);
      }
    });
  }
}
