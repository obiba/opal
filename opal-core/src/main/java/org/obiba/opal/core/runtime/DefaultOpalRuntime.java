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
import net.sf.ehcache.CacheManager;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
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
import org.obiba.opal.spi.vcf.VCFStoreService;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

  private Collection<Plugin> listPlugins() {
    Map<String, Plugin> pluginsMap = Maps.newLinkedHashMap();
    // make sure plugins directory exists
    initDirectory(PLUGINS_DIR);
    // read it to enhance classpath
    File pluginsDir = new File(PLUGINS_DIR);
    if (!pluginsDir.exists() || !pluginsDir.isDirectory() || !pluginsDir.canRead()) return pluginsMap.values();
    File[] children = pluginsDir.listFiles();
    if (children == null) return pluginsMap.values();
    for (File child : children) {
      Plugin plugin = new Plugin(child);
      addPlugin(pluginsMap, plugin);
    }
    return pluginsMap.values();
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
