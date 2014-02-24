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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.hibernate.SessionFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;
import org.hibernate.cache.ehcache.internal.util.HibernateUtil;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.service.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.service.jdbc.dialect.internal.DialectFactoryImpl;
import org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver;
import org.obiba.magma.datasource.hibernate.cfg.HibernateConfigurationHelper;
import org.obiba.magma.datasource.hibernate.cfg.MagmaHSQLDialect;
import org.obiba.magma.datasource.hibernate.cfg.MagmaNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.hibernate.cfg.AvailableSettings.*;

@Component
public class SessionFactoryFactory {

  private static final Logger log = LoggerFactory.getLogger(SessionFactoryFactory.class);

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
    factory.getHibernateProperties().setProperty(CACHE_REGION_FACTORY, NamedEhCacheRegionFactory.class.getName());
    factory.getHibernateProperties().setProperty(CURRENT_SESSION_CONTEXT_CLASS, "jta");
    factory.getHibernateProperties().setProperty(AUTO_CLOSE_SESSION, "true");
    factory.getHibernateProperties().setProperty(FLUSH_BEFORE_COMPLETION, "true");
    factory.getHibernateProperties().setProperty(DIALECT, guessDialect(dataSource, factory));
    return ((LocalSessionFactoryBean) applicationContext.getAutowireCapableBeanFactory()
        .initializeBean(factory, dataSource.hashCode() + "-sessionFactory")).getObject();
  }

  private String guessDialect(DataSource dataSource, final LocalSessionFactoryBean factory) {
    Dialect dialect = new JdbcTemplate(dataSource).execute(new ConnectionCallback<Dialect>() {
      @Override
      public Dialect doInConnection(Connection connection) throws SQLException, DataAccessException {
        return dialectFactory.buildDialect(factory.getHibernateProperties(), connection);
      }
    });
    return dialect instanceof HSQLDialect ? MagmaHSQLDialect.class.getName() : dialect.getClass().getName();
  }

  /**
   * Used to set name to cache manager to avoid net.sf.ehcache.CacheException:
   * <pre>
   *   net.sf.ehcache.CacheException:
   *   Another unnamed CacheManager already exists in the same VM. Please provide unique names for each CacheManager in the config or do one of following:
   *    1. Use one of the CacheManager.create() static factory methods to reuse same CacheManager with same name or create one if necessary
   *    2. Shutdown the earlier cacheManager before creating new one with same name.
   * </pre>
   * <p/>
   * See https://hibernate.atlassian.net/browse/HHH-7809
   */
  public static class NamedEhCacheRegionFactory extends EhCacheRegionFactory {

    private static final long serialVersionUID = -906012674778107555L;

    @Override
    @SuppressWarnings({ "ParameterHidesMemberVariable", "OverlyLongMethod", "PMD.NcssMethodCount" })
    public void start(Settings settings, Properties properties) throws CacheException {
      this.settings = settings;
      if(manager != null) {
        log.warn(
            "Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() between repeated calls to " +
                "buildSessionFactory. Using previously created EhCacheProvider. If this behaviour is required, consider " +
                "using net.sf.ehcache.hibernate.SingletonEhCacheProvider.");
        return;
      }

      try {
        String configurationResourceName = null;
        if(properties != null) {
          configurationResourceName = (String) properties.get(NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME);
        }
        if(configurationResourceName == null || configurationResourceName.isEmpty()) {
          Configuration configuration = ConfigurationFactory.parseConfiguration();
          // give the CacheManager some unique name. we could also use UUID or whatever
          configuration.setName(toString());
          manager = new CacheManager(configuration);
        } else {
          URL url;
          try {
            url = new URL(configurationResourceName);
          } catch(MalformedURLException e) {
            url = loadResource(configurationResourceName);
          }
          Configuration configuration = HibernateUtil.loadAndCorrectConfiguration(url);
          manager = new CacheManager(configuration);
        }
        mbeanRegistrationHelper.registerMBean(manager, properties);
      } catch(net.sf.ehcache.CacheException e) {
        if(e.getMessage().startsWith("Cannot parseConfiguration CacheManager. Attempt to create a new instance of " +
            "CacheManager using the diskStorePath")) {
          throw new CacheException("Attempt to restart an already started EhCacheRegionFactory. " +
              "Use sessionFactory.close() between repeated calls to buildSessionFactory. " +
              "Consider using SingletonEhCacheRegionFactory. Error from ehcache was: " + e.getMessage());
        }
        throw new CacheException(e);
      }
    }
  }

}
