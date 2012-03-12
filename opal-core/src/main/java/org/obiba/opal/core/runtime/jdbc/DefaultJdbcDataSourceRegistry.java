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
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

  private final ConcurrentMap<String, BasicDataSource> dataSourceCache = Maps.newConcurrentMap();

  private final ConcurrentMap<String, SessionFactory> sessionFactoryCache = Maps.newConcurrentMap();

  @Autowired
  public DefaultJdbcDataSourceRegistry(DataSourceFactory dataSourceFactory, SessionFactoryFactory sessionFactoryFactory, OpalConfigurationService opalConfigService, @Qualifier("opal-datasource") DataSource opalDataSource) {
    this.dataSourceFactory = dataSourceFactory;
    this.sessionFactoryFactory = sessionFactoryFactory;
    this.configSupplier = new ExtensionConfigurationSupplier<JdbcDataSourcesConfig>(opalConfigService, JdbcDataSourcesConfig.class) {
    };
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
    for(BasicDataSource ds : dataSourceCache.values()) {
      try {
        ds.close();
      } catch(SQLException e) {
        log.warn("Ignoring exception during shutdown: ", e);
      }
    }
    dataSourceCache.clear();
  }

  @Override
  public DataSource getDataSource(String name) {
    if(name.equals(defaultDatasource.getName())) {
      return opalDataSource;
    }
    synchronized(dataSourceCache) {
      if(dataSourceCache.containsKey(name) == false) {
        dataSourceCache.put(name, dataSourceFactory.createDataSource(getJdbcDataSource(name)));
      }
    }
    return dataSourceCache.get(name);
  }

  @Override
  public SessionFactory getSessionFactory(String name) {
    synchronized(sessionFactoryCache) {
      if(sessionFactoryCache.containsKey(name) == false) {
        sessionFactoryCache.put(name, sessionFactoryFactory.getSessionFactory(getDataSource(name)));
      }
    }
    return sessionFactoryCache.get(name);
  }

  @Override
  public Iterable<JdbcDataSource> listDataSources() {
    return Iterables.concat(ImmutableList.of(defaultDatasource), get().datasources);
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
    return new JdbcDataSource(DEFAULT_NAME, bds.getUrl(), bds.getDriverClassName(), bds.getUsername(), bds.getPassword(), null).immutable();
  }

}
