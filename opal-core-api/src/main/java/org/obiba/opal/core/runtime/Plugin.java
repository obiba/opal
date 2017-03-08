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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Plugin resources.
 */
public class Plugin {

  private static final Logger log = LoggerFactory.getLogger(Plugin.class);

  private final File directory;

  private final File properties;

  private final File lib;

  public Plugin(File directory) {
    this.directory = directory;
    this.properties = new File(directory, OpalRuntime.PLUGIN_PROPERTIES);
    this.lib = new File(directory, "lib");
  }

  public String getName() {
    try (FileInputStream in = new FileInputStream(properties)) {
      Properties prop = new Properties();
      prop.load(in);
      return prop.getProperty("name", directory.getName());
    } catch (Exception e) {
      log.warn("Failed reading properties: {}", properties.getAbsolutePath(), e);
      return directory.getName();
    }
  }

  public boolean isValid() {
    return directory.isDirectory() && directory.canRead()
        && properties.exists() && properties.canRead()
        && lib.exists() && lib.isDirectory() && lib.canRead();
  }

  public Properties getProperties() {
    Properties prop = getDefaultProperties();
    try (FileInputStream in = new FileInputStream(properties)) {
      prop.load(in);
      return prop;
    } catch (Exception e) {
      log.warn("Failed reading properties: {}", properties.getAbsolutePath(), e);
      return prop;
    }
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

}
