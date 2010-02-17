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

import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.audit.hibernate.HibernateVariableEntityAuditLogManager;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.PrivateVariableEntityValueTable;
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

  private DatasourceEncryptionStrategy dsEncryptionStrategy;

  private String archiveDirectory;

  private String keysTableReference = "key-datasource.keys";

  private String keysTableEntityType = "Participant";

  private HibernateVariableEntityAuditLogManager auditLogManager;

  private IParticipantIdentifier participantIdentifier;

  //
  // ImportService Methods
  //

  public void importData(String datasourceName, String owner, File file) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
    // Validate the file.
    if(!file.isFile()) {
      throw new IllegalArgumentException("No such file (" + file.getPath() + ")");
    }

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(datasourceName);

    // Create an FsDatasource for the specified file.
    FsDatasource sourceDatasource = new FsDatasource(file.getName(), file, dsEncryptionStrategy);

    // Copy the FsDatasource to the destination datasource.
    try {
      MagmaEngine.get().addDatasource(sourceDatasource);
      copyValueTables(sourceDatasource, destinationDatasource, owner);
    } finally {
      MagmaEngine.get().removeDatasource(sourceDatasource);
    }

    // Archive the file.
    archiveData(file);
  }

  //
  // Methods
  //

  public void setParticipantIdentifier(IParticipantIdentifier participantIdentifier) {
    this.participantIdentifier = participantIdentifier;
  }

  public void setDatasourceEncryptionStrategy(DatasourceEncryptionStrategy dsEncryptionStrategy) {
    this.dsEncryptionStrategy = dsEncryptionStrategy;
  }

  public void setKeysTableReference(String keysTableReference) {
    this.keysTableReference = keysTableReference;
  }

  public void setKeysTableEntityType(String keysTableEntityType) {
    this.keysTableEntityType = keysTableEntityType;
  }

  public void setArchiveDirectory(String archiveDirectory) {
    this.archiveDirectory = archiveDirectory;
  }

  public void setAuditLogManager(HibernateVariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  private void copyValueTables(Datasource source, Datasource destination, String owner) throws IOException {
    for(ValueTable valueTable : source.getValueTables()) {
      if(valueTable.isForEntityType(keysTableEntityType)) {
        copyParticipants(valueTable, source, destination, owner);
      } else {
        DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
        copier.copy(valueTable, destination);
      }
    }
  }

  private void copyParticipants(ValueTable participantTable, Datasource source, Datasource destination, String owner) throws IOException {

    // Create a SelectClause that selects all identifier variables.
    SelectClause selectClause = new SelectClause() {
      public boolean select(Variable variable) {
        return variable.hasAttribute("identifier") && (variable.getAttribute("identifier").getValue().equals(BooleanType.get().trueValue()) || variable.getAttribute("identifier").getValue().equals(TextType.get().valueOf("true")));
      }
    };

    View privateView = View.Builder.newView(participantTable.getName(), participantTable).select(selectClause).build();
    Variable ownerVariable = prepareKeysTable(privateView, owner);

    copyParticipantIdentifiers(privateView, source, ownerVariable);
    copyParticipantDataAndMetadata(participantTable, source, destination, ownerVariable);
  }

  private Variable prepareKeysTable(ValueTable privateView, String owner) throws IOException {

    Variable ownerVariable = Variable.Builder.newVariable(owner, TextType.get(), privateView.getEntityType()).build();

    ValueTableWriter writer = writeToKeysTable();
    try {
      VariableWriter vw = writer.writeVariables();
      try {
        // Create private variables
        vw.writeVariable(ownerVariable);
        DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(privateView, vw);
      } finally {
        vw.close();
      }
    } finally {
      writer.close();
    }
    return ownerVariable;
  }

  private void copyParticipantIdentifiers(ValueTable privateView, Datasource source, Variable ownerVariable) throws IOException {
    ValueTableWriter writer = writeToKeysTable();
    try {
      // Copy private entities to keys table.
      OpalPrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(lookupKeysTable(), ownerVariable, participantIdentifier);
      for(ValueSet privateValueSet : privateView.getValueSets()) {
        VariableEntity privateEntity = privateValueSet.getVariableEntity();
        VariableEntity publicEntity;
        if(entityMap.hasPrivateEntity(privateEntity) == false) {
          publicEntity = entityMap.createPublicEntity(privateEntity);
        } else {
          publicEntity = entityMap.publicEntity(privateEntity);
        }

        ValueSetWriter vsw = writer.writeValueSet(publicEntity);
        try {
          // Write the private identifier value to the "owner" variable in the keys valueSet
          vsw.writeValue(ownerVariable, TextType.get().valueOf(privateEntity.getIdentifier()));
          // Copy all other private variable values
          DatasourceCopier.Builder.newCopier().dontCopyMetadata().build().copy(privateView, privateValueSet, vsw);
        } finally {
          vsw.close();
        }
      }
    } finally {
      writer.close();
    }
  }

  private void copyParticipantDataAndMetadata(ValueTable participantTable, Datasource source, Datasource destination, Variable ownerVariable) throws IOException {
    OpalPrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(lookupKeysTable(), ownerVariable, participantIdentifier);

    // Create a SelectClause that selects all NON-IDENTIFIER variables.
    SelectClause selectClause = new SelectClause() {
      public boolean select(Variable variable) {
        return !variable.hasAttribute("identifier") || (variable.hasAttribute("identifier") && (variable.getAttribute("identifier").getValue().equals(BooleanType.get().falseValue()) || variable.getAttribute("identifier").getValue().equals(TextType.get().valueOf("false"))));
      }
    };

    // Create a view of the participant table that wraps entities with "public" entities, and that
    // selects only NON-IDENTIFIER variables.
    PrivateVariableEntityValueTable privateTable = new PrivateVariableEntityValueTable(participantTable.getName(), participantTable, entityMap);
    privateTable.setSelectClause(selectClause);
    privateTable.initialise();

    // Copy the view to the destination datasource.
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withThroughtputListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
    copier.copy(privateTable, destination);
  }

  private void archiveData(File file) {
    // Was an archive directory configured? If not, do nothing.
    if(archiveDirectory == null || archiveDirectory.isEmpty()) {
      log.info("No archive directory configured");
      return;
    }

    // Create the archive directory if necessary.
    File archiveDir = new File(archiveDirectory);
    archiveDir.mkdirs();

    // Move the file there.
    try {
      FileUtil.moveFile(file, archiveDir);
    } catch(IOException e) {
      log.error("Failed to archive file {} to dir {}. Error reported: {}", new Object[] { file, archiveDir, e.getMessage() });
    }
  }

  private ValueTable lookupKeysTable() {
    return MagmaEngineTableResolver.valueOf(keysTableReference).resolveTable();
  }

  private ValueTableWriter writeToKeysTable() {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(keysTableReference);
    return MagmaEngine.get().getDatasource(resolver.getDatasourceName()).createWriter(resolver.getTableName(), keysTableEntityType);
  }

}
