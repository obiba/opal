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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.obiba.opal.core.upgrade.v2_0_x.ConfigFolderUpgrade;
import org.obiba.opal.core.upgrade.v2_0_x.database.Opal2DatabaseConfigurator;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand {

  private static final Logger log = LoggerFactory.getLogger(UpgradeCommand.class);

  private static final String[] CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade.xml" };

  private static final String[] OPAL2_CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade-2.0.0.xml" };

  public void execute() {
    if(!isMigratedToOpal2()) {
      opal2Upgrade();
    }
    standardUpgrade();
  }

  private void standardUpgrade() {
    try(ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(CONTEXT_PATHS)) {
      try {
        ctx.getBean("upgradeManager", UpgradeManager.class).executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the upgrade manager", upgradeFailed);
      }
    }
  }

  /**
   * Load opal-config.xml and search for version node
   */
  private boolean isMigratedToOpal2() {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(new File(System.getenv().get("OPAL_HOME") + "/conf/opal-config.xml"));
      XPath xPath = XPathFactory.newInstance().newXPath();
      Node node = (Node) xPath.compile("//version").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
      return node != null;
    } catch(SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void opal2Upgrade() {
    log.info("Prepare upgrade to Opal 2.0.0");

    // need to be run out of Spring context
    new Opal2DatabaseConfigurator().configureDatabase();
    ConfigFolderUpgrade.cleanDirectories();

    try(ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(OPAL2_CONTEXT_PATHS)) {
      try {
        ctx.getBean("upgradeManager", UpgradeManager.class).executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the opal-2.0.0 upgrade manager", upgradeFailed);
      }
    }
  }

}