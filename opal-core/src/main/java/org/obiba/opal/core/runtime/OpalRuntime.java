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
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.OpalFileSystemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public class OpalRuntime implements IOpalRuntime {
  //
  // Instance Variables
  //

  @Autowired
  private PlatformTransactionManager txManager;

  private OpalConfiguration opalConfiguration;

  private OpalFileSystem opalFileSystem;

  //
  // InitializingBean Methods
  //

  public OpalRuntime(OpalConfiguration opalConfiguration) {
    this.opalConfiguration = opalConfiguration;
  }

  public void init() throws Exception {
    new TransactionTemplate(txManager).execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return opalConfiguration.getMagmaEngineFactory().create();
      }
    });

    // Initialize Opal file system
    opalFileSystem = new OpalFileSystemImpl(opalConfiguration.getFileSystemRoot());

    // Create the folders for each FunctionalUnit
    for(FunctionalUnit unit : opalConfiguration.getFunctionalUnits()) {
      getUnitDirectory(unit.getName());
    }
  }

  public void destroy() throws Exception {
    new TransactionTemplate(txManager).execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        MagmaEngine.get().shutdown();
        return null;
      }
    });
  }

  public OpalConfiguration getOpalConfiguration() {
    return opalConfiguration;
  }

  public OpalFileSystem getFileSystem() {
    return opalFileSystem;
  }

  public Set<FunctionalUnit> getFunctionalUnits() {
    return opalConfiguration.getFunctionalUnits();
  }

  public FunctionalUnit getFunctionalUnit(String unitName) {
    return opalConfiguration.getFunctionalUnit(unitName);
  }

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
}
