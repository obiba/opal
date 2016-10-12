/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.service.OrientDbService;
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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Command to perform an upgrade (i.e., invoke the upgrade manager).
 */
public class UpgradeCommand {

  private static final Logger log = LoggerFactory.getLogger(UpgradeCommand.class);

  private static final String[] CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade.xml" };

  private static final String[] OPAL2_CONTEXT_PATHS = { "classpath:/META-INF/spring/opal-server/upgrade-2.0.0.xml" };

  private String opalConfigPath = Paths.get(System.getenv("OPAL_HOME"), "data", "orientdb", "opal-config").toString();

  public void execute() {
    if(needToUpgradeToOpal2()) {
      opal2Upgrade();
    }

    if(needUpgradeOrientDb()) {
      upgradeOrientDb();
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

  private boolean needUpgradeOrientDb() {
    log.info("Checking orientdb upgrade");
    ProcessResult result = executeOpalMigrator("--check", opalConfigPath);

    if(result.exitCode == 0) {
      log.info("Older version detected. Upgrade needed.");
      return true;
    } else if(result.exitCode == 65) {
      log.info("Upgrade not needed");
      return false;
    }

    throw new RuntimeException(result.output);
  }

  private void upgradeOrientDb() {
    log.info("Upgrading orientdb");
    File exportFile = null;
    File tmpFile = null;
    Path configBackup = Paths.get(String.format("%s.bak", opalConfigPath));
    Path config = Paths.get(opalConfigPath);

    try {
      tmpFile = File.createTempFile("opal_orientdb_export", null);
      String exportFilePrefix = tmpFile.getAbsolutePath();
      exportFile = Paths.get(exportFilePrefix + ".gz").toFile();

      exportOpalConfig(opalConfigPath, exportFilePrefix);
      Files.move(config, configBackup, StandardCopyOption.ATOMIC_MOVE);
    } catch(IOException e) {
      if(exportFile != null && exportFile.exists()) exportFile.delete();

      throw Throwables.propagate(e);
    } finally {
      if(tmpFile != null) tmpFile.delete();
    }

    try(ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(CONTEXT_PATHS)) {
      OrientDbService orientDbService = ctx.getBean("orientDbService", OrientDbService.class);
      orientDbService.importDatabase(exportFile);
      log.info("Upgraded orientdb successfully");

      try {
        FileUtils.deleteDirectory(configBackup.toFile());
      } catch(Exception e) {
        log.error("Error cleaning up orientdb upgrade back up directory. Ignoring.", e);
      }
    } catch(IOException e) {
      //roll back
      try {
        FileUtils.deleteDirectory(config.toFile());
        Files.move(configBackup, config, StandardCopyOption.ATOMIC_MOVE);
      } catch(Exception ex) {
        log.error("Error in orientdb upgrade rollback. Ignoring.", ex);
      }

      throw Throwables.propagate(e);
    } finally {
      if(exportFile != null && exportFile.exists()) exportFile.delete();
    }
  }

  private void exportOpalConfig(String opalConfigPath, String exportFilePrefix) {
    ProcessResult result = executeOpalMigrator(opalConfigPath, exportFilePrefix);
    if(result.exitCode != 0) throw new RuntimeException(result.output);
  }

  private ProcessResult executeOpalMigrator(String... args) {
    ProcessBuilder pb = getOpalMigratorProcessBuilder(args);

    try {
      Process p = pb.start();
      p.waitFor();
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      StringBuilder stringBuilder = new StringBuilder();

      while((line = br.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }

      return new ProcessResult(p.exitValue(), stringBuilder.toString());
    } catch(IOException | InterruptedException e) {
      throw Throwables.propagate(e);
    }
  }

  private ProcessBuilder getOpalMigratorProcessBuilder(String... args) {
    String dist = System.getenv("OPAL_DIST");
    if(Strings.isNullOrEmpty(dist))
      throw new RuntimeException("Cannot locate opal tools directory: OPAL_DIST is not defined.");

    File toolsDir = Paths.get(dist, "tools", "lib").toFile();
    if(!toolsDir.exists() || !toolsDir.isDirectory())
      throw new RuntimeException("No such directory: " + toolsDir.getAbsolutePath());

    File[] jars = toolsDir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith("opal-config-migrator-") && name.endsWith("-cli.jar");
      }
    });
    if(jars == null || jars.length == 0) throw new RuntimeException(
        String.format("Cannot find any opal-config-migrator-*-cli.jar file in '%s'", toolsDir.getAbsolutePath()));

    List<String> processArgs = Lists.newArrayList("java", "-jar", jars[0].getName());
    processArgs.addAll(Arrays.asList(args));

    log.info("Running Opal config migrator command: {}", Joiner.on(" ").join(processArgs));

    ProcessBuilder pb = new ProcessBuilder(processArgs);
    pb.redirectErrorStream(true);
    pb.directory(toolsDir);

    return pb;
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