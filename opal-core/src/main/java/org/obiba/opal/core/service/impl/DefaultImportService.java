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
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.audit.hibernate.HibernateVariableEntityAuditLogManager;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.ValueSetWrapper;
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

  private HibernateVariableEntityAuditLogManager auditLogManager;

  private IParticipantIdentifier participantIdentifier;

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

  public void setArchiveDirectory(String archiveDirectory) {
    this.archiveDirectory = archiveDirectory;
  }

  public void setAuditLogManager(HibernateVariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  private void copyValueTables(Datasource source, Datasource destination, String owner) throws IOException {
    for(ValueTable valueTable : source.getValueTables()) {
      if(valueTable.isForEntityType("Participant")) {
        copyParticipants(valueTable, source, destination, owner);
      } else {
        DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
        copier.copy(valueTable, destination);
      }
    }
  }

  private void copyParticipants(ValueTable participantTable, Datasource source, Datasource destination, String owner) throws IOException {
    copyParticipantIdentifiers(participantTable, source, owner);
    copyParticipantDataAndMetadata(participantTable, source, destination, owner);
  }

  // TODO: This method needs refactoring.
  private void copyParticipantIdentifiers(ValueTable participantTable, Datasource source, String owner) throws IOException {
    OpalPrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap();
    entityMap.setOwner(owner);
    entityMap.setParticipantIdentifier(participantIdentifier);

    // Create a SelectClause that selects all identifier variables.
    SelectClause selectClause = new SelectClause() {
      public boolean select(Variable variable) {
        return variable.hasAttribute("identifier") && (variable.getAttribute("identifier").getValue().equals(BooleanType.get().trueValue()) || variable.getAttribute("identifier").getValue().equals(TextType.get().valueOf("true")));
      }
    };

    // Create a view of the participant table that wraps entities with "public" entities, and that
    // selects only IDENTIFIER variables.
    PrivateVariableEntityValueTable privateTable = new PrivateVariableEntityValueTable(participantTable.getName(), participantTable, entityMap);
    privateTable.setSelectClause(selectClause);
    privateTable.initialise();

    Datasource destination = MagmaEngine.get().getDatasource("key-datasource");
    ValueTableWriter tableWriter = destination.createWriter("keys", privateTable.getEntityType());
    try {
      // First write the variables (owner + identifiers).
      VariableWriter variableWriter = tableWriter.writeVariables();
      Variable ownerVariable = Variable.Builder.newVariable(owner, TextType.get(), participantTable.getEntityType()).build();
      try {
        variableWriter.writeVariable(ownerVariable);
        for(Variable variable : privateTable.getVariables()) {
          variableWriter.writeVariable(variable);
        }
      } finally {
        variableWriter.close();
      }
      // Next write the ValueSets.
      for(ValueSet valueSet : privateTable.getValueSets()) {
        ValueSetWriter valueSetWriter = tableWriter.writeValueSet(valueSet.getVariableEntity());
        try {
          valueSetWriter.writeValue(ownerVariable, TextType.get().valueOf(((ValueSetWrapper) valueSet).getWrappedValueSet().getVariableEntity().getIdentifier()));
          for(Variable variable : privateTable.getVariables()) {
            Value value = privateTable.getValue(variable, valueSet);
            if(!value.isNull()) {
              valueSetWriter.writeValue(variable, value);
            }
          }
        } finally {
          valueSetWriter.close();
        }
      }
    } finally {
      tableWriter.close();
    }
  }

  private void copyParticipantDataAndMetadata(ValueTable participantTable, Datasource source, Datasource destination, String owner) throws IOException {
    OpalPrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap();
    entityMap.setOwner(owner);
    entityMap.setParticipantIdentifier(participantIdentifier);

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
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
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
    } catch(IOException ex) {
      log.error("Failed to archive file {}", file);
    }
  }

}
