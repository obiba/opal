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

import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.security.OpalSecurityManager;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.OpalFileSystemImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableSet;

/**
 *
 */
public class DefaultOpalRuntime implements OpalRuntime {

  private static final Logger log = LoggerFactory.getLogger(OpalRuntime.class);

  private final OpalConfiguration opalConfiguration;

  @Autowired
  private PlatformTransactionManager txManager;

  @Autowired
  private Set<Service> services;

  @Autowired
  private OpalSecurityManager opalSecurityManager;

  private OpalFileSystem opalFileSystem;

  public DefaultOpalRuntime(OpalConfiguration opalConfiguration) {
    this.opalConfiguration = opalConfiguration;
  }

  @Override
  public void start() {
    opalSecurityManager.start();

    try {
      opalConfiguration.getMagmaEngineFactory().create();

      Runnable magmaEngineInit = new Runnable() {
        public void run() {
          opalConfiguration.getMagmaEngineFactory().initialize(MagmaEngine.get());
        }
      };
      new TransactionalThread(txManager, magmaEngineInit).start();
    } catch(RuntimeException e) {
      log.error("Could not create MagmaEngine.", e);
    }

    for(Service service : services) {
      try {
        service.start();
      } catch(RuntimeException e) {
        log.warn("Error starting service " + service.getClass(), e);
      }
    }

    // Initialize Opal file system
    try {
      opalFileSystem = new OpalFileSystemImpl(opalConfiguration.getFileSystemRoot());
      // Create the folders for each FunctionalUnit
      for(FunctionalUnit unit : opalConfiguration.getFunctionalUnits()) {
        getUnitDirectory(unit.getName());
      }
    } catch(RuntimeException e) {
      log.error("The opal filesystem cannot be started.", e);
    } catch(FileSystemException e) {
      log.error("Error creating functional unit's directory in the Opal File System.", e);
    }

  }

  @Override
  public void stop() {
    for(Service service : services) {
      try {
        if(service.isRunning()) service.stop();
      } catch(RuntimeException e) {
        log.warn("Error stoping service " + service.getClass(), e);
      }
    }

    new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        MagmaEngine.get().shutdown();
      }
    });

    opalSecurityManager.stop();
  }

  @Override
  public Set<Service> getServices() {
    return ImmutableSet.copyOf(services);
  }

  @Override
  public OpalConfiguration getOpalConfiguration() {
    return opalConfiguration;
  }

  @Override
  public OpalFileSystem getFileSystem() {
    return opalFileSystem;
  }

  @Override
  public Set<FunctionalUnit> getFunctionalUnits() {
    return opalConfiguration.getFunctionalUnits();
  }

  @Override
  public FunctionalUnit getFunctionalUnit(String unitName) {
    return opalConfiguration.getFunctionalUnit(unitName);
  }

  @Override
  public FileObject getUnitDirectory(String unitName) throws NoSuchFunctionalUnitException, FileSystemException {
    if(getFunctionalUnit(unitName) == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }

    FileObject unitsDir = getFileSystem().getRoot().resolveFile("units");
    unitsDir.createFolder();

    FileObject unitDir = unitsDir.resolveFile(unitName);
    unitDir.createFolder();

    return unitDir;
  }

  //
  // Inner Classes
  //

  static class TransactionalThread extends Thread {
    private PlatformTransactionManager txManager;

    private Runnable runnable;

    public TransactionalThread(PlatformTransactionManager txManager, Runnable runnable) {
      this.txManager = txManager;
      this.runnable = runnable;
    }

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
