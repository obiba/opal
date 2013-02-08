/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade.binary;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSource;
import org.obiba.opal.core.runtime.support.OpalConfigurationProvider;
import org.obiba.opal.core.support.TimedExecution;
import org.obiba.runtime.Version;
import org.obiba.runtime.jdbc.DatabaseProduct;
import org.obiba.runtime.jdbc.DatabaseProductRegistry;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.support.jdbc.SqlScriptUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig;

/**
 *
 */
public class SqlBinariesTableEngineUpgradeStep extends AbstractUpgradeStep {

  private final static Logger log = LoggerFactory.getLogger(SqlBinariesTableEngineUpgradeStep.class);

  private OpalConfigurationProvider opalConfigurationProvider;

  private DataSource opalDataSource;

  private DataSource keyDataSource;

  private DataSourceFactory dataSourceFactory;

  private SqlScriptUpgradeStep sqlScriptUpgradeStep;

  private final Map<DataSource, String> dataSourceNames = new LinkedHashMap<DataSource, String>();

  @Override
  public void execute(Version currentVersion) {

    dataSourceNames.put(opalDataSource, "Default");
    dataSourceNames.put(keyDataSource, "Key");

    OpalConfiguration configuration = opalConfigurationProvider.readOpalConfiguration(true);
    try {
      JdbcDataSourcesConfig dataSourcesConfig = configuration.getExtension(JdbcDataSourcesConfig.class);
      for(JdbcDataSource jdbcDataSource : dataSourcesConfig.getDatasources()) {
        dataSourceNames.put(dataSourceFactory.createDataSource(jdbcDataSource), jdbcDataSource.getName());
      }
    } catch(NoSuchElementException e) {
      // ignore
    }

    for(Map.Entry<DataSource, String> entry : dataSourceNames.entrySet()) {
      DataSource dataSource = entry.getKey();
      String dataSourceName = entry.getValue();
      log.debug("Analyse datasource {}", dataSourceName);
      if(hasNonInnoDbBinaryValuesTable(dataSource)) {
        upgradeSchema(currentVersion, dataSource, dataSourceName);
      }
    }
  }

  private void upgradeSchema(Version currentVersion, DataSource dataSource, String name) {
    log.info("Start schema upgrade for database {}", name);
    TimedExecution timedExecution = new TimedExecution().start();
    sqlScriptUpgradeStep.setDataSource(dataSource);
    try {
      sqlScriptUpgradeStep.initialize();
    } catch(IOException e) {
      throw new RuntimeException("Cannot upgrade schema for binaries table engine", e);
    }
    sqlScriptUpgradeStep.execute(currentVersion);

    log.info("Database {}: schema upgraded in {}", name, timedExecution.end().formatExecutionTime());
  }

  private static boolean hasNonInnoDbBinaryValuesTable(DataSource dataSource) {
    try {
      DatabaseProduct product = new DatabaseProductRegistry().getDatabaseProduct(dataSource);
      if("mysql".equals(product.getNormalizedName())) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        String engine = template.queryForObject(
            "SELECT `engine` FROM information_schema.tables WHERE table_name = ? AND table_schema = database()",
            new Object[] { "value_set_binary_value" }, String.class);
        return !"innodb".equalsIgnoreCase(engine);
      }
    } catch(DataAccessException e) {
      log.error("Cannot check if database has a non InnoDB value_set_binary_value table", e);
    }
    return false;
  }

  public void setOpalDataSource(DataSource opalDataSource) {
    this.opalDataSource = opalDataSource;
  }

  public void setOpalConfigurationProvider(OpalConfigurationProvider opalConfigurationProvider) {
    this.opalConfigurationProvider = opalConfigurationProvider;
  }

  public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
    this.dataSourceFactory = dataSourceFactory;
  }

  public void setSqlScriptUpgradeStep(SqlScriptUpgradeStep sqlScriptUpgradeStep) {
    this.sqlScriptUpgradeStep = sqlScriptUpgradeStep;
  }

  public void setKeyDataSource(DataSource keyDataSource) {
    this.keyDataSource = keyDataSource;
  }

}
