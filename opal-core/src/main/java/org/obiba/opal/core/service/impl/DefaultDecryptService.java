/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.crypt.support.NullKeyProvider;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.DecryptService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link DecryptService}.
 */
@Transactional
public class DefaultDecryptService implements DecryptService {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultDecryptService.class);

  //
  // Instance Variables
  //

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private UnitKeyStoreService unitKeyStoreService;

  @Autowired
  private VariableEntityAuditLogManager auditLogManager;

  //
  // DecryptService Methods
  //

  public void decryptData(String unitName, String datasourceName, FileObject file) throws NoSuchFunctionalUnitException, NoSuchDatasourceException, IllegalArgumentException, IOException {
    Assert.notNull(file, "file is null");
    Assert.isTrue(file.getType() == FileType.FILE, "No such file (" + file.getName().getPath() + ")");

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(datasourceName);

    FunctionalUnit unit = opalRuntime.getFunctionalUnit(unitName);
    if(!FunctionalUnit.OPAL_INSTANCE.equals(unitName) && unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
    // Create an FsDatasource for the specified file.
    FsDatasource sourceDatasource = new FsDatasource(file.getName().getBaseName(), opalRuntime.getFileSystem().getLocalFile(file), unit != null ? getDatasourceEncryptionStrategy(unit) : getOpalInstanceEncryptionStrategy());
    try {
      MagmaEngine.get().addDatasource(sourceDatasource);
      copyValueTables(sourceDatasource, destinationDatasource);
    } finally {
      MagmaEngine.get().removeDatasource(sourceDatasource);
    }
  }

  public void decryptData(String datasourceName, FileObject file) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
    decryptData(FunctionalUnit.OPAL_INSTANCE, datasourceName, file);
  }

  //
  // Methods
  //

  private DatasourceEncryptionStrategy getDatasourceEncryptionStrategy(FunctionalUnit unit) {
    DatasourceEncryptionStrategy dsEncryptionStrategy = unit.getDatasourceEncryptionStrategy();
    if(dsEncryptionStrategy == null) {
      dsEncryptionStrategy = getDefaultEncryptionStrategy();
    }
    UnitKeyStore unitKeyStore = unit.getKeyStore(false);
    dsEncryptionStrategy.setKeyProvider(unitKeyStore != null ? unitKeyStore : new NullKeyProvider());

    return dsEncryptionStrategy;
  }

  private DatasourceEncryptionStrategy getOpalInstanceEncryptionStrategy() {
    DatasourceEncryptionStrategy dsEncryptionStrategy = getDefaultEncryptionStrategy();
    dsEncryptionStrategy.setKeyProvider(unitKeyStoreService.getOrCreateUnitKeyStore(FunctionalUnit.OPAL_INSTANCE));

    return dsEncryptionStrategy;
  }

  private DatasourceEncryptionStrategy getDefaultEncryptionStrategy() {
    return new EncryptedSecretKeyDatasourceEncryptionStrategy();
  }

  private void copyValueTables(Datasource source, Datasource destination) throws IOException {
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
    copier.copy(source, destination);
  }

}
