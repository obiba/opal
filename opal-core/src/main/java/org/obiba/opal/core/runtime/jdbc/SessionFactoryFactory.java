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
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.service.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.service.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.obiba.magma.datasource.hibernate.cfg.HibernateConfigurationHelper;
import org.obiba.magma.datasource.hibernate.cfg.MagmaNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class SessionFactoryFactory {

  private final static Logger log = LoggerFactory.getLogger(SessionFactoryFactory.class);

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  @Qualifier("hibernate")
  private Properties hibernateProperties;

  private final DialectFactoryImpl dialectFactory = new DialectFactoryImpl();

  public SessionFactoryFactory() {
    dialectFactory.setClassLoaderService(new ClassLoaderServiceImpl(getClass().getClassLoader()));
    dialectFactory.setDialectResolver(new StandardDialectResolver());
  }

  public SessionFactory getSessionFactory(DataSource dataSource) {

    String beanName = dataSource.hashCode() + "-sessionFactory";

    Set<Class<?>> annotatedTypes = new HibernateConfigurationHelper().getAnnotatedTypes();
    Dialect dialect = determineDialect(dataSource);

    LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
    factoryBean.setDataSource(dataSource);
    factoryBean.setHibernateProperties(hibernateProperties);
    factoryBean.getHibernateProperties().setProperty(Environment.DIALECT, dialect.getClass().getName());
    factoryBean.setAnnotatedClasses(annotatedTypes.toArray(new Class[annotatedTypes.size()]));
    factoryBean.setNamingStrategy(new MagmaNamingStrategy());

    // Inject dependencies
    factoryBean = (LocalSessionFactoryBean) applicationContext.getAutowireCapableBeanFactory()
        .initializeBean(factoryBean, beanName);

    onSessionFactoryBeanCreated(factoryBean);

    return factoryBean.getObject();
  }

  protected void onSessionFactoryBeanCreated(LocalSessionFactoryBean factoryBean) {
    try {
      log.info("Verifying database schema.");
      new SchemaValidator(factoryBean.getConfiguration()).validate();
    } catch(HibernateException dae) {
      log.info("Invalid schema for hibernate datasource; updating schema.");
      try {
        new SchemaUpdate(factoryBean.getConfiguration()).execute(false, true);
      } catch(RuntimeException e) {
        log.error("Failed to update schema: {}", e.getMessage());
        throw e;
      }
    }
  }

  private Dialect determineDialect(DataSource dataSource) {
    return new JdbcTemplate(dataSource).execute(new ConnectionCallback<Dialect>() {
      @Override
      public Dialect doInConnection(Connection connection) throws SQLException, DataAccessException {
        return dialectFactory.buildDialect(hibernateProperties, connection);
      }
    });
  }

}
