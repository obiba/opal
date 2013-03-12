/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.jdbc;

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
import org.obiba.magma.datasource.hibernate.support.AnnotationConfigurationHelper;
import org.obiba.magma.hibernate.cfg.MagmaNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class SessionFactoryFactory {

  private final static Logger log = LoggerFactory.getLogger(SessionFactoryFactory.class);

  @Autowired
  private ApplicationContext ac;

  @Autowired
  private TransactionManager txmgr;

  @Autowired
  @Qualifier("hibernate")
  private Properties hibernateProperties;

  public SessionFactory getSessionFactory(DataSource dataSource) {
    AnnotationSessionFactoryBean asfb = new CustomSessionFactoryBean();
    asfb.setDataSource(dataSource);
    asfb.setJtaTransactionManager(txmgr);
    asfb.setHibernateProperties(hibernateProperties);
    asfb.getHibernateProperties().setProperty(Environment.DIALECT, determineDialect(dataSource).getClass().getName());
    asfb.setAnnotatedClasses(new AnnotationConfigurationHelper().getAnnotatedTypes().toArray(new Class[] { }));
    asfb.setNamingStrategy(new MagmaNamingStrategy());
    asfb.setExposeTransactionAwareSessionFactory(false);

    // Inject dependencies
    asfb = (AnnotationSessionFactoryBean) ac.getAutowireCapableBeanFactory()
        .initializeBean(asfb, dataSource.hashCode() + "-session");

    onSessionFactoryBeanCreated(asfb);

    return asfb.getObject();
  }

  protected void onSessionFactoryBeanCreated(AnnotationSessionFactoryBean asfb) {
    try {
      log.info("Veryfying database schema.");
      asfb.validateDatabaseSchema();
    } catch(DataAccessException dae) {
      log.info("Invalid schema for hibernate datasource; updating schema.");
      try {
        asfb.updateDatabaseSchema();
      } catch(RuntimeException e) {
        log.error("Failed to update schema: {}", e.getMessage());
        throw e;
      }
    }
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

  private final static class CustomSessionFactoryBean extends AnnotationSessionFactoryBean {

    /**
     * Overridden to use the current autocommit setting on the connection.
     */
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
  }

}
