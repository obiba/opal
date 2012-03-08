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
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier.ExtensionConfigModificationTask;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  private final DataSourceFactory dataSourceFactory;

  private final ExtensionConfigurationSupplier<JdbcDataSourcesConfig> configSupplier;

  private final ConcurrentMap<String, BasicDataSource> dataSourceCache = Maps.newConcurrentMap();

  @Autowired
  public DefaultJdbcDataSourceRegistry(DataSourceFactory dataSourceFactory, OpalConfigurationService opalConfigService) {
    this.dataSourceFactory = dataSourceFactory;
    configSupplier = new ExtensionConfigurationSupplier<JdbcDataSourcesConfig>(opalConfigService, JdbcDataSourcesConfig.class) {
    };
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
    synchronized(dataSourceCache) {
      if(dataSourceCache.containsKey(name) == false) {
        dataSourceCache.put(name, dataSourceFactory.createDataSource(getJdbcDataSource(name)));
      }
    }
    return dataSourceCache.get(name);
  }

  @Override
  public Iterable<JdbcDataSource> listDataSources() {
    return ImmutableList.copyOf(get().datasources);
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
    configSupplier.modify(new ExtensionConfigModificationTask<DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig>() {

      @Override
      public void doWithConfig(JdbcDataSourcesConfig config) {
        config.datasources.remove(jdbcDataSource);
      }
    });
  }

  @Override
  public void registerDataSource(final JdbcDataSource jdbcDataSource) {
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
}
