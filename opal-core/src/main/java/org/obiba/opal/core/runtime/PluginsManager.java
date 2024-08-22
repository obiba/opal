/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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
import org.obiba.opal.spi.datasource.DatasourceServiceLoader;
import org.obiba.opal.spi.r.analysis.RAnalysisServiceLoader;
import org.obiba.opal.spi.vcf.VCFStoreServiceLoader;
import org.obiba.plugins.PluginResources;
import org.obiba.plugins.PluginsClassLoader;
import org.obiba.plugins.PluginsManagerHelper;
import org.obiba.plugins.spi.ServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PluginsManager {

  private static final Logger log = LoggerFactory.getLogger(PluginsManager.class);

  private static final String PLUGIN_DIST_SUFFIX = "-dist.zip";

  private static final String[] DEPRECATED_PLUGINS = new String[]{"opal-search-es"};

  private final File pluginsDir = new File(OpalRuntime.PLUGINS_DIR);

  private final File archiveDir = new File(OpalRuntime.PLUGINS_DIR, ".archive");

  @Value("${productionMode}")
  private boolean productionMode;

  private Collection<PluginResources> registeredPlugins;

  private List<ServicePlugin> servicePlugins = Lists.newArrayList();

  private PluginsClassLoader pluginsClassLoader;

  public boolean restartRequired() {
    File[] children = pluginsDir.listFiles(pathname -> !pathname.getName().startsWith("."));
    if (children == null || children.length == 0) return false;
    for (File child : children) {
      if (child.isFile() && child.getName().endsWith(PLUGIN_DIST_SUFFIX)) return true;
      if (child.isDirectory() && new File(child, OpalPlugin.UNINSTALL_FILE).exists()) return true;
    }
    return false;
  }

  public PluginResources getPlugin(String name) {
    Optional<PluginResources> plugin = getPlugins().stream().filter(p -> p.getName().equals(name)).findFirst();
    if (!plugin.isPresent()) throw new NoSuchElementException("No such plugin with name: " + name);
    return plugin.get();
  }

  public void setPluginSiteProperties(String name, String properties) throws IOException {
    Optional<PluginResources> plugin = getPlugins().stream().filter(p -> p.getName().equals(name)).findFirst();
    if (!plugin.isPresent()) throw new NoSuchElementException("No such plugin with name: " + name);
    PluginResources thePlugin = plugin.get();
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
    pluginsClassLoader = new PluginsClassLoader();
    getPlugins(true).forEach(p -> p.init(pluginsClassLoader, productionMode));
    initServicePlugins();
  }

  void stopPlugins() {
    for (ServicePlugin service : getServicePlugins()) {
      try {
        if (service.isRunning()) service.stop();
      } catch (RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error stopping service plugin " + service.getClass(), e);
      }
    }
    try {
      pluginsClassLoader.close();
    } catch (IOException e) {
      log.warn("Error closing plugins class loader", e);
    }
  }

  /**
   * List valid and most recent version plugins.
   *
   * @return
   */
  public Collection<PluginResources> getPlugins() {
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

  //
  // Private methods
  //

  private void initServicePlugins() {
    Map<String, PluginResources> pluginsMap = getPlugins().stream().collect(Collectors.toMap(PluginResources::getName, Function.identity()));
    VCFStoreServiceLoader.get(pluginsClassLoader).getServices().stream()
        .filter(service -> pluginsMap.containsKey(service.getName()))
        .forEach(service -> PluginsManagerHelper.registerServicePlugin(servicePlugins, pluginsMap, service));
    DatasourceServiceLoader.get(pluginsClassLoader).getServices().stream()
        .filter(service -> pluginsMap.containsKey(service.getName()))
        .forEach(service -> PluginsManagerHelper.registerServicePlugin(servicePlugins, pluginsMap, service));
    RAnalysisServiceLoader.get(pluginsClassLoader).getServices().stream()
        .filter(service -> pluginsMap.containsKey(service.getName()))
        .forEach(service -> PluginsManagerHelper.registerServicePlugin(servicePlugins, pluginsMap, service));
  }

  private synchronized Collection<PluginResources> getPlugins(boolean extract) {
    Map<String, PluginResources> pluginsMap = Maps.newLinkedHashMap();
    // make sure plugins directory exists
    // read it to enhance classpath
    if (!pluginsDir.exists() || !pluginsDir.isDirectory() || !pluginsDir.canRead()) return pluginsMap.values();
    if (extract) PluginsManagerHelper.preparePlugins(pluginsDir, archiveDir);
    processPlugins(pluginsMap, pluginsDir);
    registeredPlugins = pluginsMap.values();
    return registeredPlugins;
  }

  /**
   * Discover valid and most recent version plugins and archive plugins prepared for uninstallation.
   *
   * @param pluginsMap
   * @param pluginsDir
   */
  private void processPlugins(Map<String, PluginResources> pluginsMap, File pluginsDir) {
    File[] children = pluginsDir.listFiles(pathname -> pathname.isDirectory() && !pathname.getName().startsWith("."));
    if (children == null || children.length == 0) return;
    for (File child : children) {
      boolean deprecated = Arrays.stream(DEPRECATED_PLUGINS).anyMatch((name) -> child.getName().startsWith(name));
      if (!deprecated) { // deprecated plugin
        PluginResources plugin = new OpalPlugin(child);
        PluginsManagerHelper.processPlugin(pluginsMap, plugin, archiveDir);
      }
    }
  }

}
