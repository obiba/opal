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
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.spi.search.SearchServiceLoader;
import org.obiba.opal.spi.vcf.VCFStoreServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
class PluginsManager {

  private static final Logger log = LoggerFactory.getLogger(PluginsManager.class);

  private static final String PLUGINS_REPO_FILE = "plugins.json";

  @Value("${org.obiba.opal.plugin.repos}")
  private List<String> repos;

  private Set<PluginDescription> pluginDescriptions = Sets.newLinkedHashSet();

  private List<ServicePlugin> servicePlugins = Lists.newArrayList();

  boolean hasPlugins() {
    return getPlugins().size()>0;
  }

  boolean hasPlugin(String name) {
    return getPlugins().stream().anyMatch(p -> p.getName().equals(name));
  }

  Plugin getPlugin(String name) {
    Optional<Plugin> plugin = getPlugins().stream().filter(p -> p.getName().equals(name)).findFirst();
    if (!plugin.isPresent()) throw new NoSuchElementException("No such plugin with name: " + name);
    return plugin.get();
  }

  void initPlugins() {
    initPluginDescriptions();
    getPlugins().forEach(Plugin::init);
  }

  /**
   * Fetch the plugin descriptions from the configured repositories.
   *
   */
  void initPluginDescriptions() {
    if (repos != null) {
      pluginDescriptions.clear();
      for (String repo : repos) {
        String location = repo + (repo.endsWith("/") ? "" : "/") + PLUGINS_REPO_FILE;
        try (InputStream input = new URL(location).openStream()) {
          JSONObject pluginsObject = new JSONObject(new String(FileCopyUtils.copyToByteArray(input), StandardCharsets.UTF_8));
          if (pluginsObject.has("plugins")) {
            JSONArray pluginsArray = pluginsObject.getJSONArray("plugins");
            for (int i=0; i<pluginsArray.length(); i++) {
              JSONObject pluginObject = pluginsArray.getJSONObject(i);
              PluginDescription desc = new PluginDescription(pluginObject, repo);
              pluginDescriptions.add(desc);
            }
          }
        } catch (Exception e) {
          log.warn("Could not retrieve plugins list from {}: {}: {}", repo, e.getClass().getName(), e.getMessage());
        }
      }
    }
  }

  /**
   * List valid and most recent version plugins.
   *
   * @return
   */
  Collection<Plugin> getPlugins() {
    Map<String, Plugin> pluginsMap = Maps.newLinkedHashMap();
    // make sure plugins directory exists
    // read it to enhance classpath
    File pluginsDir = new File(OpalRuntime.PLUGINS_DIR);
    if (!pluginsDir.exists() || !pluginsDir.isDirectory() || !pluginsDir.canRead()) return pluginsMap.values();
    preparePlugins(pluginsDir);
    addPlugins(pluginsMap, pluginsDir);
    return pluginsMap.values();
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
    Map<String, Plugin> pluginsMap = getPlugins().stream().collect(Collectors.toMap(Plugin::getName, Function.identity()));
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

  /**
   * Register every instance of service plugin.
   *
   * @param pluginsMap
   * @param service
   */
  private void registerServicePlugin(Map<String, Plugin> pluginsMap, ServicePlugin service) {
    try {
      Plugin plugin = pluginsMap.get(service.getName());
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
  private void registerSingletonServicePlugin(Map<String, Plugin> pluginsMap, ServicePlugin service) {
    Plugin plugin = pluginsMap.get(service.getName());
    // check if service plugin of same type is already registered
    for (ServicePlugin servicePlugin : servicePlugins) {
      Plugin p = pluginsMap.get(servicePlugin.getName());
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
    File[] children = pluginsDir.listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith("-dist.zip"));
    if (children == null || children.length == 0) return;
    File archiveDir = new File(pluginsDir, ".archive");
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
    File expectedFolder = new File(destination, fileZip.getName().replace("-dist.zip", ""));
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
      File siteProperties = new File(pluginFolder, Plugin.SITE_PROPERTIES);
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
    File siteProperties = new File(pluginFolder, Plugin.SITE_PROPERTIES);
    if (siteProperties.exists()) FileUtil.delete(siteProperties);
    FileUtil.copyFile(sitePropertiesBackup, siteProperties);
    sitePropertiesBackup.delete();
  }

  /**
   * Discover valid and most recent version plugins.
   *
   * @param pluginsMap
   * @param pluginsDir
   */
  private void addPlugins(Map<String, Plugin> pluginsMap, File pluginsDir) {
    File[] children = pluginsDir.listFiles(pathname -> pathname.isDirectory() && !pathname.getName().startsWith("."));
    if (children == null || children.length == 0) return;
    for (File child : children) {
      Plugin plugin = new Plugin(child);
      addPlugin(pluginsMap, plugin);
    }
  }

  /**
   * Add plugin if valid and if most recent version.
   *
   * @param pluginsMap
   * @param plugin
   */
  private void addPlugin(Map<String, Plugin> pluginsMap, Plugin plugin) {
    if (!plugin.isValid()) return;
    if (!pluginsMap.containsKey(plugin.getName()))
      pluginsMap.put(plugin.getName(), plugin);
    else if (plugin.getVersion().compareTo(pluginsMap.get(plugin.getName()).getVersion())>0)
      pluginsMap.put(plugin.getName(), plugin);
  }

}
