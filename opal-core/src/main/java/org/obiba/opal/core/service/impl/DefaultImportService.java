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
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.DatasourceCopier;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.opal.core.service.ImportService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ImportService}.
 */
@Transactional
public class DefaultImportService implements ImportService {
  //
  // Instance Variables
  //

  private DatasourceEncryptionStrategy dsEncryptionStrategy;

  //
  // ImportService Methods
  //

  public void importData(String datasourceName, File file, boolean encrypted) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
    // Validate the file.
    if(!file.isFile()) {
      throw new IllegalArgumentException("No such file (" + file.getPath() + ")");
    }

    // Validate the datasource name.
    Datasource dsDestination = MagmaEngine.get().getDatasource(datasourceName);
    if(dsDestination == null) {
      throw new NoSuchDatasourceException("No such datasource (" + datasourceName + ")");
    }

    // Create an FsDatasource for the specified file.
    FsDatasource dsSource = null;
    if(encrypted) {
      dsSource = new FsDatasource(file.getName(), file, dsEncryptionStrategy);
    } else {
      dsSource = new FsDatasource(file.getName(), file);
    }
    MagmaEngine.get().addDatasource(dsSource);

    // Copy the FsDatasource to the specified destination datasource.
    DatasourceCopier copier = new DatasourceCopier();
    try {
      copier.copy(file.getName(), datasourceName);
    } finally {
      // Always disconnect the FsDatasource after copying it (even if an exception occurs).
      MagmaEngine.get().removeDatasource(dsSource);
    }
  }

  //
  // Methods
  //

  public void setDatasourceEncryptionStrategy(DatasourceEncryptionStrategy dsEncryptionStrategy) {
    this.dsEncryptionStrategy = dsEncryptionStrategy;
  }
}
