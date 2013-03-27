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
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.NonExistentVariableEntitiesException;
import org.obiba.opal.core.support.OnyxDatasource;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers.UnitIdentifier;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Default implementation of {@link ImportService}.
 */
@SuppressWarnings("OverlyCoupledClass")
public class DefaultImportService implements ImportService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultImportService.class);

  private final TransactionTemplate txTemplate;

  private final FunctionalUnitService functionalUnitService;

  private final OpalRuntime opalRuntime;

  private final IParticipantIdentifier participantIdentifier;

  private final IdentifiersTableService identifiersTableService;

  private final IdentifierService identifierService;

  @Autowired
  public DefaultImportService(TransactionTemplate txTemplate, FunctionalUnitService functionalUnitService,
      OpalRuntime opalRuntime, IParticipantIdentifier participantIdentifier,
      IdentifiersTableService identifiersTableService, IdentifierService identifierService) {

    Assert.notNull(txTemplate, "txManager cannot be null");
    Assert.notNull(functionalUnitService, "functionalUnitService cannot be null");
    Assert.notNull(opalRuntime, "opalRuntime cannot be null");
    Assert.notNull(participantIdentifier, "participantIdentifier cannot be null");
    Assert.notNull(identifiersTableService, "identifiersTableService cannot be null");

    this.txTemplate = txTemplate;
    this.txTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
    this.functionalUnitService = functionalUnitService;
    this.opalRuntime = opalRuntime;
    this.participantIdentifier = participantIdentifier;
    this.identifierService = identifierService;
    this.identifiersTableService = identifiersTableService;
  }

  @Override
  public void importData(@Nullable String unitName, @Nonnull FileObject sourceFile,
      @Nonnull String destinationDatasourceName, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws NoSuchFunctionalUnitException, NoSuchDatasourceException, IllegalArgumentException, IOException,
      InterruptedException {
    // If unitName is the empty string, coerce it to null
    String nonEmptyUnitName = Strings.emptyToNull(unitName);
    Assert.isTrue(!Objects.equal(nonEmptyUnitName, FunctionalUnit.OPAL_INSTANCE),
        "unitName cannot be " + FunctionalUnit.OPAL_INSTANCE);
    Assert.hasText(destinationDatasourceName, "datasourceName is null or empty");
    Assert.notNull(sourceFile, "file is null");
    Assert.isTrue(sourceFile.getType() == FileType.FILE, "No such file (" + sourceFile.getName().getPath() + ")");

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);

    FunctionalUnit unit = getFunctionalUnit(nonEmptyUnitName);

    copyToDestinationDatasource(sourceFile, destinationDatasource, unit, allowIdentifierGeneration,
        ignoreUnknownIdentifier);
  }

  @Nullable
  private FunctionalUnit getFunctionalUnit(@Nullable String unitName) {
    FunctionalUnit unit = functionalUnitService.getFunctionalUnit(unitName);
    if(unitName != null && unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
    return unit;
  }

  @Override
  public void importData(String unitName, @Nonnull String sourceDatasourceName, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws NoSuchFunctionalUnitException, NoSuchDatasourceException, NoSuchValueTableException, IOException,
      InterruptedException {
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    Datasource sourceDatasource = getDatasourceOrTransientDatasource(sourceDatasourceName);
    try {
      importData(unitName, sourceDatasource.getValueTables(), destinationDatasourceName, allowIdentifierGeneration,
          ignoreUnknownIdentifier);
    } finally {
      MagmaEngine.get().removeTransientDatasource(sourceDatasource.getName());
    }
  }

  @Override
  public void importData(String unitName, @Nonnull List<String> sourceTableNames, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws NoSuchFunctionalUnitException, NoSuchDatasourceException, NoSuchValueTableException,
      NonExistentVariableEntitiesException, IOException, InterruptedException {
    Assert.notNull(sourceTableNames, "sourceTableNames is null");
    Assert.notEmpty(sourceTableNames, "sourceTableNames is empty");

    ImmutableSet.Builder<ValueTable> builder = ImmutableSet.builder();
    for(String tableName : sourceTableNames) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      Datasource ds = getDatasourceOrTransientDatasource(resolver.getDatasourceName());
      builder.add(ds.getValueTable(resolver.getTableName()));
    }
    Set<ValueTable> sourceTables = builder.build();
    try {
      importData(unitName, sourceTables, destinationDatasourceName, allowIdentifierGeneration, ignoreUnknownIdentifier);
    } finally {
      for(ValueTable table : sourceTables) {
        MagmaEngine.get().removeTransientDatasource(table.getDatasource().getName());
      }
    }
  }

  @Override
  public void importData(String unitName, Set<ValueTable> sourceTables, @Nonnull String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws NoSuchFunctionalUnitException, NonExistentVariableEntitiesException, IOException, InterruptedException {
    // If unitName is the empty string, coerce it to null
    String nonEmptyUnitName = Strings.emptyToNull(unitName);
    Assert.isTrue(!Objects.equal(nonEmptyUnitName, FunctionalUnit.OPAL_INSTANCE),
        "unitName cannot be " + FunctionalUnit.OPAL_INSTANCE);
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName is null or empty");

    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    copyValueTables(sourceTables, destinationDatasource, allowIdentifierGeneration, ignoreUnknownIdentifier);
  }

  @Override
  public int importIdentifiers(@Nonnull String unitName, IParticipantIdentifier pIdentifier) {
    Assert.hasText(unitName, "unitName is null or empty");
    IParticipantIdentifier localParticipantIdentifier = pIdentifier == null ? participantIdentifier : pIdentifier;

    FunctionalUnit unit = functionalUnitService.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }

    int count = 0;

    ValueTable keysTable = identifiersTableService.getValueTable();
    if(!keysTable.hasVariable(unit.getKeyVariableName())) {
      identifierService.createKeyVariable(null, unit.getKeyVariableName());
    }
    PrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(keysTable,
        keysTable.getVariable(unit.getKeyVariableName()), localParticipantIdentifier);

    for(UnitIdentifier unitId : new FunctionalUnitIdentifiers(keysTable, unit)) {
      // Create a private entity for each missing unitIdentifier
      if(!unitId.hasUnitIdentifier()) {
        entityMap.createPrivateEntity(unitId.getOpalEntity());
        count++;
      }
    }

    return count;
  }

  @Override
  public void importIdentifiers(@Nonnull String unitName, @Nonnull String sourceDatasourceName, String select)
      throws IOException {
    Assert.hasText(unitName, "unitName is null or empty");
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    FunctionalUnit unit = functionalUnitService.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
    Datasource sourceDatasource = getDatasourceOrTransientDatasource(sourceDatasourceName);

    importIdentifiers(unit, sourceDatasource, select);
  }

  @SuppressWarnings("OverlyNestedMethod")
  @Override
  public void importIdentifiers(FunctionalUnit unit, Datasource sourceDatasource, String select) throws IOException {

    try {
      for(ValueTable vt : sourceDatasource.getValueTables()) {
        if(vt.getEntityType().equals(identifiersTableService.getEntityType())) {
          ValueTable sourceKeysTable = identifierService.createPrivateView(vt.getName(), vt, unit, select);
          Variable unitKeyVariable = identifierService.createKeyVariable(sourceKeysTable, unit.getKeyVariableName());
          PrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(identifiersTableService.getValueTable(),
              unitKeyVariable, participantIdentifier);
          ValueTableWriter identifiersTableWriter = identifiersTableService.createValueTableWriter();
          try {
            for(VariableEntity privateEntity : sourceKeysTable.getVariableEntities()) {
              if(entityMap.publicEntity(privateEntity) == null) {
                entityMap.createPublicEntity(privateEntity);
              }
              identifierService
                  .copyParticipantIdentifiers(entityMap.publicEntity(privateEntity), sourceKeysTable, entityMap,
                      identifiersTableWriter);
            }
          } finally {
            Closeables.closeQuietly(identifiersTableWriter);
          }
        }
      }
    } finally {
      MagmaEngine.get().removeTransientDatasource(sourceDatasource.getName());
    }
  }

  @Override
  public void importIdentifiers(@Nonnull String sourceDatasourceName) throws IOException, NoSuchDatasourceException {
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    importIdentifiers(getDatasourceOrTransientDatasource(sourceDatasourceName));
  }

  @Override
  public void importIdentifiers(Datasource sourceDatasource) throws IOException {
    try {
      if(sourceDatasource.getValueTables().isEmpty()) {
        throw new IllegalArgumentException("source identifiers datasource is empty (no tables)");
      }
      String idTableName = identifiersTableService.getValueTable().getName();
      ValueTable sourceKeysTable = sourceDatasource.hasValueTable(idTableName) //
          ? sourceDatasource.getValueTable(idTableName) //
          : sourceDatasource.getValueTables().iterator().next();

      importIdentifiers(sourceKeysTable);

    } finally {
      MagmaEngine.get().removeTransientDatasource(sourceDatasource.getName());
    }
  }

  @Override
  public void importIdentifiers(ValueTable sourceKeysTable) throws IOException {

    Assert.isTrue(!sourceKeysTable.getEntityType().equals(identifiersTableService.getEntityType()),
        "source identifiers table has unexpected entity type '" + sourceKeysTable.getEntityType() + "' (expected '" +
            identifiersTableService.getEntityType() + "')");

    ValueTable sourceKeysTableCopy = sourceKeysTable;
    String idTableName = identifiersTableService.getValueTable().getName();
    if(!sourceKeysTable.getName().equals(idTableName)) {
      ImmutableSet.Builder<String> builder = ImmutableSet.builder();
      builder.addAll(Iterables.transform(sourceKeysTable.getVariableEntities(), new Function<VariableEntity, String>() {

        @Override
        public String apply(VariableEntity input) {
          return input.getIdentifier();
        }
      }));
      sourceKeysTableCopy = new StaticValueTable(sourceKeysTable.getDatasource(), idTableName, builder.build(),
          identifiersTableService.getEntityType());
    }

    // Don't copy null values otherwise, we'll delete existing mappings
    DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().build()
        .copy(sourceKeysTableCopy, identifiersTableService.getValueTable().getDatasource());
  }

  private Datasource getDatasourceOrTransientDatasource(String datasourceName) throws NoSuchDatasourceException {
    return MagmaEngine.get().hasDatasource(datasourceName)
        ? MagmaEngine.get().getDatasource(datasourceName)
        : MagmaEngine.get().getTransientDatasourceInstance(datasourceName);
  }

  private void copyToDestinationDatasource(FileObject file, Datasource destinationDatasource,
      @Nullable FunctionalUnit unit, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws IOException, InterruptedException {
    DatasourceEncryptionStrategy datasourceEncryptionStrategy = null;
    if(unit != null) datasourceEncryptionStrategy = unit.getDatasourceEncryptionStrategy();
    // always wrap fs datasources in onyx datasource to support old onyx data dictionary (from 1.0 to 1.6 version)
    Datasource sourceDatasource = new OnyxDatasource(
        new FsDatasource(file.getName().getBaseName(), opalRuntime.getFileSystem().getLocalFile(file),
            datasourceEncryptionStrategy));

    try {
      sourceDatasource.initialise();
      copyValueTables(sourceDatasource.getValueTables(), destinationDatasource, allowIdentifierGeneration,
          ignoreUnknownIdentifier);
    } finally {
      sourceDatasource.dispose();
    }
  }

  /**
   * Copy the specified set a tables.
   *
   * @param sourceTables
   * @param destination
   * @param allowIdentifierGeneration
   * @throws IOException
   * @throws InterruptedException
   */
  private void copyValueTables(Set<ValueTable> sourceTables, Datasource destination, boolean allowIdentifierGeneration,
      boolean ignoreUnknownIdentifier) throws IOException, InterruptedException {
    try {
      new CopyValueTablesLockingAction(identifiersTableService, identifierService, txTemplate, sourceTables,
          destination, allowIdentifierGeneration, ignoreUnknownIdentifier).execute();
    } catch(InvocationTargetException ex) {
      if(ex.getCause() instanceof IOException) {
        throw (IOException) ex.getCause();
      }
      if(ex.getCause() instanceof InterruptedException) {
        throw (InterruptedException) ex.getCause();
      }
      throw new RuntimeException(ex.getCause());
    }
  }

}
