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
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.upgrade.v2_0_x.ConfigFolderUpgrade;
import org.obiba.opal.core.upgrade.v2_0_x.database.Opal2PropertiesConfigurator;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand {

  private static final Logger log = LoggerFactory.getLogger(UpgradeCommand.class);

  private static final String[] CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade.xml" };

  private static final String[] OPAL2_CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade-2.0.0.xml" };

  public void execute() {
    if(needToUpgradeToOpal2()) {
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

  private boolean needToUpgradeToOpal2() {
    return !hasVersionInConfigXml() && hasDatasourceInConfigProperties();
  }

  /**
   * Load opal-config.xml and search for version node
   */
  private boolean hasVersionInConfigXml() {
    try {
      File opalConfig = new File(getOpalHome(), "data" + File.separatorChar + "opal-config.xml");
      if(!opalConfig.exists()) return false;

      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(opalConfig);
      XPath xPath = XPathFactory.newInstance().newXPath();
      Node node = (Node) xPath.compile("//version").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
      return node != null && !Strings.isNullOrEmpty(node.getNodeValue());
    } catch(SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
  }

    private String getOpalHome() {
        return getSysProp("OPAL_HOME");
    }

    private String getSysProp(String name) {
        return System.getProperty(name); //this is what is being used everywhere else in Opal code, and is mutable
        //return System.getenv().get(name);
    }

  /**
   * Load opal-config.properties and search for datasource definition
   */
  private boolean hasDatasourceInConfigProperties() {
    try {
      Properties properties = PropertiesLoaderUtils.loadProperties(
          new FileSystemResource(new File(getOpalHome(), "conf" + File.separatorChar +
              "opal-config.properties")));
      return properties.containsKey("org.obiba.opal.datasource.opal.driver");
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void opal2Upgrade() {
    log.info("Prepare upgrade to Opal 2.0.0");

    prepareConfigFiles();

    // need to be run out of Spring context
    new Opal2PropertiesConfigurator().upgrade();
    ConfigFolderUpgrade.cleanDirectories();

    try(ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(OPAL2_CONTEXT_PATHS)) {
      try {
        ctx.getBean("upgradeManager", UpgradeManager.class).executeUpgrade();
      } catch(UpgradeException upgradeFailed) {
        throw new RuntimeException("An error occurred while running the opal-2.0.0 upgrade manager", upgradeFailed);
      }
    }
  }

  private void prepareConfigFiles() {
    prepareOpalConfigFile();
    prepareDistConfigFile("logback.xml");
    prepareDistConfigFile("newrelic.yml");
  }

  private void prepareDistConfigFile(String name) {
    File confFile = new File(getOpalHome(), "conf" + File.separatorChar +
        name);
    if(confFile.exists()) return;

    // try to find conf file in opal distribution
    String opalDist = getSysProp("OPAL_DIST");

    if (opalDist == null) {
        return;
    }

    File distFile = new File(opalDist, "conf" + File.separatorChar +
        name);
    if(!distFile.exists()) return;
    try {
      FileUtil.copyFile(distFile, confFile);
    } catch(IOException e) {
      throw new RuntimeException("Unable to prepare " + name + " configuration file before upgrade", e);
    }
  }

  private void prepareOpalConfigFile() {
    File originalConfig = new File(getOpalHome(), "conf" + File.separatorChar +
        "opal-config.xml");
    if(!originalConfig.exists()) return;

    try {
      File originalConfigCopy = new File(originalConfig.getAbsolutePath() + ".opal1-backup");
      if(!originalConfigCopy.exists()) FileUtil.copyFile(originalConfig, originalConfigCopy);
      File dataDir = new File(getOpalHome(), "data");
      if(!dataDir.exists()) dataDir.mkdirs();
      FileUtil.moveFile(originalConfig, dataDir);
    } catch(IOException e) {
      throw new RuntimeException("Unable to change location of opal-config.xml before upgrade", e);
    }
  }

}