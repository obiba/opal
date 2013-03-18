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
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.opal.core.runtime.upgrade.support.UpgradeUtils;
import org.obiba.opal.core.support.TimedExecution;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.support.jdbc.SqlScriptUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SqlBinariesStorageUpgradeStep extends AbstractUpgradeStep {

  private final static Logger log = LoggerFactory.getLogger(SqlBinariesStorageUpgradeStep.class);

  private UpgradeUtils upgradeUtils;

  private SqlScriptUpgradeStep sqlScriptUpgradeStep;

  @Override
  public void execute(Version currentVersion) {

    Map<DataSource, String> dataSourceNames = upgradeUtils.getConfiguredDatasources();

    for(Map.Entry<DataSource, String> entry : dataSourceNames.entrySet()) {
      DataSource dataSource = entry.getKey();
      String dataSourceName = entry.getValue();
      if(UpgradeUtils.hasHibernateDatasource(dataSource)) {
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

  public void setSqlScriptUpgradeStep(SqlScriptUpgradeStep sqlScriptUpgradeStep) {
    this.sqlScriptUpgradeStep = sqlScriptUpgradeStep;
  }

  public void setUpgradeUtils(UpgradeUtils upgradeUtils) {
    this.upgradeUtils = upgradeUtils;
  }
}
