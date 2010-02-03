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
import org.obiba.magma.audit.hibernate.HibernateVariableEntityAuditLogManager;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.core.service.DecryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

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

  private DatasourceEncryptionStrategy dsEncryptionStrategy;

  private HibernateVariableEntityAuditLogManager auditLogManager;

  //
  // ImportService Methods
  //

  public void decryptData(String datasourceName, File file, boolean encrypted) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
    // Validate the file.
    if(!file.isFile()) {
      throw new IllegalArgumentException("No such file (" + file.getPath() + ")");
    }

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(datasourceName);
    if(destinationDatasource == null) {
      throw new NoSuchDatasourceException("No such datasource (" + datasourceName + ")");
    }

    // Create an FsDatasource for the specified file.
    FsDatasource sourceDatasource = null;
    if(encrypted) {
      System.out.println("yes it's encrypted");
      sourceDatasource = new FsDatasource(file.getName(), file, dsEncryptionStrategy);
    } else {
      sourceDatasource = new FsDatasource(file.getName(), file);
    }

    // Copy the FsDatasource to the destination datasource.
    try {
      MagmaEngine.get().addDatasource(sourceDatasource);
      copyValueTables(sourceDatasource, destinationDatasource);
    } finally {
      MagmaEngine.get().removeDatasource(sourceDatasource);
    }
  }

  //
  // Methods
  //

  public void setDatasourceEncryptionStrategy(DatasourceEncryptionStrategy dsEncryptionStrategy) {
    this.dsEncryptionStrategy = dsEncryptionStrategy;
  }

  public void setAuditLogManager(HibernateVariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  private void copyValueTables(Datasource source, Datasource destination) throws IOException {
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withVariableEntityCopyEventListener(auditLogManager, source, destination).build();
    copier.copy(source, destination);
  }

}
