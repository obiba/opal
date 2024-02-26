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

import com.google.common.base.Strings;
import org.obiba.core.util.FileUtil;
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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand {

  private static final Logger log = LoggerFactory.getLogger(UpgradeCommand.class);

  private static final String[] CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade.xml" };

  private String opalConfigPath = Paths.get(System.getenv("OPAL_HOME"), "data", "orientdb", "opal-config").toString();

  public void execute() {
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
  private boolean hasVersionInConfigXml() {
    try {
      File opalConfig = new File(System.getenv().get("OPAL_HOME"), "data" + File.separatorChar + "opal-config.xml");
      if(!opalConfig.exists()) return false;

      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(opalConfig);
      XPath xPath = XPathFactory.newInstance().newXPath();
      Node node = (Node) xPath.compile("//version").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
      return node != null && !Strings.isNullOrEmpty(node.getNodeValue());
    } catch(SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load opal-config.properties and search for datasource definition
   */
  private boolean hasDatasourceInConfigProperties() {
    try {
      Properties properties = PropertiesLoaderUtils.loadProperties(
          new FileSystemResource(new File(System.getenv().get("OPAL_HOME"), "conf" + File.separatorChar +
              "opal-config.properties")));
      return properties.containsKey("org.obiba.opal.datasource.opal.driver");
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void prepareConfigFiles() {
    prepareOpalConfigFile();
    prepareDistConfigFile("logback.xml");
  }

  private void prepareDistConfigFile(String name) {
    File confFile = new File(System.getenv().get("OPAL_HOME"), "conf" + File.separatorChar +
        name);
    if(confFile.exists()) return;

    // try to find conf file in opal distribution
    if(!System.getenv().containsKey("OPAL_DIST")) return;
    File distFile = new File(System.getenv().get("OPAL_DIST"), "conf" + File.separatorChar +
        name);
    if(!distFile.exists()) return;
    try {
      FileUtil.copyFile(distFile, confFile);
    } catch(IOException e) {
      throw new RuntimeException("Unable to prepare " + name + " configuration file before upgrade", e);
    }
  }

  private void prepareOpalConfigFile() {
    File originalConfig = new File(System.getenv().get("OPAL_HOME"), "conf" + File.separatorChar +
        "opal-config.xml");
    if(!originalConfig.exists()) return;

    try {
      File originalConfigCopy = new File(originalConfig.getAbsolutePath() + ".opal1-backup");
      if(!originalConfigCopy.exists()) FileUtil.copyFile(originalConfig, originalConfigCopy);
      File dataDir = new File(System.getenv().get("OPAL_HOME"), "data");
      if(!dataDir.exists()) dataDir.mkdirs();
      FileUtil.moveFile(originalConfig, dataDir);
    } catch(IOException e) {
      throw new RuntimeException("Unable to change location of opal-config.xml before upgrade", e);
    }
  }

  private class ProcessResult {
    int exitCode;
    String output;

    ProcessResult(int exitCode, String output) {
      this.exitCode = exitCode;
      this.output = output;
    }
  }
}