/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.jdbc;

import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.obiba.magma.datasource.hibernate.cfg.HibernateConfigurationHelper;
import org.obiba.magma.datasource.hibernate.cfg.MagmaDialectResolver;
import org.obiba.magma.datasource.hibernate.cfg.MagmaNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;

import static org.hibernate.cfg.AvailableSettings.*;

@Component
public class SessionFactoryFactory {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private JtaTransactionManager jtaTransactionManager;

  // need to run outside the transaction so HBM2DDL_AUTO can change auto-commit to update schema
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public SessionFactory getSessionFactory(DataSource dataSource) {
    Set<Class<?>> annotatedTypes = HibernateConfigurationHelper.getAnnotatedTypes();
    LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setAnnotatedClasses(annotatedTypes.toArray(new Class[annotatedTypes.size()]));
    factory.setJtaTransactionManager(jtaTransactionManager);
    factory.setNamingStrategy(new MagmaNamingStrategy());
    factory.getHibernateProperties().setProperty(HBM2DDL_AUTO, "update");
    factory.getHibernateProperties().setProperty(GENERATE_STATISTICS, "false");
    factory.getHibernateProperties().setProperty(USE_STRUCTURED_CACHE, "true");
    factory.getHibernateProperties().setProperty(USE_QUERY_CACHE, "true");
    factory.getHibernateProperties().setProperty(USE_SECOND_LEVEL_CACHE, "true");
    factory.getHibernateProperties()
        .setProperty(CACHE_REGION_FACTORY, NoShutdownSingletonEhCacheRegionFactory.class.getName());
    factory.getHibernateProperties().setProperty(CURRENT_SESSION_CONTEXT_CLASS, "jta");
    factory.getHibernateProperties().setProperty(AUTO_CLOSE_SESSION, "true");
    factory.getHibernateProperties().setProperty(FLUSH_BEFORE_COMPLETION, "true");
    factory.getHibernateProperties().setProperty(DIALECT_RESOLVERS, MagmaDialectResolver.class.getName());

    return ((LocalSessionFactoryBean) applicationContext.getAutowireCapableBeanFactory()
        .initializeBean(factory, dataSource.hashCode() + "-sessionFactory")).getObject();
  }

  /**
   * Never shutdown CacheManager
   */
  public static class NoShutdownSingletonEhCacheRegionFactory extends SingletonEhCacheRegionFactory {

    private static final long serialVersionUID = 4004496611012448022L;

    @Override
    public void stop() {
      if(manager != null) manager = null;
    }
  }

}
