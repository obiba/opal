/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.js.GlobalMethodProvider;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.magma.js.OpalGlobalMethodProvider;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.fs.security.SecuredOpalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableSet;

/**
 *
 */
@Component
public class DefaultOpalRuntime implements OpalRuntime {

  private static final Logger log = LoggerFactory.getLogger(OpalRuntime.class);

  @Autowired
  private PlatformTransactionManager txManager;

  @Autowired
  private Set<Service> services;

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  @Autowired
  private ViewManager viewManager;

  private OpalFileSystem opalFileSystem;

  private final Object syncFs = new Object();

  @Override
  public void start() {
    initExtensions();
    initOpalConfiguration();
    initMagmaEngine();
    initServices();
    initFileSystem();
  }

  @Override
  public void stop() {

    for(Service service : services) {
      try {
        if(service.isRunning()) service.stop();
      } catch(RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error stopping service " + service.getClass(), e);
      }
    }

    new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        // Remove all datasources before writing the configuration.
        // This is done so that Disposable instances are disposed of before being written to the config file
        for(Datasource ds : MagmaEngine.get().getDatasources()) {
          try {
            MagmaEngine.get().removeDatasource(ds);
          } catch(RuntimeException e) {
            log.warn("Ignoring exception during shutdown sequence.", e);
          }
        }

        // opalConfigIo.writeConfiguration(opalConfiguration);
        MagmaEngine.get().shutdown();
      }
    });

  }

  @Override
  public boolean hasService(String name) {
    for(Service service : services) {
      if(service.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Nonnull
  @Override
  public Service getService(String name) throws NoSuchServiceException {
    for(Service service : services) {
      if(service.getName().equals(name)) {
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
  public OpalFileSystem getFileSystem() {
    synchronized(syncFs) {
      while(opalFileSystem == null) {
        try {
          syncFs.wait();
        } catch(InterruptedException ignored) {
        }
      }
    }
    return opalFileSystem;
  }

  private void initExtensions() {
    // Make sure some extensions folder exists
    initExtension(MAGMA_JS_EXTENSION);
    initExtension(WEBAPP_EXTENSION);
  }

  private void initExtension(String directory) {
    File ext = new File(directory);
    if(!ext.exists() && !ext.mkdirs()) {
      log.warn("Cannot create directory: {}", directory);
    }
  }

  private void initOpalConfiguration() {
    // Add opal specific javascript methods
    MagmaJsExtension jsExtension = new MagmaJsExtension();
    MagmaContextFactory ctxFactory = new MagmaContextFactory();
    Set<GlobalMethodProvider> providers = new HashSet<GlobalMethodProvider>();
    providers.add(new OpalGlobalMethodProvider());
    ctxFactory.setGlobalMethodProviders(providers);
    jsExtension.setMagmaContextFactory(ctxFactory);

    // We need these two extensions to read the opal config file
    new MagmaEngine().extend(new MagmaXStreamExtension()).extend(jsExtension);

    opalConfigurationService.readOpalConfiguration();
  }

  private void initMagmaEngine() {
    try {
      Runnable magmaEngineInit = new Runnable() {
        @Override
        public void run() {
          // This needs to be added BEFORE otherwise bad things happen. That really sucks.
          MagmaEngine.get().addDecorator(viewManager);
          initialise(MagmaEngine.get(), opalConfigurationService.getOpalConfiguration().getMagmaEngineFactory());
        }
      };
      new TransactionalThread(txManager, magmaEngineInit).start();
    } catch(RuntimeException e) {
      log.error("Could not create MagmaEngine.", e);
    }
  }

  private void initServices() {
    for(Service service : services) {
      try {
        service.start();
      } catch(RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error starting service " + service.getClass(), e);
      }
    }
  }

  private void initFileSystem() {
    synchronized(syncFs) {
      try {
        opalFileSystem = new SecuredOpalFileSystem(
            new DefaultOpalFileSystem(opalConfigurationService.getOpalConfiguration().getFileSystemRoot()));

        // Create tmp folder, if it does not exist.
        FileObject tmpFolder = getFileSystem().getRoot().resolveFile("tmp");
        tmpFolder.createFolder();
      } catch(RuntimeException e) {
        log.error("The opal filesystem cannot be started.");
        throw e;
      } catch(FileSystemException e) {
        log.error("Error creating functional unit's directory in the Opal File System.", e);
      }
      syncFs.notifyAll();
    }
  }

  private void initialise(MagmaEngine engine, MagmaEngineFactory magmaEngineFactory) {
    for(MagmaEngineExtension extension : magmaEngineFactory.extensions()) {
      engine.extend(extension);
    }

    for(DatasourceFactory factory : magmaEngineFactory.factories()) {
      try {
        engine.addDatasource(factory);
      } catch(RuntimeException e) {
        log.warn("Cannot initialise datasource '{}' : {}", factory.getName(), e.getMessage());
        log.debug("Datasource exception:", e);
      }
    }
  }

  //
  // Inner Classes
  //

  static class TransactionalThread extends Thread {
    private final PlatformTransactionManager txManager;

    private final Runnable runnable;

    TransactionalThread(PlatformTransactionManager txManager, Runnable runnable) {
      this.txManager = txManager;
      this.runnable = runnable;
    }

    @Override
    public void run() {
      new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          runnable.run();
        }
      });
    }
  }

}
