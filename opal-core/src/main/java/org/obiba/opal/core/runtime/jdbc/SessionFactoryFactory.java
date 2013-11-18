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
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.hibernate.SessionFactory;
import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.service.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.service.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver;
import org.obiba.magma.datasource.hibernate.cfg.HibernateConfigurationHelper;
import org.obiba.magma.datasource.hibernate.cfg.MagmaNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import static org.hibernate.cfg.AvailableSettings.*;

@Component
public class SessionFactoryFactory {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private TransactionManager jtaTransactionManager;

  private final DialectFactoryImpl dialectFactory;

  public SessionFactoryFactory() {
    dialectFactory = new DialectFactoryImpl();
    dialectFactory.setClassLoaderService(new ClassLoaderServiceImpl(getClass().getClassLoader()));
    dialectFactory.setDialectResolver(new StandardDialectResolver());
  }

  public SessionFactory getSessionFactory(DataSource dataSource) {

    Set<Class<?>> annotatedTypes = HibernateConfigurationHelper.getAnnotatedTypes();

    final LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setAnnotatedClasses(annotatedTypes.toArray(new Class[annotatedTypes.size()]));
    factory.setJtaTransactionManager(jtaTransactionManager);
    factory.setNamingStrategy(new MagmaNamingStrategy());
    factory.getHibernateProperties().setProperty(HBM2DDL_AUTO, "update");
    factory.getHibernateProperties().setProperty(GENERATE_STATISTICS, "false");
    factory.getHibernateProperties().setProperty(USE_STRUCTURED_CACHE, "true");
    factory.getHibernateProperties().setProperty(USE_QUERY_CACHE, "true");
    factory.getHibernateProperties().setProperty(USE_SECOND_LEVEL_CACHE, "true");
    factory.getHibernateProperties().setProperty(CACHE_REGION_FACTORY, SingletonEhCacheRegionFactory.class.getName());
    factory.getHibernateProperties().setProperty(CURRENT_SESSION_CONTEXT_CLASS, "jta");
    factory.getHibernateProperties().setProperty(AUTO_CLOSE_SESSION, "true");
    factory.getHibernateProperties().setProperty(FLUSH_BEFORE_COMPLETION, "true");
    Dialect dialect = new JdbcTemplate(dataSource).execute(new ConnectionCallback<Dialect>() {
      @Override
      public Dialect doInConnection(Connection connection) throws SQLException, DataAccessException {
        return dialectFactory.buildDialect(factory.getHibernateProperties(), connection);
      }
    });
    factory.getHibernateProperties().setProperty(DIALECT,
        dialect instanceof HSQLDialect ? MagmaHSQLDialect.class.getName() : dialect.getClass().getName());

    // Inject dependencies
    return ((LocalSessionFactoryBean) applicationContext.getAutowireCapableBeanFactory()
        .initializeBean(factory, dataSource.hashCode() + "-sessionFactory")).getObject();
  }

}
