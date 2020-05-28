/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime;

import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.CacheManager;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.tx.TransactionalThread;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.fs.security.SecuredOpalFileSystem;
import org.obiba.plugins.spi.ServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collection;
import java.util.Set;

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

  @Autowired
  private PluginsManager pluginsManager;

  private OpalFileSystem opalFileSystem;

  private final Object syncFs = new Object();

  @Value("${org.obiba.magma.readDataPointsCount}")
  private Integer readDataPointsCount;
  
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
    for (ServicePlugin service : pluginsManager.getServicePlugins()) {
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
  public boolean hasServicePlugins() {
    return pluginsManager.getServicePlugins().size() > 0;
  }

  @Override
  public Collection<ServicePlugin> getServicePlugins() {
    return pluginsManager.getServicePlugins();
  }

  @Override
  public boolean hasServicePlugins(Class clazz) {
    return pluginsManager.hasServicePlugins(clazz);
  }

  @Override
  public Collection<ServicePlugin> getServicePlugins(Class clazz) {
    return pluginsManager.getServicePlugins(clazz);
  }

  @Override
  public ServicePlugin getServicePlugin(Class clazz) {
    return getServicePlugins(clazz).iterator().next();
  }

  @Override
  public boolean hasServicePlugin(String name) {
    return pluginsManager.getServicePlugins().stream().filter(s -> name.equals(s.getName())).count() == 1;
  }

  @Override
  public ServicePlugin getServicePlugin(String name) {
    return pluginsManager.getServicePlugin(name);
  }

  //
  // Private methods
  //

  private void initPlugins() {
    // make sure plugins directory exists
    initDirectory(PLUGINS_DIR);
    // read it to enhance classpath
    pluginsManager.initPlugins();
  }

  private void initServicePlugins() {
    pluginsManager.initServicePlugins();
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
      Runnable magmaEngineInit = () -> {
        // This needs to be added BEFORE otherwise bad things happen. That really sucks.
        MagmaEngine.get().addDecorator(viewManager);
        MagmaEngineFactory magmaEngineFactory = opalConfigurationService.getOpalConfiguration()
            .getMagmaEngineFactory();

        for (MagmaEngineExtension extension : magmaEngineFactory.extensions()) {
          MagmaEngine.get().extend(extension);
        }
        MagmaEngine.get().extend(new MagmaCacheExtension(new EhCacheCacheManager(cacheManager)));
        MagmaParametersExtension paramsExt = new MagmaParametersExtension();
        paramsExt.setParameter(AbstractValueTable.READ_DATA_POINTS_COUNT_KEY, readDataPointsCount);
        MagmaEngine.get().extend(paramsExt);
      };
      new TransactionalThread(transactionTemplate, magmaEngineInit).start();
    } catch (RuntimeException e) {
      log.error("Could not create MagmaEngine.", e);
    }
  }

  private void initServices() {
    for (Service service : services) {
      try {
        service.initialize(this);
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

  private void ensureFolder(String path) throws FileSystemException {
    FileObject folder = getFileSystem().getRoot().resolveFile(path);
    folder.createFolder();
  }

}
