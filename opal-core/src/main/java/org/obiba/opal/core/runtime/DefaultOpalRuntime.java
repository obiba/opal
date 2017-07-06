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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import net.sf.ehcache.CacheManager;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaCacheExtension;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.tx.TransactionalThread;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.fs.security.SecuredOpalFileSystem;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.spi.search.SearchServiceLoader;
import org.obiba.opal.spi.vcf.VCFStoreServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
@Component
public class DefaultOpalRuntime implements OpalRuntime {

  private static final Logger log = LoggerFactory.getLogger(OpalRuntime.class);

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private Set<Service> services;

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private CacheManager cacheManager;

  private OpalFileSystem opalFileSystem;

  private final Object syncFs = new Object();

  private List<ServicePlugin> servicePlugins = Lists.newArrayList();

  @Override
  @PostConstruct
  public void start() {
    initExtensions();
    initPlugins();
    initFileSystem();
    initMagmaEngine();
    initServicePlugins();
    initServices();
  }

  @Override
  @PreDestroy
  public void stop() {
    for (ServicePlugin service : servicePlugins) {
      try {
        if (service.isRunning()) service.stop();
      } catch (RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error stopping service plugin " + service.getClass(), e);
      }
    }

    for (Service service : services) {
      try {
        if (service.isRunning()) service.stop();
      } catch (RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error stopping service " + service.getClass(), e);
      }
    }

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        // Remove all datasources before writing the configuration.
        // This is done so that Disposable instances are disposed of before being written to the config file
        for (Datasource ds : MagmaEngine.get().getDatasources()) {
          try {
            MagmaEngine.get().removeDatasource(ds);
          } catch (RuntimeException e) {
            log.warn("Ignoring exception during shutdown sequence.", e);
          }
        }
      }
    });
  }

  @Override
  public boolean hasService(String name) {
    for (Service service : services) {
      if (service.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  @Override
  public Service getService(String name) throws NoSuchServiceException {
    for (Service service : services) {
      if (service.getName().equals(name)) {
        return service;
      }
    }
    throw new NoSuchServiceException(name);
  }

  @Override
  public Set<Service> getServices() {
    return ImmutableSet.copyOf(services);
  }

  @Override
  public boolean hasFileSystem() {
    return true;
  }

  @Override
  public OpalFileSystem getFileSystem() {
    synchronized (syncFs) {
      while (opalFileSystem == null) {
        try {
          syncFs.wait();
        } catch (InterruptedException ignored) {
        }
      }
    }
    return opalFileSystem;
  }

  @Override
  public boolean hasPlugins() {
    return listPlugins().size()>0;
  }

  @Override
  public Collection<Plugin> getPlugins() {
    return listPlugins();
  }

  @Override
  public boolean hasPlugin(String name) {
    return listPlugins().stream().anyMatch(p -> p.getName().equals(name));
  }

  @Override
  public Plugin getPlugin(String name) {
    Optional<Plugin> plugin = listPlugins().stream().filter(p -> p.getName().equals(name)).findFirst();
    if (!plugin.isPresent()) throw new NoSuchElementException("No such plugin with name: " + name);
    return plugin.get();
  }

  @Override
  public boolean hasServicePlugins() {
    return servicePlugins.size()>0;
  }

  @Override
  public Collection<ServicePlugin> getServicePlugins() {
    return servicePlugins;
  }

  @Override
  public boolean hasServicePlugins(Class clazz) {
    return servicePlugins.stream().filter(s -> clazz.isAssignableFrom(s.getClass())).count() > 0;
  }

  @Override
  public Collection<ServicePlugin> getServicePlugins(Class clazz) {
    return servicePlugins.stream().filter(s -> clazz.isAssignableFrom(s.getClass())).collect(Collectors.toList());
  }

  @Override
  public ServicePlugin getServicePlugin(Class clazz) {
    return getServicePlugins(clazz).iterator().next();
  }

  @Override
  public boolean hasServicePlugin(String name) {
    return servicePlugins.stream().filter(s -> name.equals(s.getName())).count() == 1;
  }

  @Override
  public ServicePlugin getServicePlugin(String name) {
    Optional<ServicePlugin> service = servicePlugins.stream().filter(s -> name.equals(s.getName())).findFirst();
    if (!service.isPresent()) throw new NoSuchServiceException(name);
    return service.get();
  }

  //
  // Private methods
  //

  private void initPlugins() {
    // read it to enhance classpath
    listPlugins().forEach(Plugin::init);
  }

  private void initExtensions() {
    // Make sure some extensions folder exists
    initDirectory(MAGMA_JS_EXTENSION);
    initDirectory(WEBAPP_EXTENSION);
  }

  private void initDirectory(String directory) {
    File dir = new File(directory);
    if (!dir.exists() && !dir.mkdirs()) {
      log.warn("Cannot create directory: {}", directory);
    }
  }

  private void initMagmaEngine() {
    try {
      Runnable magmaEngineInit = new Runnable() {
        @Override
        public void run() {
          // This needs to be added BEFORE otherwise bad things happen. That really sucks.
          MagmaEngine.get().addDecorator(viewManager);
          MagmaEngineFactory magmaEngineFactory = opalConfigurationService.getOpalConfiguration()
              .getMagmaEngineFactory();

          for (MagmaEngineExtension extension : magmaEngineFactory.extensions()) {
            MagmaEngine.get().extend(extension);
          }
          MagmaEngine.get().extend(new MagmaCacheExtension(new EhCacheCacheManager(cacheManager)));
        }
      };
      new TransactionalThread(transactionTemplate, magmaEngineInit).start();
    } catch (RuntimeException e) {
      log.error("Could not create MagmaEngine.", e);
    }
  }

  private void initServices() {
    for (Service service : services) {
      try {
        service.start();
      } catch (RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error starting service " + service.getClass(), e);
      }
    }
  }

  private void initFileSystem() {
    synchronized (syncFs) {
      try {
        opalFileSystem = new SecuredOpalFileSystem(
            new DefaultOpalFileSystem(opalConfigurationService.getOpalConfiguration().getFileSystemRoot()));

        // Create some system folders, if they do not exist.
        ensureFolder("home");
        ensureFolder("projects");
        ensureFolder("reports");
        ensureFolder("tmp");
      } catch (RuntimeException e) {
        log.error("The opal filesystem cannot be started.");
        throw e;
      } catch (FileSystemException e) {
        log.error("Error creating a system directory in the Opal File System.", e);
      }
      syncFs.notifyAll();
    }
  }

  private void initServicePlugins() {
    Map<String, Plugin> pluginsMap = listPlugins().stream().collect(Collectors.toMap(Plugin::getName, Function.identity()));
    VCFStoreServiceLoader.get().getServices().stream()
        .filter(service -> pluginsMap.containsKey(service.getName()))
        .forEach(service -> registerServicePlugin(pluginsMap, service));
    SearchServiceLoader.get().getServices().stream()
        .filter(service -> pluginsMap.containsKey(service.getName()))
        .forEach(service -> registerServicePlugin(pluginsMap, service));
  }

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

  private void ensureFolder(String path) throws FileSystemException {
    FileObject folder = getFileSystem().getRoot().resolveFile(path);
    folder.createFolder();
  }

  /**
   * List valid and most recent version plugins.
   *
   * @return
   */
  private Collection<Plugin> listPlugins() {
    Map<String, Plugin> pluginsMap = Maps.newLinkedHashMap();
    // make sure plugins directory exists
    initDirectory(PLUGINS_DIR);
    // read it to enhance classpath
    File pluginsDir = new File(PLUGINS_DIR);
    if (!pluginsDir.exists() || !pluginsDir.isDirectory() || !pluginsDir.canRead()) return pluginsMap.values();
    preparePlugins(pluginsDir);
    addPlugins(pluginsMap, pluginsDir);
    return pluginsMap.values();
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
      File siteProperties = new File(pluginFolder, SITE_PROPERTIES);
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
    File siteProperties = new File(pluginFolder, SITE_PROPERTIES);
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
