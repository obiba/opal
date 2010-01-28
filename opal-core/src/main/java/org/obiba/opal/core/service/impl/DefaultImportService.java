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
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.views.SelectClause;
import org.obiba.opal.core.magma.PrivateVariableEntityValueTable;
import org.obiba.opal.core.service.IOpalKeyRegistry;
import org.obiba.opal.core.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ImportService}.
 */
@Transactional
public class DefaultImportService implements ImportService {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DefaultImportService.class);

  //
  // Instance Variables
  //

  private IOpalKeyRegistry opalKeyRegistry;

  private DatasourceEncryptionStrategy dsEncryptionStrategy;

  //
  // ImportService Methods
  //

  public void importData(String datasourceName, String owner, File file, boolean encrypted) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
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
      sourceDatasource = new FsDatasource(file.getName(), file, dsEncryptionStrategy);
    } else {
      sourceDatasource = new FsDatasource(file.getName(), file);
    }

    // Copy the FsDatasource to the destination datasource.
    try {
      MagmaEngine.get().addDatasource(sourceDatasource);
      copyValueTables(sourceDatasource, destinationDatasource, owner);
    } finally {
      MagmaEngine.get().removeDatasource(sourceDatasource);
    }
  }

  //
  // Methods
  //

  public void setOpalKeyRegistry(IOpalKeyRegistry opalKeyRegistry) {
    this.opalKeyRegistry = opalKeyRegistry;
  }

  public void setDatasourceEncryptionStrategy(DatasourceEncryptionStrategy dsEncryptionStrategy) {
    this.dsEncryptionStrategy = dsEncryptionStrategy;
  }

  private void copyValueTables(Datasource source, Datasource destination, String owner) throws IOException {
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().build();

    for(ValueTable valueTable : source.getValueTables()) {
      if(valueTable.isForEntityType("Participant")) {
        copyParticipants(valueTable, destination, copier, owner);
      } else {
        copier.copy(valueTable, destination);
      }
    }
  }

  private void copyParticipants(ValueTable participantTable, Datasource destination, DatasourceCopier copier, String owner) throws IOException {
    // Create an entity map, that maps private entities to public entities, and vice-versa.
    OpalPrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap();
    entityMap.setOpalKeyRegistry(opalKeyRegistry);
    entityMap.setOwner(owner);

    // Create a SelectClause that selects all identifier variables.
    SelectClause privateSelectClause = null; // new JavascriptClause("$var().attribute('identifier')");

    // Now create a "view" of the participant table, that exposes only the public data.
    PrivateVariableEntityValueTable privateTable = new PrivateVariableEntityValueTable(participantTable.getName(), participantTable, entityMap, privateSelectClause);

    // Copy the view.
    copier.copy(privateTable, destination);
  }
}
