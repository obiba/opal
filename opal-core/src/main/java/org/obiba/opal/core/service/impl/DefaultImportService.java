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
import java.util.TreeSet;
import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.audit.support.CopyAuditor;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.DatasourceCopier.DatasourceCopyValueSetEventListener;
import org.obiba.magma.support.DatasourceCopier.MultiplexingStrategy;
import org.obiba.magma.support.DatasourceCopier.VariableTransformer;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.PrivateVariableEntityValueTable;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link ImportService}.
 */
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = { RuntimeException.class, InterruptedException.class })
public class DefaultImportService implements ImportService {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultImportService.class);

  //
  // Instance Variables
  //

  private final OpalRuntime opalRuntime;

  private final VariableEntityAuditLogManager auditLogManager;

  private final IParticipantIdentifier participantIdentifier;

  /** Configured through org.obiba.opal.keys.tableReference */
  private final String keysTableReference;

  /** Configured through org.obiba.opal.keys.entityType */
  private final String keysTableEntityType;

  @Autowired
  public DefaultImportService(OpalRuntime opalRuntime, VariableEntityAuditLogManager auditLogManager, IParticipantIdentifier participantIdentifier, @Value("${org.obiba.opal.keys.tableReference}") String keysTableReference, @Value("${org.obiba.opal.keys.entityType}") String keysTableEntityType) {
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(auditLogManager == null) throw new IllegalArgumentException("auditLogManager cannot be null");
    if(participantIdentifier == null) throw new IllegalArgumentException("participantIdentifier cannot be null");
    if(keysTableReference == null) throw new IllegalArgumentException("keysTableReference cannot be null");
    if(keysTableEntityType == null) throw new IllegalArgumentException("keysTableEntityType cannot be null");

    this.opalRuntime = opalRuntime;
    this.auditLogManager = auditLogManager;
    this.participantIdentifier = participantIdentifier;
    this.keysTableReference = keysTableReference;
    this.keysTableEntityType = keysTableEntityType;
  }

  //
  // ImportService Methods
  //

  public void importData(String unitName, String datasourceName, FileObject file) throws NoSuchFunctionalUnitException, NoSuchDatasourceException, IllegalArgumentException, IOException, InterruptedException {
    // OPAL-170 Dispatch the variables in tables corresponding to Onyx stage attribute value.
    importData(unitName, datasourceName, file, "stage");
  }

  private void importData(String unitName, String datasourceName, FileObject file, String dispatchAttribute) throws NoSuchFunctionalUnitException, NoSuchDatasourceException, IllegalArgumentException, IOException, InterruptedException {
    Assert.hasText(unitName, "unitName is null or empty");
    Assert.isTrue(!unitName.equals(FunctionalUnit.OPAL_INSTANCE), "unitName cannot be " + FunctionalUnit.OPAL_INSTANCE);
    Assert.hasText(datasourceName, "datasourceName is null or empty");
    Assert.notNull(file, "file is null");
    Assert.isTrue(file.getType() == FileType.FILE, "No such file (" + file.getName().getPath() + ")");

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(datasourceName);

    FunctionalUnit unit = opalRuntime.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }

    copyToDestinationDatasource(file, dispatchAttribute, destinationDatasource, unit);
  }

  private void copyToDestinationDatasource(FileObject file, String dispatchAttribute, Datasource destinationDatasource, FunctionalUnit unit) throws IOException, InterruptedException {
    FsDatasource sourceDatasource = new FsDatasource(file.getName().getBaseName(), opalRuntime.getFileSystem().getLocalFile(file), unit.getDatasourceEncryptionStrategy());

    try {
      sourceDatasource.initialise();
      copyValueTables(sourceDatasource, destinationDatasource, unit, dispatchAttribute);
    } finally {
      sourceDatasource.dispose();
    }
  }

  private void copyValueTables(Datasource source, Datasource destination, FunctionalUnit unit, String dispatchAttribute) throws IOException, InterruptedException {
    Set<String> tablesToLock = getTablesToLock(source);
    MagmaEngine.get().lock(tablesToLock);

    try {
      for(ValueTable valueTable : source.getValueTables()) {
        if(Thread.interrupted()) {
          throw new InterruptedException("Thread interrupted");
        }

        if(valueTable.isForEntityType(keysTableEntityType)) {
          copyParticipants(valueTable, source, destination, unit, dispatchAttribute);
        } else {
          DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().build().copy(valueTable, destination);
        }
      }
    } finally {
      MagmaEngine.get().unlock(tablesToLock);
    }
  }

  private Set<String> getTablesToLock(Datasource source) {
    Set<String> tablesToLock = new TreeSet<String>();

    boolean needToLockKeysTable = false;

    for(ValueTable valueTable : source.getValueTables()) {
      tablesToLock.add(valueTable.getDatasource() + "." + valueTable.getName());
      if(valueTable.getEntityType().equals(keysTableEntityType)) {
        needToLockKeysTable = true;
      }
    }

    if(needToLockKeysTable) {
      tablesToLock.add(keysTableReference);
    }

    return tablesToLock;
  }

  private void copyParticipants(ValueTable participantTable, Datasource source, Datasource destination, FunctionalUnit unit, final String dispatchAttribute) throws IOException {
    final String keyVariableName = unit.getKeyVariableName();
    final View privateView = createPrivateView(participantTable, unit);
    final Variable keyVariable = prepareKeysTable(privateView, keyVariableName);
    final OpalPrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(lookupKeysTable(), keyVariable, participantIdentifier);

    PrivateVariableEntityValueTable publicView = createPublicView(participantTable, unit, entityMap);

    // prepare for copying participant data
    final ValueTableWriter keysTableWriter = writeToKeysTable();

    try {
      copyPublicViewToDestinationDatasource(destination, dispatchAttribute, publicView, createKeysListener(privateView, keyVariable, entityMap, keysTableWriter));
    } finally {
      keysTableWriter.close();
    }
  }

  /**
   * This listener will insert all participant identifiers in the keys datasource prior to copying the valueset to the
   * data datasource. It will also generate the public variable entity if it does not exist yet. As such, it must be
   * executed before the ValueSet is copied to the data datasource otherwise, it will not have an associated entity.
   */
  private DatasourceCopyValueSetEventListener createKeysListener(final View privateView, final Variable keyVariable, final OpalPrivateVariableEntityMap entityMap, final ValueTableWriter keysTableWriter) {
    DatasourceCopyValueSetEventListener createKeysListener = new DatasourceCopyValueSetEventListener() {

      public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... destination) {
      }

      public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
        copyParticipantIdentifiers(valueSet.getVariableEntity(), privateView, keyVariable, keysTableWriter, entityMap);
      }

    };
    return createKeysListener;
  }

  private void copyPublicViewToDestinationDatasource(Datasource destination, final String dispatchAttribute, PrivateVariableEntityValueTable publicView, DatasourceCopyValueSetEventListener createKeysListener) throws IOException {
    DatasourceCopier.Builder builder = DatasourceCopier.Builder.newCopier() //
    .withLoggingListener().withThroughtputListener() //
    .withListener(createKeysListener)//
    .withMultiplexingStrategy(new VariableAttributeMutiplexingStrategy(dispatchAttribute, publicView.getName()))//
    .withVariableTransformer(new VariableTransformer() {
      /** Remove the dispatch attribute from the variable name. This is onyx-specific. See OPAL-170 */
      public Variable transform(Variable variable) {
        return Variable.Builder.sameAs(variable).name(variable.hasAttribute(dispatchAttribute) ? variable.getName().replaceFirst("^.*\\.?" + variable.getAttributeStringValue(dispatchAttribute) + "\\.", "") : variable.getName()).build();
      }
    });
    CopyAuditor auditor = auditLogManager.createAuditor(builder, destination, null);
    builder.build()
    // Copy participant's non-identifiable variables and data
    .copy(publicView, destination);
    auditor.completeAuditing();
  }

  /**
   * Creates a {@link View} of the participant table's "private" variables (i.e., identifiers).
   * 
   * @param participantTable
   * @return
   */
  private View createPrivateView(ValueTable participantTable, FunctionalUnit unit) {
    if(unit.getSelect() != null) {
      final View privateView = View.Builder.newView(participantTable.getName(), participantTable).select(unit.getSelect()).build();
      privateView.initialise();
      return privateView;
    } else {
      final View privateView = View.Builder.newView(participantTable.getName(), participantTable).select(new SelectClause() {
        public boolean select(Variable variable) {
          return isIdentifierVariable(variable);
        }
      }).build();
      return privateView;
    }
  }

  /**
   * Wraps the participant table in a {@link View} that exposes public entities and non-identifier variables.
   * 
   * @param participantTable
   * @param entityMap
   * @return
   */
  private PrivateVariableEntityValueTable createPublicView(ValueTable participantTable, final FunctionalUnit unit, final OpalPrivateVariableEntityMap entityMap) {
    PrivateVariableEntityValueTable publicTable = new PrivateVariableEntityValueTable(participantTable.getName(), participantTable, entityMap);
    publicTable.setSelectClause(new SelectClause() {

      public boolean select(Variable variable) {
        return isIdentifierVariable(variable) == false && isIdentifierVariableForUnit(variable, unit) == false;
      }

    });
    publicTable.initialise();
    return publicTable;
  }

  /**
   * Write the key variable.
   * @param privateView
   * @param keyVariableName
   * @return
   * @throws IOException
   */
  private Variable prepareKeysTable(ValueTable privateView, String keyVariableName) throws IOException {

    Variable keyVariable = Variable.Builder.newVariable(keyVariableName, TextType.get(), privateView.getEntityType()).build();

    ValueTableWriter writer = writeToKeysTable();
    try {
      VariableWriter vw = writer.writeVariables();
      try {
        // Create private variables
        vw.writeVariable(keyVariable);
        DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(privateView, lookupKeysTable().getName(), vw);
      } finally {
        vw.close();
      }
    } finally {
      writer.close();
    }
    return keyVariable;
  }

  /**
   * Write the key variable and the identifier variables values; update the participant key private/public map.
   */
  private VariableEntity copyParticipantIdentifiers(VariableEntity publicEntity, ValueTable privateView, Variable keyVariable, ValueTableWriter writer, OpalPrivateVariableEntityMap entityMap) {
    VariableEntity privateEntity = entityMap.privateEntity(publicEntity);

    ValueSetWriter vsw = writer.writeValueSet(publicEntity);
    try {
      // Copy all other private variable values
      DatasourceCopier.Builder.newCopier().dontCopyMetadata().build().copy(privateView, privateView.getValueSet(privateEntity), lookupKeysTable().getName(), vsw);
    } finally {
      try {
        vsw.close();
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      }
    }
    return publicEntity;
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

  private boolean isIdentifierVariableForUnit(Variable variable, FunctionalUnit unit) {
    return (unit.getSelect() != null && unit.getSelect().select(variable));
  }

  /**
   * A MultiplexingStrategy that uses a variable attribute as the destination table name
   */
  static private class VariableAttributeMutiplexingStrategy implements MultiplexingStrategy {

    private final String attributeName;

    private final String defaultName;

    public VariableAttributeMutiplexingStrategy(String attributeName, String defaultName) {
      this.attributeName = attributeName;
      this.defaultName = defaultName;
    }

    public String multiplexVariable(Variable variable) {
      return variable.hasAttribute(attributeName) ? variable.getAttributeStringValue(attributeName) : defaultName;
    }

    public String multiplexValueSet(VariableEntity entity, Variable variable) {
      return multiplexVariable(variable);
    }
  }
}
