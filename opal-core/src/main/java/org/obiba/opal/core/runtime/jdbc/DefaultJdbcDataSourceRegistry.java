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

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier.ExtensionConfigModificationTask;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

@Component
public class DefaultJdbcDataSourceRegistry implements JdbcDataSourceRegistry, Service {

  private static final Logger log = LoggerFactory.getLogger(DefaultJdbcDataSourceRegistry.class);

  public static class JdbcDataSourcesConfig implements OpalConfigurationExtension {

    private List<JdbcDataSource> datasources = Lists.newArrayList();

  }

  private static final String DEFAULT_NAME = "<default>";

  private final JdbcDataSource defaultDatasource;

  private final DataSource opalDataSource;

  private final DataSourceFactory dataSourceFactory;

  private final SessionFactoryFactory sessionFactoryFactory;

  private final ExtensionConfigurationSupplier<JdbcDataSourcesConfig> configSupplier;

  private final Cache<String, BasicDataSource> dataSourceCache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, BasicDataSource>() {

        @Override
        public void onRemoval(RemovalNotification<String, BasicDataSource> notification) {
          try {
            log.info("Destroying DataSource {}", notification.getKey());
            notification.getValue().close();
          } catch(SQLException e) {
            log.warn("Ignoring exception during shutdown: ", e);
          }
        }
      }).build(new CacheLoader<String, BasicDataSource>() {

        @Override
        public BasicDataSource load(String key) throws Exception {
          log.info("Building DataSource {}", key);
          return dataSourceFactory.createDataSource(getJdbcDataSource(key));
        }
      });

  private final Cache<String, SessionFactory> sessionFactoryCache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, SessionFactory>() {
        @Override
        public void onRemoval(RemovalNotification<String, SessionFactory> notification) {
          try {
            log.info("Destroying session factory {}", notification.getKey());
            notification.getValue().close();
          } catch(HibernateException e) {
            log.warn("Ignoring exception during shutdown: ", e);
          }
        }
      }).build(new CacheLoader<String, SessionFactory>() {

        @Override
        public SessionFactory load(String key) throws Exception {
          log.info("Building SessionFactory {}", key);
          return sessionFactoryFactory.getSessionFactory(getDataSource(key, null));
        }
      });

  private final SetMultimap<String, String> registrations = Multimaps
      .synchronizedSetMultimap(HashMultimap.<String, String>create());

  @Autowired
  public DefaultJdbcDataSourceRegistry(DataSourceFactory dataSourceFactory, SessionFactoryFactory sessionFactoryFactory,
      OpalConfigurationService opalConfigService, @Qualifier("opal-datasource") DataSource opalDataSource) {
    this.dataSourceFactory = dataSourceFactory;
    this.sessionFactoryFactory = sessionFactoryFactory;
    this.configSupplier = new ExtensionConfigurationSupplier<JdbcDataSourcesConfig>(opalConfigService,
        JdbcDataSourcesConfig.class);
    this.opalDataSource = opalDataSource;
    this.defaultDatasource = buildDefaultDataSource(opalDataSource);

  }

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {
    sessionFactoryCache.invalidateAll();
    dataSourceCache.invalidateAll();
  }

  @Override
  public String getName() {
    return "databases";
  }

  @Override
  public DataSource getDataSource(String name, String usedBy) {
    if(defaultDatasource.getName().equals(name)) {
      return opalDataSource;
    }
    if(usedBy != null) {
      registrations.put(name, usedBy);
    }
    return dataSourceCache.getUnchecked(name);
  }

  @Override
  public SessionFactory getSessionFactory(String name, String usedBy) {
    if(usedBy != null) {
      registrations.put(name, usedBy);
    }
    return sessionFactoryCache.getUnchecked(name);
  }

  @Override
  public void unregister(String databaseName, String usedBy) {
    registrations.remove(databaseName, usedBy);
  }

  @Override
  public Iterable<JdbcDataSource> listDataSources() {
    return Iterables.transform(Iterables.concat(ImmutableList.of(defaultDatasource), get().datasources),
        new Function<JdbcDataSource, JdbcDataSource>() {

          @Override
          public JdbcDataSource apply(JdbcDataSource input) {
            if(input.getName().equals(DEFAULT_NAME) || registrations.containsKey(input.getName())) {
              return input.immutable();
            }
            return input.mutable();
          }
        });
  }

  @Override
  public JdbcDataSource getJdbcDataSource(final String name) {
    return Iterables.find(listDataSources(), new Predicate<JdbcDataSource>() {
      @Override
      public boolean apply(JdbcDataSource input) {
        return input.getName().equals(name);
      }
    });
  }

  @Override
  public void update(final JdbcDataSource jdbcDataSource) {
    if(getJdbcDataSource(jdbcDataSource.getName()).isEditable() == false) return;
    configSupplier.modify(new ExtensionConfigModificationTask<DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig>() {

      @Override
      public void doWithConfig(JdbcDataSourcesConfig config) {
        int index = config.datasources.indexOf(jdbcDataSource);
        if(index > -1) {
          config.datasources.set(index, jdbcDataSource);
        }
        destroyDataSource(jdbcDataSource.getName());
      }
    });
  }

  @Override
  public void remove(final JdbcDataSource jdbcDataSource) {
    if(getJdbcDataSource(jdbcDataSource.getName()).isEditable() == false) return;
    configSupplier.modify(new ExtensionConfigModificationTask<DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig>() {

      @Override
      public void doWithConfig(JdbcDataSourcesConfig config) {
        config.datasources.remove(jdbcDataSource);
        destroyDataSource(jdbcDataSource.getName());
      }
    });
  }

  @Override
  public void registerDataSource(final JdbcDataSource jdbcDataSource) {
    if(jdbcDataSource.getName().equals(DEFAULT_NAME)) return;
    configSupplier.modify(new ExtensionConfigModificationTask<DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig>() {

      @Override
      public void doWithConfig(JdbcDataSourcesConfig config) {
        config.datasources.add(jdbcDataSource);
      }
    });
  }

  private JdbcDataSourcesConfig get() {
    if(configSupplier.hasExtension() == false) {
      configSupplier.addExtension(new JdbcDataSourcesConfig());
    }
    return configSupplier.get();
  }

  private JdbcDataSource buildDefaultDataSource(DataSource opalDataSource) {
    BasicDataSource bds = (BasicDataSource) opalDataSource;
    return new JdbcDataSource(DEFAULT_NAME, bds.getUrl(), bds.getDriverClassName(), bds.getUsername(),
        bds.getPassword(), null).immutable();
  }

  private void destroyDataSource(String name) {
    sessionFactoryCache.invalidate(name);
    dataSourceCache.invalidate(name);
  }

}
