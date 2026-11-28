/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server;

import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand {

  private static final String[] CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade.xml" };

  public void execute() {
    // Before the context: loading it creates the configuration database, which a 5.x installation must not get here.
    checkConfigurationMigrated(new File(System.getProperty("OPAL_HOME"), "data/opal-config.xml"));
    try(ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(CONTEXT_PATHS)) {
      try {
        ctx.getBean("upgradeManager", UpgradeManager.class).executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the upgrade manager", upgradeFailed);
      }
    }
  }

  /**
   * Opal 6.0 to 6.2 migrate the configuration out of OrientDB; this version no longer can. An installation below 6.0
   * has to go through 6.2 first.
   */
  static void checkConfigurationMigrated(File opalConfig) {
    if(!opalConfig.exists()) return; // new installation
    String major;
    try {
      major = XPathFactory.newInstance().newXPath()
          .evaluate("/*/version/major", new InputSource(opalConfig.toURI().toString())).trim();
    } catch(XPathExpressionException e) {
      throw new IllegalStateException("Cannot read the version of this Opal from " + opalConfig.getAbsolutePath(), e);
    }
    if(!major.isEmpty() && Integer.parseInt(major) < 6) {
      throw new IllegalStateException("This Opal is version " + major + ".x and its configuration is still in " +
          "OrientDB, which this version cannot migrate. Install Opal 6.2, start it once to migrate the configuration, " +
          "then upgrade to this version. Nothing has been changed.");
    }
  }
}
