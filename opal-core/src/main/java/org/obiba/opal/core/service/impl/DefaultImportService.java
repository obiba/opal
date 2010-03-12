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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /** Configured through org.obiba.opal.keys.tableReference */
  private String keysTableReference;

  /** Configured through org.obiba.opal.keys.entityType */
  private String keysTableEntityType;

  private HibernateVariableEntityAuditLogManager auditLogManager;

  private IParticipantIdentifier participantIdentifier;

  //
  // ImportService Methods
  //

  public void importData(String datasourceName, String owner, File file) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
    // OPAL-170 Dispatch the variables in tables corresponding to Onyx stage attribute value.
    importData(datasourceName, owner, file, "stage");
  }

  public void importData(String datasourceName, String owner, File file, String dispatchAttribute) throws NoSuchDatasourceException, IllegalArgumentException, IOException {
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
      copyValueTables(sourceDatasource, destinationDatasource, owner, dispatchAttribute);
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

  private void copyValueTables(Datasource source, Datasource destination, String owner, String dispatchAttribute) throws IOException {
    for(ValueTable valueTable : source.getValueTables()) {
      if(valueTable.isForEntityType(keysTableEntityType)) {
        copyParticipants(valueTable, source, destination, owner, dispatchAttribute);
      } else {
        DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
        copier.copy(valueTable, destination);
      }
    }
  }

  private void copyParticipants(ValueTable participantTable, Datasource source, Datasource destination, String owner, String dispatchAttribute) throws IOException {

    // Create a SelectClause that selects all identifier variables.
    SelectClause selectClause = new SelectClause() {
      public boolean select(Variable variable) {
        return isIdentifierVariable(variable);
      }
    };

    View privateView = View.Builder.newView(participantTable.getName(), participantTable).select(selectClause).build();
    Variable ownerVariable = prepareKeysTable(privateView, owner);

    OpalPrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(lookupKeysTable(), ownerVariable, participantIdentifier);

    List<ValueTable> participantTables = prepareParticipantTables(participantTable, source, destination, dispatchAttribute, entityMap);

    // prepare for copying participant data
    ValueTableWriter keysTableWriter = writeToKeysTable();

    DatasourceCopier dataCopier = DatasourceCopier.Builder.newCopier().dontCopyMetadata().withLoggingListener().withThroughtputListener().withVariableEntityCopyEventListener(auditLogManager, destination).build();
    Map<String, ValueTableWriter> valueTableWriters = new HashMap<String, ValueTableWriter>();
    for(ValueTable valueTable : participantTables) {
      valueTableWriters.put(valueTable.getName(), dataCopier.createValueTableWriter(valueTable, destination));
    }

    try {
      // for each value set, dispatch values in all participant tables
      for(ValueSet valueSet : participantTable.getValueSets()) {
        // copy participant identifiers and update entity map
        copyParticipantIdentifiers(privateView, ownerVariable, keysTableWriter, entityMap, valueSet);

        // copy participant data
        for(ValueTable valueTable : participantTables) {
          copyParticipantData(valueTable, source, destination, dataCopier, valueTableWriters.get(valueTable.getName()), entityMap, valueSet);
        }
      }
    } finally {
      keysTableWriter.close();
      for(ValueTable valueTable : participantTables) {
        dataCopier.closeValueTableWriter(valueTableWriters.get(valueTable.getName()), valueTable);
      }
    }
  }

  /**
   * Write the key variable.
   * @param privateView
   * @param owner
   * @return
   * @throws IOException
   */
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

  /**
   * Dispatch variables in value tables according to the value of the dispatch attribute and write the variables.
   * @param participantTable
   * @param source
   * @param destination
   * @param dispatchAttribute
   * @param entityMap
   * @return
   * @throws IOException
   */
  private List<ValueTable> prepareParticipantTables(ValueTable participantTable, Datasource source, Datasource destination, String dispatchAttribute, OpalPrivateVariableEntityMap entityMap) throws IOException {

    List<ValueTable> tables = new ArrayList<ValueTable>();
    List<String> dispatchTables = getDispatchAttributeValues(participantTable, dispatchAttribute);

    PrivateVariableEntityValueTable privateTable;

    // Create a view of the participant table that wraps entities with "public" entities, and that
    // selects only NON-IDENTIFIER variables.
    privateTable = new PrivateVariableEntityValueTable(participantTable.getName(), participantTable, entityMap);
    privateTable.setSelectClause(new CopySelectClause(dispatchAttribute));
    privateTable.initialise();

    // do not copy a table without variables (that may result from the select clause)
    if(privateTable.getVariables().iterator().hasNext()) {
      tables.add(privateTable);
    }

    for(String dispatchTable : dispatchTables) {
      privateTable = new PrivateVariableEntityValueTable(dispatchTable, participantTable, entityMap);
      privateTable.setSelectClause(new CopySelectClause(dispatchAttribute, dispatchTable));
      privateTable.initialise();
      tables.add(privateTable);
    }

    // Copy the view to the destination datasource.
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
    for(ValueTable table : tables) {
      copier.copy(table, destination);
    }

    return tables;
  }

  /**
   * Write the key variable and the identifier variables values; update the participant key private/public map.
   * @param privateView
   * @param ownerVariable
   * @param writer
   * @param entityMap
   * @param valueSet
   * @throws IOException
   */
  private void copyParticipantIdentifiers(ValueTable privateView, Variable ownerVariable, ValueTableWriter writer, OpalPrivateVariableEntityMap entityMap, ValueSet valueSet) throws IOException {
    VariableEntity privateEntity = valueSet.getVariableEntity();
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
      DatasourceCopier.Builder.newCopier().dontCopyMetadata().build().copy(privateView, valueSet, vsw);
    } finally {
      vsw.close();
    }
  }

  /**
   * Write the variables values.
   * @param privateView
   * @param source
   * @param destination
   * @param copier
   * @param writer
   * @param entityMap
   * @param valueSet
   * @throws IOException
   */
  private void copyParticipantData(ValueTable privateView, Datasource source, Datasource destination, DatasourceCopier copier, ValueTableWriter writer, OpalPrivateVariableEntityMap entityMap, ValueSet valueSet) throws IOException {
    VariableEntity privateEntity = valueSet.getVariableEntity();
    // entity exists
    VariableEntity publicEntity = entityMap.publicEntity(privateEntity);

    ValueSetWriter vsw = writer.writeValueSet(publicEntity);
    try {
      // Copy all other variable values
      copier.copy(privateView, valueSet, vsw);
    } finally {
      vsw.close();
    }
  }

  /**
   * Get the distinct values for given variable attribute.
   * @param participantTable
   * @param dispatchAttribute
   * @return
   */
  private List<String> getDispatchAttributeValues(ValueTable participantTable, String dispatchAttribute) {
    List<String> stages = new ArrayList<String>();

    if(dispatchAttribute != null) {
      for(Variable variable : participantTable.getVariables()) {
        if(variable.hasAttribute(dispatchAttribute) && !isIdentifierVariable(variable)) {
          String value = variable.getAttributeStringValue(dispatchAttribute);
          if(value != null) {
            value = value.trim();
            if(value.length() > 0 && !stages.contains(value)) {
              stages.add(value.trim());
            }
          }
        }
      }
    }

    return stages;
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

  private boolean isIdentifierVariable(Variable variable) {
    return variable.hasAttribute("identifier") && (variable.getAttribute("identifier").getValue().equals(BooleanType.get().trueValue()) || variable.getAttribute("identifier").getValue().equals(TextType.get().valueOf("true")));
  }

  /**
   * Create a SelectClause that selects all NON-IDENTIFIER variables and variables from a particular attribute.
   */
  private class CopySelectClause implements SelectClause {

    private String dispatchValue;

    private String dispatchAttribute;

    public CopySelectClause(String dispatchAttribute, String dispatchValue) {
      super();
      this.dispatchValue = dispatchValue;
      this.dispatchAttribute = dispatchAttribute;
    }

    public CopySelectClause(String dispatchAttribute) {
      this(dispatchAttribute, null);
    }

    public boolean select(Variable variable) {
      boolean selected = true;
      if(dispatchAttribute != null) {
        if(dispatchValue != null) {
          selected = variable.hasAttribute(dispatchAttribute) && variable.getAttributeStringValue(dispatchAttribute).equals(dispatchValue);
        } else {
          selected = !variable.hasAttribute(dispatchAttribute);
        }
      }

      return selected && !isIdentifierVariable(variable);
    }

  }

}
