/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x;

import java.io.IOException;

import javax.sql.DataSource;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.support.jdbc.SqlScriptUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;

public class HibernateSqlOrderColumnsUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  private final SqlScriptUpgradeStep sqlScriptUpgradeStep;

  public HibernateSqlOrderColumnsUpgradeStep() {
    sqlScriptUpgradeStep = new SqlScriptUpgradeStep();
    sqlScriptUpgradeStep
        .setScriptPath(new DefaultResourceLoader().getResource("classpath:/META-INF/opal/upgrade-scripts/2.0.x/"));
    sqlScriptUpgradeStep.setScriptBasename("nullable-order-columns");
    sqlScriptUpgradeStep.setAppliesTo(new Version(2, 0, 0));
  }

  @Override
  public void execute(Version currentVersion) {
    for(Database database : databaseRegistry.listSqlDatabases()) {
      if(database.getSqlSettings() != null &&
          database.getSqlSettings().getSqlSchema() == SqlSettings.SqlSchema.HIBERNATE) {
        upgradeSchema(currentVersion, databaseRegistry.getDataSource(database.getName(), null), database.getName());
      }
    }
  }

  private void upgradeSchema(Version currentVersion, DataSource dataSource, String name) {
    try {
      sqlScriptUpgradeStep.setDataSource(dataSource);
      sqlScriptUpgradeStep.initialize();
      sqlScriptUpgradeStep.execute(currentVersion);
    } catch(IOException e) {
      throw new RuntimeException("Cannot upgrade " + name + " schema", e);
    }
  }

}
