/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.obiba.opal.cli.client.command.options.UpgradeCommandOptions;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand extends AbstractContextLoadingCommand<UpgradeCommandOptions> {
  //
  // Constants
  //

  private static final String[] CONTEXT_PATHS = { // 
  "classpath:/spring/opal-core/upgrade-context.xml", // 
  "classpath:/spring/opal-core/version.xml", //
  "classpath:/spring/opal-core/upgrade.xml" //
  };

  //
  // AbstractContextLoadingCommand Methods
  //

  public void executeWithContext() {
    new UserAuthentication(options).authenticate();

    String runtimeDatabase = getRuntimeDatabase();

    if(supportsDatabase(runtimeDatabase)) {
      UpgradeManager upgradeManager = getBean("upgradeManager");
      try {
        upgradeManager.executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the upgrade manager", upgradeFailed);
      }
    } else {
      throw new RuntimeException("Upgrade manager does not support database (" + runtimeDatabase + ")");
    }
  }

  protected ConfigurableApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext(CONTEXT_PATHS);
  }

  //
  // Methods
  //

  private boolean supportsDatabase(String runtimeDatabase) {
    return (runtimeDatabase != null && runtimeDatabase.equals("MySQL"));
  }

  private String getRuntimeDatabase() {
    String runtimeDatabase = null;

    DataSource dataSource = getBean("opalDataSource");

    try {
      runtimeDatabase = (String) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
          return dbmd.getDatabaseProductName();
        }
      });
    } catch(MetaDataAccessException ex) {
      throw new RuntimeException("Could not determine runtime database", ex);
    }

    return runtimeDatabase;
  }
}