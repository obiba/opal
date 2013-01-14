/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server;

import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand {
  //
  // Constants
  //

  private static final String[] CONTEXT_PATHS = {"classpath:/META-INF/spring/opal-server/upgrade.xml"};

  //
  // AbstractContextLoadingCommand Methods
  //

  public void execute() {
    ConfigurableApplicationContext ctx = loadContext();

    try {
      UpgradeManager upgradeManager = (UpgradeManager) ctx.getBean("upgradeManager");
      try {
        upgradeManager.executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the upgrade manager", upgradeFailed);
      }
    } finally {
      ctx.close();
    }
  }

  private ConfigurableApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext(CONTEXT_PATHS);
  }
}