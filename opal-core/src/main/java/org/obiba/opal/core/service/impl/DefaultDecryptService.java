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

import java.io.File;
import java.io.IOException;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.DecryptService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private OpalRuntime opalRuntime;

  // private DatasourceEncryptionStrategy dsEncryptionStrategy;

  private VariableEntityAuditLogManager auditLogManager;

  //
  // DecryptService Methods
  //

  public void decryptData(String unitName, String datasourceName, File file) throws NoSuchFunctionalUnitException, NoSuchDatasourceException, IllegalArgumentException, IOException {
    Assert.notNull(file, "file is null");
    Assert.isTrue(file.isFile(), "No such file (" + file.getPath() + ")");

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(datasourceName);

    FunctionalUnit unit = opalRuntime.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }

    FsDatasource sourceDatasource = new FsDatasource(file.getName(), file, getDatasourceEncryptionStrategy(unit));
    try {
      MagmaEngine.get().addDatasource(sourceDatasource);
      copyValueTables(sourceDatasource, destinationDatasource);
    } finally {
      MagmaEngine.get().removeDatasource(sourceDatasource);
    }
  }

  public void decryptData(String datasourceName, File file) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
    decryptData(FunctionalUnit.OPAL_INSTANCE, datasourceName, file);
  }

  //
  // Methods
  //

  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  public void setAuditLogManager(VariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  private DatasourceEncryptionStrategy getDatasourceEncryptionStrategy(FunctionalUnit unit) {
    DatasourceEncryptionStrategy dsEncryptionStrategy = unit.getDatasourceEncryptionStrategy();
    if(dsEncryptionStrategy == null) {
      // Use default strategy.
      dsEncryptionStrategy = new EncryptedSecretKeyDatasourceEncryptionStrategy();
    }
    dsEncryptionStrategy.setKeyProvider(unit.getKeyStore());

    return dsEncryptionStrategy;
  }

  private void copyValueTables(Datasource source, Datasource destination) throws IOException {
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
    copier.copy(source, destination);
  }

}
