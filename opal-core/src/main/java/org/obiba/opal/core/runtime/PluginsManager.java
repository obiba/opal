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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.type.DateType;
import org.obiba.plugins.spi.ServicePlugin;
import org.obiba.opal.spi.search.SearchServiceLoader;
import org.obiba.opal.spi.vcf.VCFStoreServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class PluginsManager {

  private static final Logger log = LoggerFactory.getLogger(PluginsManager.class);

  private static final String PLUGINS_REPO_FILE = "plugins.json";

  private static final String PLUGIN_DIST_SUFFIX = "-dist.zip";

  private final File pluginsDir = new File(OpalRuntime.PLUGINS_DIR);

  private final File archiveDir = new File(OpalRuntime.PLUGINS_DIR, ".archive");

  @Value("${org.obiba.opal.plugins.site}")
  private String repo;

  private Collection<OpalPlugin> registeredPlugins;

  private List<ServicePlugin> servicePlugins = Lists.newArrayList();

  public boolean restartRequired() {
    File[] children = pluginsDir.listFiles(pathname -> !pathname.getName().startsWith("."));
    if (children == null || children.length == 0) return false;
    for (File child : children) {
      if (child.isFile() && child.getName().endsWith(PLUGIN_DIST_SUFFIX)) return true;
      if (child.isDirectory() && new File(child, OpalPlugin.UNINSTALL_FILE).exists()) return true;
    }
    return false;
  }

  public OpalPlugin getPlugin(String name) {
    Optional<OpalPlugin> plugin = getPlugins().stream().filter(p -> p.getName().equals(name)).findFirst();
    if (!plugin.isPresent()) throw new NoSuchElementException("No such plugin with name: " + name);
    return plugin.get();
  }

  public void setPluginSiteProperties(String name, String properties) throws IOException {
    Optional<OpalPlugin> plugin = getPlugins().stream().filter(p -> p.getName().equals(name)).findFirst();
    if (!plugin.isPresent()) throw new NoSuchElementException("No such plugin with name: " + name);
    OpalPlugin thePlugin = plugin.get();
    thePlugin.writeSiteProperties(properties);
    updateServiceProperties(name, thePlugin.getProperties());
  }

  private void updateServiceProperties(String name, Properties properties) {
    ServicePlugin servicePlugin = getServicePlugin(name);
    if (servicePlugin != null) {
      servicePlugin.configure(properties);
    }
  }

  void initPlugins() {
    getPlugins(true).forEach(OpalPlugin::init);
  }

  /**
   * List valid and most recent version plugins.
   *
   * @return
   */
  public Collection<OpalPlugin> getPlugins() {
    if (registeredPlugins != null) return registeredPlugins;
    return getPlugins(false);
  }

  public Collection<String> getUninstalledPluginNames() {
    List<String> names = Lists.newArrayList();
    File[] children = pluginsDir.listFiles(pathname -> pathname.isDirectory() && !pathname.getName().startsWith("."));
    if (children == null || children.length == 0) return names;
    for (File child : children) {
      OpalPlugin plugin = new OpalPlugin(child);
      if (plugin.isToUninstall()) names.add(plugin.getName());
    }
    return names;
  }

  boolean hasServicePlugins(Class clazz) {
    return servicePlugins.stream().filter(s -> clazz.isAssignableFrom(s.getClass())).count() > 0;
  }

  Collection<ServicePlugin> getServicePlugins(Class clazz) {
    return servicePlugins.stream().filter(s -> clazz.isAssignableFrom(s.getClass())).collect(Collectors.toList());
  }

  List<ServicePlugin> getServicePlugins() {
    return servicePlugins;
  }

  ServicePlugin getServicePlugin(String name) {
    Optional<ServicePlugin> service = servicePlugins.stream().filter(s -> name.equals(s.getName())).findFirst();
    if (!service.isPresent()) throw new NoSuchServiceException(name);
    return service.get();
  }

  void initServicePlugins() {
    Map<String, OpalPlugin> pluginsMap = getPlugins().stream().collect(Collectors.toMap(OpalPlugin::getName, Function.identity()));
    VCFStoreServiceLoader.get().getServices().stream()
        .filter(service -> pluginsMap.containsKey(service.getName()))
        .forEach(service -> registerServicePlugin(pluginsMap, service));
    SearchServiceLoader.get().getServices().stream()
        .filter(service -> pluginsMap.containsKey(service.getName()))
        .forEach(service -> registerSingletonServicePlugin(pluginsMap, service));
  }

  //
  // Private methods
  //

  private synchronized Collection<OpalPlugin> getPlugins(boolean extract) {
    Map<String, OpalPlugin> pluginsMap = Maps.newLinkedHashMap();
    // make sure plugins directory exists
    // read it to enhance classpath
    if (!pluginsDir.exists() || !pluginsDir.isDirectory() || !pluginsDir.canRead()) return pluginsMap.values();
    if (extract) preparePlugins(pluginsDir);
    processPlugins(pluginsMap, pluginsDir);
    registeredPlugins = pluginsMap.values();
    return registeredPlugins;
  }

  /**
   * Register every instance of service plugin.
   *
   * @param pluginsMap
   * @param service
   */
  private void registerServicePlugin(Map<String, OpalPlugin> pluginsMap, ServicePlugin service) {
    try {
      OpalPlugin plugin = pluginsMap.get(service.getName());
      service.configure(plugin.getProperties());
      service.start();
      servicePlugins.add(service);
    } catch (Exception e) {
      log.warn("Error initializing/starting plugin service: {}", service.getClass(), e);
    }
  }

  /**
   * Register only the first service plugin of a given type.
   *
   * @param pluginsMap
   * @param service
   */
  private void registerSingletonServicePlugin(Map<String, OpalPlugin> pluginsMap, ServicePlugin service) {
    OpalPlugin plugin = pluginsMap.get(service.getName());
    // check if service plugin of same type is already registered
    for (ServicePlugin servicePlugin : servicePlugins) {
      OpalPlugin p = pluginsMap.get(servicePlugin.getName());
      if (p.getType().equals(plugin.getType())) return;
    }
    registerServicePlugin(pluginsMap, service);
  }

  /**
   * Uncompress and archive any zip file that could be found.
   *
   * @param pluginsDir
   */
  private void preparePlugins(File pluginsDir) {
    File[] children = pluginsDir.listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(PLUGIN_DIST_SUFFIX));
    if (children == null || children.length == 0) return;
    if (!archiveDir.exists()) archiveDir.mkdirs();
    for (File child : children) {
      try {
        extractPlugin(child);
        Files.move(child, new File(archiveDir, child.getName()));
      } catch (IOException e) {
        log.warn("Failed extracting plugin file: {}" + child.getAbsolutePath(), e);
      }
    }
  }

  /**
   * Extract plugin folder from zip file.
   *
   * @param fileZip
   * @throws IOException
   */
  private void extractPlugin(File fileZip) throws IOException {
    File destination = new File(fileZip.getParent());
    File expectedFolder = new File(destination, fileZip.getName().replace(PLUGIN_DIST_SUFFIX, ""));
    // backup any site properties
    File sitePropertiesBackup = backupPluginSiteProperties(expectedFolder);
    // Open the zip file
    ZipFile zipFile = new ZipFile(fileZip);
    Enumeration<?> enu = zipFile.entries();
    while (enu.hasMoreElements()) {
      ZipEntry zipEntry = (ZipEntry) enu.nextElement();
      String name = zipEntry.getName();
      log.info("Plugin extract: {}", name);
      // Do we need to create a directory ?
      File file = new File(destination, name);
      if (name.endsWith("/")) {
        file.mkdirs();
        continue;
      }
      // Extract the file
      InputStream is = zipFile.getInputStream(zipEntry);
      FileOutputStream fos = new FileOutputStream(file);
      byte[] bytes = new byte[1024];
      int length;
      while ((length = is.read(bytes)) >= 0) {
        fos.write(bytes, 0, length);
      }
      is.close();
      fos.close();
    }
    zipFile.close();
    // restore site properties
    restorePluginSiteProperties(expectedFolder, sitePropertiesBackup);
  }

  /**
   * Backup site properties file if found and clear old plugin folder.
   *
   * @param pluginFolder
   * @return
   * @throws IOException
   */
  private File backupPluginSiteProperties(File pluginFolder) throws IOException {
    File sitePropertiesBackup = null;
    if (pluginFolder.exists()) {
      File siteProperties = new File(pluginFolder, OpalPlugin.SITE_PROPERTIES);
      sitePropertiesBackup = File.createTempFile("site", ".properties");
      if (siteProperties.exists()) {
        FileUtil.copyFile(siteProperties, sitePropertiesBackup);
      }
      FileUtil.delete(pluginFolder);
    }
    return sitePropertiesBackup;
  }

  /**
   * Restore any site properties file that would have been backed up.
   *
   * @param pluginFolder
   * @param sitePropertiesBackup
   * @throws IOException
   */
  private void restorePluginSiteProperties(File pluginFolder, File sitePropertiesBackup) throws IOException {
    if (sitePropertiesBackup == null || !sitePropertiesBackup.exists()) return;
    File siteProperties = new File(pluginFolder, OpalPlugin.SITE_PROPERTIES);
    if (siteProperties.exists()) FileUtil.delete(siteProperties);
    FileUtil.copyFile(sitePropertiesBackup, siteProperties);
    sitePropertiesBackup.delete();
  }

  /**
   * Discover valid and most recent version plugins and archive plugins prepared for uninstallation.
   *
   * @param pluginsMap
   * @param pluginsDir
   */
  private void processPlugins(Map<String, OpalPlugin> pluginsMap, File pluginsDir) {
    File[] children = pluginsDir.listFiles(pathname -> pathname.isDirectory() && !pathname.getName().startsWith("."));
    if (children == null || children.length == 0) return;
    for (File child : children) {
      OpalPlugin plugin = new OpalPlugin(child);
      processPlugin(pluginsMap, plugin);
    }
  }

  /**
   * Add plugin if valid and if most recent version or archive it if marked for uninstallation.
   *
   * @param pluginsMap
   * @param plugin
   */
  private void processPlugin(Map<String, OpalPlugin> pluginsMap, OpalPlugin plugin) {
    if (plugin.isToUninstall()) {
      File archiveDest = new File(archiveDir, plugin.getDirectory().getName() + "-" + DateType.get().valueOf(new Date()).toString());
      log.info("Archiving plugin {} to {}", plugin.getName(), archiveDest.getAbsolutePath());
      try {
        if (archiveDest.exists()) FileUtil.delete(archiveDest);
        archiveDir.mkdirs();
        FileUtil.moveFile(plugin.getDirectory(), archiveDest);
      } catch (IOException e) {
        log.info("Failed to archive plugin directory: {}", plugin.getDirectory().getName(), e);
      }
      return;
    }
    if (!plugin.isValid()) return;
    if (!pluginsMap.containsKey(plugin.getName()))
      pluginsMap.put(plugin.getName(), plugin);
    else if (plugin.getVersion().compareTo(pluginsMap.get(plugin.getName()).getVersion()) > 0)
      pluginsMap.put(plugin.getName(), plugin);
  }
}
