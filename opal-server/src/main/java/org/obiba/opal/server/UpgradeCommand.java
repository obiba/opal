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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.obiba.opal.core.upgrade.ConfigFolderUpgrade;
import org.obiba.opal.core.upgrade.database.Opal2DatabaseConfigurator;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand {

  private static final Logger log = LoggerFactory.getLogger(UpgradeCommand.class);

  private static final String[] CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade.xml" };

  private static final String[] OPAL2_CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade-2.0.xml" };

  public void execute() {
    if(!isMigratedToOpal2()) {
      opal2Upgrade();
    }
    standardUpgrade();
  }

  private void standardUpgrade() {
    ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(CONTEXT_PATHS);
    try {
      try {
        ctx.getBean("upgradeManager", UpgradeManager.class).executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the upgrade manager", upgradeFailed);
      }
    } finally {
      ctx.close();
    }
  }

  /**
   * Load opal-config.xml and search for migratedToOpal2 node
   */
  private boolean isMigratedToOpal2() {
    String opalHome = System.getenv().get("OPAL_HOME");
    try {
      SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
      ConfigurationHandler handler = new ConfigurationHandler();
      saxParser.parse(new File(opalHome + "/conf/opal-config.xml"), handler);
      return handler.isMigrated();
    } catch(SAXException e) {
      throw new RuntimeException("An error occurred while reading opal-config.xml during upgrade", e);
    } catch(IOException e) {
      throw new RuntimeException("An error occurred while reading opal-config.xml during upgrade", e);
    } catch(ParserConfigurationException e) {
      throw new RuntimeException("An error occurred while reading opal-config.xml during upgrade", e);
    }
  }

  private void opal2Upgrade() {
    log.info("Prepare upgrade to Opal 2.0");

    // need to be run out of Spring context
    new Opal2DatabaseConfigurator().configureDatabase();
    ConfigFolderUpgrade.cleanDirectories();

    ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(OPAL2_CONTEXT_PATHS);
    try {
      try {
        ctx.getBean("upgradeManager", UpgradeManager.class).executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the opal2 upgrade manager", upgradeFailed);
      }
    } finally {
      ctx.close();
    }
  }

  private static class ConfigurationHandler extends DefaultHandler {

    private boolean migrated;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if("migratedToOpal2".equals(qName)) {
        migrated = true;
      }
    }

    private boolean isMigrated() {
      return migrated;
    }
  }
}