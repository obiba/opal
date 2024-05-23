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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.tx.TransactionalThread;
import org.obiba.plugins.spi.ServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 *
 */
@Component
public class DefaultOpalRuntime implements OpalRuntime {

  private static final Logger log = LoggerFactory.getLogger(DefaultOpalRuntime.class);

  private final TransactionTemplate transactionTemplate;

  private final OpalConfigurationService opalConfigurationService;

  private final PluginsManager pluginsManager;

  private final Set<Service> services;

  private final ViewManager viewManager;

  private final ProjectService projectService;

  private Integer readDataPointsCount;

  @Autowired
  public DefaultOpalRuntime(TransactionTemplate transactionTemplate, OpalConfigurationService opalConfigurationService,
                            PluginsManager pluginsManager, Set<Service> services,
                            ViewManager viewManager, ProjectService projectService,
                            @Value("${org.obiba.magma.readDataPointsCount}") Integer readDataPointsCount) {
    this.transactionTemplate = transactionTemplate;
    this.opalConfigurationService = opalConfigurationService;
    this.pluginsManager = pluginsManager;
    this.services = services;
    this.viewManager = viewManager;
    this.projectService = projectService;
    this.readDataPointsCount = readDataPointsCount;
  }

  @Override
  public void start() {
    initExtensions();
    initPlugins();
    initServices();
    initMagmaEngine();
  }

  @Override
  public void stop() {
    pluginsManager.stopPlugins();

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

  @Override
  public Collection<App> getApps() {
    return Lists.newArrayList();
  }

  @Override
  public App getApp(String name) {
    return null;
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
        //MagmaEngine.get().extend(new MagmaCacheExtension(new EhCacheCacheManager(cacheManager)));
        MagmaParametersExtension paramsExt = new MagmaParametersExtension();
        paramsExt.setParameter(AbstractValueTable.READ_DATA_POINTS_COUNT_KEY, readDataPointsCount);
        MagmaEngine.get().extend(paramsExt);
        projectService.initialize();
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
      } catch (Exception | Error e) {
        log.warn("Error starting service: {}", service.getClass(), e);
      }
    }
  }

}
