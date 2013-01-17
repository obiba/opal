/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSource;
import org.obiba.opal.core.runtime.support.OpalConfigurationProvider;
import org.obiba.opal.core.support.TimedExecution;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.support.jdbc.SqlScriptUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry.JdbcDataSourcesConfig;

/**
 *
 */
public class SqlBinariesStorageUpgradeStep extends AbstractUpgradeStep {

  private final static Logger log = LoggerFactory.getLogger(SqlBinariesStorageUpgradeStep.class);

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

    OpalConfiguration configuration = opalConfigurationProvider.readOpalConfiguration();
    JdbcDataSourcesConfig dataSourcesConfig = configuration.getExtension(JdbcDataSourcesConfig.class);
    for(JdbcDataSource jdbcDataSource : dataSourcesConfig.getDatasources()) {
      dataSourceNames.put(dataSourceFactory.createDataSource(jdbcDataSource), jdbcDataSource.getName());
    }

    for(Map.Entry<DataSource, String> entry : dataSourceNames.entrySet()) {
      DataSource dataSource = entry.getKey();
      String dataSourceName = entry.getValue();
      if(hasHibernateDatasource(dataSource)) {
        upgradeSchema(currentVersion, dataSource, dataSourceName);
      }
    }
  }

  private void upgradeSchema(Version currentVersion, DataSource dataSource, String name) {
    TimedExecution timedExecution = new TimedExecution().start();

    sqlScriptUpgradeStep.setDataSource(dataSource);
    try {
      sqlScriptUpgradeStep.initialize();
    } catch(IOException e) {
      throw new RuntimeException("Cannot upgrade schema for binaries storage", e);
    }
    sqlScriptUpgradeStep.execute(currentVersion);

    log.info("Database {}: schema upgraded in {}", name, timedExecution.end().formatExecutionTime());
  }

  static boolean hasHibernateDatasource(DataSource dataSource) {
    try {
      DatabaseMetaData meta = dataSource.getConnection().getMetaData();
      ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });
      while(res.next()) {
        if("value_set_value".equalsIgnoreCase(res.getString("TABLE_NAME"))) return true;
      }
    } catch(SQLException e) {
      log.error("Cannot check if database has an HibernateDatasource", e);
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
