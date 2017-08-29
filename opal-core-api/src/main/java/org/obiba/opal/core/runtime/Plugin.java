/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime;

import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Plugin resources.
 */
public class Plugin {

  private static final Logger log = LoggerFactory.getLogger(Plugin.class);

  public static final String UNINSTALL_FILE = "uninstall";

  public static final String PLUGIN_PROPERTIES = "plugin.properties";

  public static final String SITE_PROPERTIES = "site.properties";

  private final File directory;

  private final File properties;

  private final File siteProperties;

  private final File lib;

  private final File uninstallFile;

  public Plugin(File directory) {
    this.directory = directory;
    this.properties = new File(directory, PLUGIN_PROPERTIES);
    this.siteProperties = new File(directory, SITE_PROPERTIES);
    this.uninstallFile = new File(directory, UNINSTALL_FILE);
    this.lib = new File(directory, "lib");
  }

  public String getName() {
    try (FileInputStream in = new FileInputStream(properties)) {
      Properties prop = new Properties();
      prop.load(in);
      return prop.getProperty("name", directory.getName());
    } catch (Exception e) {
      log.warn("Failed reading plugin name property: {}", properties.getAbsolutePath(), e);
      return directory.getName();
    }
  }

  public String getType() {
    return getProperties().getProperty("type", "");
  }

  public boolean isValid() {
    return directory.isDirectory() && directory.canRead()
        && properties.exists() && properties.canRead()
        && lib.exists() && lib.isDirectory() && lib.canRead()
        && !uninstallFile.exists();
  }

  public Version getVersion() {
    String version = getProperties().getProperty("version", "0.0.0");
    return new Version(version);
  }

  public String getTitle() {
    return getProperties().getProperty("title", "");
  }

  public String getDescription() {
    return getProperties().getProperty("description", "");
  }

  public Version getOpalVersion() {
    String version = getProperties().getProperty("opal.version", "0.0.0");
    return new Version(version);
  }

  public Properties getProperties() {
    Properties prop = getDefaultProperties();
    try (FileInputStream in = new FileInputStream(properties)) {
      prop.load(in);
    } catch (Exception e) {
      log.warn("Failed reading properties: {}", properties.getAbsolutePath(), e);
    }
    if (siteProperties.exists()) {
      try (FileInputStream in = new FileInputStream(siteProperties)) {
        prop.load(in);
      } catch (Exception e) {
        log.warn("Failed reading site properties: {}", siteProperties.getAbsolutePath(), e);
      }
    }
    return prop;
  }

  public boolean isToUninstall() {
    return uninstallFile.exists();
  }

  public File getDirectory() {
    return directory;
  }

  private Properties getDefaultProperties() {
    String name = getName();
    String home = System.getProperty("OPAL_HOME");
    Properties defaultProperties = new Properties();
    defaultProperties.put("OPAL_HOME", home);
    File dataDir = new File(home, "data" + File.separator + name);
    dataDir.mkdirs();
    defaultProperties.put("data.dir", dataDir.getAbsolutePath());
    File workDir = new File(home, "work" + File.separator + name);
    workDir.mkdirs();
    defaultProperties.put("work.dir", workDir.getAbsolutePath());
    defaultProperties.put("install.dir", directory.getAbsolutePath());
    return defaultProperties;
  }

  public void init() {
    File[] libs = lib.listFiles();
    if (libs == null) return;
    for (File lib : libs) {
      try {
        addLibrary(lib);
      } catch (Exception e) {
        log.warn("Failed adding library file to classpath: {}", lib, e);
      }
    }
  }

  private void addLibrary(File file) throws Exception {
    log.info("Adding library file to classpath: {}", file);
    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
  }

  public void cancelUninstall() {
    if (uninstallFile.exists()) uninstallFile.delete();
  }

  public void prepareForUninstall() {
    try {
      if (!uninstallFile.exists()) uninstallFile.createNewFile();
    } catch (IOException e) {
      log.error("Failed to prepare plugin {} for removal", getName(), e);
    }
  }

  public void writeSiteProperties(Properties properties) throws IOException {
    properties.store(new OutputStreamWriter(new FileOutputStream(siteProperties), "UTF-8"), null);
  }
}
