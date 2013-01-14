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
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.DatasourceCopier.DatasourceCopyValueSetEventListener;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.FunctionalUnitView;
import org.obiba.opal.core.magma.FunctionalUnitView.Policy;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.obiba.opal.core.magma.concurrent.LockingActionTemplate;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Default implementation of {@link ImportService}.
 */
public class DefaultImportService implements ImportService {
  //
  // Constants
  //

  public static final String STAGE_ATTRIBUTE_NAME = "stage";

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultImportService.class);

  private final TransactionTemplate txTemplate;

  private final FunctionalUnitService functionalUnitService;

  private final OpalRuntime opalRuntime;

  private final IParticipantIdentifier participantIdentifier;

  private final IdentifiersTableService identifiersTableService;

  @Autowired
  public DefaultImportService(TransactionTemplate txTemplate, FunctionalUnitService functionalUnitService,
      OpalRuntime opalRuntime, IParticipantIdentifier participantIdentifier,
      IdentifiersTableService identifiersTableService) {
    if(txTemplate == null) throw new IllegalArgumentException("txManager cannot be null");
    if(functionalUnitService == null) throw new IllegalArgumentException("functionalUnitService cannot be null");
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(participantIdentifier == null) throw new IllegalArgumentException("participantIdentifier cannot be null");
    if(identifiersTableService == null) throw new IllegalArgumentException("identifiersTableService cannot be null");

    this.opalRuntime = opalRuntime;
    this.functionalUnitService = functionalUnitService;
    this.participantIdentifier = participantIdentifier;
    this.identifiersTableService = identifiersTableService;

    this.txTemplate = txTemplate;
    this.txTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
  }

  //
  // ImportService Methods
  //

  @Override
  public void importData(String unitName, FileObject sourceFile, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) throws NoSuchFunctionalUnitException,
      NoSuchDatasourceException, IllegalArgumentException, IOException, InterruptedException {
    // If unitName is the empty string, coerce it to null.
    String nonEmptyUnitName = "".equals(unitName) ? null : unitName;

    if(nonEmptyUnitName != null) Assert.isTrue(!nonEmptyUnitName.equals(FunctionalUnit.OPAL_INSTANCE),
        "unitName cannot be " + FunctionalUnit.OPAL_INSTANCE);
    Assert.hasText(destinationDatasourceName, "datasourceName is null or empty");
    Assert.notNull(sourceFile, "file is null");
    Assert.isTrue(sourceFile.getType() == FileType.FILE, "No such file (" + sourceFile.getName().getPath() + ")");

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);

    FunctionalUnit unit = null;
    if(nonEmptyUnitName != null) {
      unit = functionalUnitService.getFunctionalUnit(nonEmptyUnitName);
      if(unit == null) {
        throw new NoSuchFunctionalUnitException(nonEmptyUnitName);
      }
    }

    copyToDestinationDatasource(sourceFile, destinationDatasource, unit, allowIdentifierGeneration,
        ignoreUnknownIdentifier);
  }

  @Override
  public void importData(String unitName, String sourceDatasourceName, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) throws NoSuchFunctionalUnitException,
      NoSuchDatasourceException, NoSuchValueTableException, IOException, InterruptedException {
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");
    Datasource sourceDatasource = getDatasourceOrTransientDatasource(sourceDatasourceName);

    try {
      importData(unitName, sourceDatasource.getValueTables(), destinationDatasourceName, allowIdentifierGeneration,
          ignoreUnknownIdentifier);
    } finally {
      silentlyDisposeTransientDatasource(sourceDatasource);
    }
  }

  @Override
  public void importData(String unitName, List<String> sourceTableNames, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) throws NoSuchFunctionalUnitException,
      NoSuchDatasourceException, NoSuchValueTableException, NonExistentVariableEntitiesException, IOException,
      InterruptedException {
    Assert.isTrue(sourceTableNames != null, "sourceTableNames is null");
    Assert.isTrue(sourceTableNames.size() > 0, "sourceTableNames is empty");

    ImmutableSet.Builder<ValueTable> builder = ImmutableSet.<ValueTable>builder();
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
        silentlyDisposeTransientDatasource(table.getDatasource());
      }
    }
  }

  @Override
  public void importData(String unitName, Set<ValueTable> sourceTables, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) throws NoSuchFunctionalUnitException,
      NonExistentVariableEntitiesException, IOException, InterruptedException {
    // If unitName is the empty string, coerce it to null.
    String nonEmptyUnitName = (unitName != null && unitName.equals("")) ? null : unitName;

    if(nonEmptyUnitName != null) {
      Assert.isTrue(!nonEmptyUnitName.equals(FunctionalUnit.OPAL_INSTANCE),
          "unitName cannot be " + FunctionalUnit.OPAL_INSTANCE);
    }
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName is null or empty");
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);

    FunctionalUnit unit = null;
    if(nonEmptyUnitName != null) {
      unit = functionalUnitService.getFunctionalUnit(nonEmptyUnitName);
      if(unit == null) {
        throw new NoSuchFunctionalUnitException(nonEmptyUnitName);
      }
    }

    copyValueTables(sourceTables, destinationDatasource, unit, allowIdentifierGeneration, ignoreUnknownIdentifier);
  }

  public int importIdentifiers(String unitName, IParticipantIdentifier pIdentifier) {
    Assert.hasText(unitName, "unitName is null or empty");
    IParticipantIdentifier participantIdentifier = pIdentifier != null ? pIdentifier : this.participantIdentifier;

    FunctionalUnit unit = functionalUnitService.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }

    int count = 0;

    ValueTable keysTable = getIdentifiersValueTable();
    if(keysTable.hasVariable(unit.getKeyVariableName()) == false) {
      try {
        prepareKeysTable(null, unit.getKeyVariableName());
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
    PrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(keysTable,
        keysTable.getVariable(unit.getKeyVariableName()), participantIdentifier);

    for(UnitIdentifier unitId : new FunctionalUnitIdentifiers(keysTable, unit)) {
      // Create a private entity for each missing unitIdentifier
      if(unitId.hasUnitIdentifier() == false) {
        entityMap.createPrivateEntity(unitId.getOpalEntity());
        count++;
      }
    }

    return count;
  }

  @Override
  public void importIdentifiers(String unitName, String sourceDatasourceName, String select) throws IOException {
    Assert.hasText(unitName, "unitName is null or empty");
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    FunctionalUnit unit = functionalUnitService.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
    Datasource sourceDatasource = getDatasourceOrTransientDatasource(sourceDatasourceName);

    importIdentifiers(unit, sourceDatasource, select);
  }

  @Override
  public void importIdentifiers(FunctionalUnit unit, Datasource sourceDatasource, String select) throws IOException {

    try {
      for(ValueTable vt : sourceDatasource.getValueTables()) {
        if(vt.getEntityType().equals(identifiersTableService.getEntityType())) {
          ValueTable sourceKeysTable = createPrivateView(vt, unit, select);
          Variable unitKeyVariable = prepareKeysTable(sourceKeysTable, unit.getKeyVariableName());
          PrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(getIdentifiersValueTable(),
              unitKeyVariable, participantIdentifier);
          for(VariableEntity privateEntity : sourceKeysTable.getVariableEntities()) {
            VariableEntity publicEntity = entityMap.publicEntity(privateEntity);
            if(publicEntity == null) {
              publicEntity = entityMap.createPublicEntity(privateEntity);
            }
            copyParticipantIdentifiers(entityMap.publicEntity(privateEntity), sourceKeysTable, unitKeyVariable,
                writeToKeysTable(), entityMap);
          }
        }
      }
    } finally {
      silentlyDisposeTransientDatasource(sourceDatasource);
    }
  }

  @Override
  public void importIdentifiers(String sourceDatasourceName) throws IOException, NoSuchDatasourceException {
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    importIdentifiers(getDatasourceOrTransientDatasource(sourceDatasourceName));
  }

  @Override
  public void importIdentifiers(Datasource sourceDatasource) throws IOException {
    try {
      if(sourceDatasource.getValueTables().size() == 0) {
        throw new IllegalArgumentException("source identifiers datasource is empty (no tables)");
      }
      String idTableName = getIdentifiersValueTable().getName();
      ValueTable sourceKeysTable = sourceDatasource.hasValueTable(idTableName) ? sourceDatasource
          .getValueTable(idTableName) : sourceDatasource.getValueTables().iterator().next();

      importIdentifiers(sourceKeysTable);

    } finally {
      silentlyDisposeTransientDatasource(sourceDatasource);
    }
  }

  @Override
  public void importIdentifiers(ValueTable sourceKeysTable) throws IOException {
    if(sourceKeysTable.getEntityType().equals(identifiersTableService.getEntityType()) == false) {
      throw new IllegalArgumentException("source identifiers table has unexpected entity type '" + sourceKeysTable
          .getEntityType() + "' (expected '" + identifiersTableService.getEntityType() + "')");
    }

    ValueTable sourceKeysTableCopy = sourceKeysTable;
    String idTableName = getIdentifiersValueTable().getName();
    if(sourceKeysTable.getName().equals(idTableName) == false) {
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
        .copy(sourceKeysTableCopy, getIdentifiersValueTable().getDatasource());
  }

  private Datasource getDatasourceOrTransientDatasource(String datasourceName) throws NoSuchDatasourceException {
    if(MagmaEngine.get().hasDatasource(datasourceName)) {
      return MagmaEngine.get().getDatasource(datasourceName);
    } else {
      return MagmaEngine.get().getTransientDatasourceInstance(datasourceName);
    }
  }

  private void silentlyDisposeTransientDatasource(Datasource datasource) {
    if(MagmaEngine.get().hasTransientDatasource(datasource.getName())) {
      Disposables.silentlyDispose(datasource);
    }
  }

  private void copyToDestinationDatasource(FileObject file, Datasource destinationDatasource, FunctionalUnit unit,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) throws IOException, InterruptedException {
    DatasourceEncryptionStrategy datasourceEncryptionStrategy = null;
    if(unit != null) datasourceEncryptionStrategy = unit.getDatasourceEncryptionStrategy();
    // always wrap fs datasources in onyx datasource to support old onyx data dictionary (from 1.0 to 1.6 version)
    Datasource sourceDatasource = new OnyxDatasource(
        new FsDatasource(file.getName().getBaseName(), opalRuntime.getFileSystem().getLocalFile(file),
            datasourceEncryptionStrategy));

    try {
      sourceDatasource.initialise();
      copyValueTables(sourceDatasource.getValueTables(), destinationDatasource, unit, allowIdentifierGeneration,
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
   * @param unit
   * @param allowIdentifierGeneration
   * @throws IOException
   * @throws InterruptedException
   */
  private void copyValueTables(final Set<ValueTable> sourceTables, final Datasource destination,
      final FunctionalUnit unit, final boolean allowIdentifierGeneration, final boolean ignoreUnknownIdentifier) throws
      IOException, InterruptedException {
    try {
      new LockingActionTemplate() {

        @Override
        protected Set<String> getLockNames() {
          return getTablesToLock(sourceTables);
        }

        @Override
        protected TransactionTemplate getTransactionTemplate() {
          return txTemplate;
        }

        @Override
        protected Action getAction() {
          return new Action() {
            public void execute() throws Exception {
              for(ValueTable valueTable : sourceTables) {
                if(Thread.interrupted()) {
                  throw new InterruptedException("Thread interrupted");
                }

                if(valueTable.isForEntityType(identifiersTableService.getEntityType())) {
                  if(unit != null) {
                    copyParticipants(valueTable, destination, unit, allowIdentifierGeneration, ignoreUnknownIdentifier);
                  } else {
                    addMissingEntitiesToKeysTable(valueTable);
                    MultithreadedDatasourceCopier.Builder.newCopier().withThreads(new ThreadFactory() {
                      @Override
                      public Thread newThread(Runnable r) {
                        return new TransactionalThread(r);
                      }
                    }).withCopier(newCopierForParticipants(valueTable)).from(valueTable).to(destination).build().copy();
                  }
                } else {
                  DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().build()
                      .copy(valueTable, destination);
                }
              }
            }
          };
        }
      }.execute();
    } catch(InvocationTargetException ex) {
      if(ex.getCause() instanceof IOException) {
        throw (IOException) (ex.getCause());
      } else if(ex.getCause() instanceof InterruptedException) {
        throw (InterruptedException) (ex.getCause());
      } else {
        throw new RuntimeException(ex.getCause());
      }
    }
  }

  private Set<VariableEntity> addMissingEntitiesToKeysTable(ValueTable valueTable) {
    Set<VariableEntity> nonExistentVariableEntities = Sets.newHashSet(valueTable.getVariableEntities());

    if(identifiersTableService.hasValueTable()) {
      // Remove all entities that exist in the keys table. Whatever is left are the ones that don't exist...
      Set<VariableEntity> entitiesInKeysTable = getIdentifiersValueTable().getVariableEntities();
      nonExistentVariableEntities.removeAll(entitiesInKeysTable);
    }

    if(nonExistentVariableEntities.size() > 0) {
      ValueTableWriter keysTableWriter = writeToKeysTable();
      try {
        for(VariableEntity ve : nonExistentVariableEntities) {
          keysTableWriter.writeValueSet(ve).close();
        }
      } catch(IOException e) {
        throw new RuntimeException(e);
      } finally {
        Closeables.closeQuietly(keysTableWriter);
      }
    }

    return nonExistentVariableEntities;
  }

  private Set<String> getTablesToLock(Set<ValueTable> sourceTables) {
    Set<String> tablesToLock = new TreeSet<String>();

    boolean needToLockKeysTable = false;

    for(ValueTable valueTable : sourceTables) {
      tablesToLock.add(valueTable.getDatasource() + "." + valueTable.getName());
      if(valueTable.getEntityType().equals(identifiersTableService.getEntityType())) {
        needToLockKeysTable = true;
      }
    }

    if(needToLockKeysTable) {
      tablesToLock.add(identifiersTableService.getTableReference());
    }

    return tablesToLock;
  }

  private void copyParticipants(ValueTable participantTable, Datasource destination, FunctionalUnit unit,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) throws IOException {
    final String keyVariableName = unit.getKeyVariableName();
    final View privateView = createPrivateView(participantTable, unit, null);
    final Variable keyVariable = prepareKeysTable(privateView, keyVariableName);

    final FunctionalUnitView publicView = createPublicView(participantTable, unit, allowIdentifierGeneration,
        ignoreUnknownIdentifier);
    final PrivateVariableEntityMap entityMap = publicView.getPrivateVariableEntityMap();

    // prepare for copying participant data
    final ValueTableWriter keysTableWriter = writeToKeysTable();

    try {
      copyPublicViewToDestinationDatasource(destination, publicView,
          createKeysListener(privateView, keyVariable, entityMap, keysTableWriter));
    } finally {
      keysTableWriter.close();
    }
  }

  /**
   * This listener will insert all participant identifiers in the keys datasource prior to copying the valueset to the
   * data datasource. It will also generate the public variable entity if it does not exist yet. As such, it must be
   * executed before the ValueSet is copied to the data datasource otherwise, it will not have an associated entity.
   */
  private DatasourceCopyValueSetEventListener createKeysListener(final View privateView, final Variable keyVariable,
      final PrivateVariableEntityMap entityMap, final ValueTableWriter keysTableWriter) {
    DatasourceCopyValueSetEventListener createKeysListener = new DatasourceCopyValueSetEventListener() {

      public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... destination) {
      }

      public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
        copyParticipantIdentifiers(valueSet.getVariableEntity(), privateView, keyVariable, keysTableWriter, entityMap);
      }

    };
    return createKeysListener;
  }

  private DatasourceCopier.Builder newCopierForParticipants(final ValueTable sourceTable) {
    return DatasourceCopier.Builder.newCopier() //
        .withLoggingListener().withThroughtputListener();
  }

  private void copyPublicViewToDestinationDatasource(Datasource destination, FunctionalUnitView publicView,
      DatasourceCopyValueSetEventListener createKeysListener) throws IOException {
    newCopierForParticipants(publicView) //
        .withListener(createKeysListener).build()
        // Copy participant's non-identifiable variables and data
        .copy(publicView, destination);
  }

  /**
   * Creates a {@link View} of the participant table's "private" variables (i.e., identifiers).
   *
   * @param viewName
   * @param participantTable
   * @return
   */
  private View createPrivateView(String viewName, ValueTable participantTable, FunctionalUnit unit, String select) {
    if(select != null) {
      final View privateView = View.Builder.newView(viewName, participantTable).select(new JavascriptClause(select))
          .build();
      privateView.initialise();
      return privateView;
    } else if(unit.getSelect() != null) {
      final View privateView = View.Builder.newView(viewName, participantTable).select(unit.getSelect()).build();
      privateView.initialise();
      return privateView;
    } else {
      final View privateView = View.Builder.newView(viewName, participantTable).select(new SelectClause() {
        public boolean select(Variable variable) {
          return isIdentifierVariable(variable);
        }
      }).build();
      return privateView;
    }
  }

  private View createPrivateView(ValueTable participantTable, FunctionalUnit unit, String select) {
    return createPrivateView(participantTable.getName(), participantTable, unit, select);
  }

  /**
   * Wraps the participant table in a {@link View} that exposes public entities and non-identifier variables.
   *
   * @param participantTable
   * @param unit
   * @param allowIdentifierGeneration
   * @return
   */
  private FunctionalUnitView createPublicView(ValueTable participantTable, final FunctionalUnit unit,
      final boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) {
    FunctionalUnitView publicTable = new FunctionalUnitView(unit, Policy.UNIT_IDENTIFIERS_ARE_PRIVATE, participantTable,
        getIdentifiersValueTable(), allowIdentifierGeneration ? participantIdentifier : null, ignoreUnknownIdentifier);
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
   *
   * @param privateView
   * @param keyVariableName
   * @return
   * @throws IOException
   */
  private Variable prepareKeysTable(ValueTable privateView, String keyVariableName) throws IOException {

    Variable keyVariable = Variable.Builder
        .newVariable(keyVariableName, TextType.get(), identifiersTableService.getEntityType()).build();

    ValueTableWriter writer = writeToKeysTable();
    try {
      VariableWriter vw = writer.writeVariables();
      try {
        // Create private variables
        vw.writeVariable(keyVariable);
        if(privateView != null) {
          DatasourceCopier.Builder.newCopier().dontCopyValues().build()
              .copy(privateView, getIdentifiersValueTable().getName(), vw);
        }
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
  private VariableEntity copyParticipantIdentifiers(VariableEntity publicEntity, ValueTable privateView,
      Variable keyVariable, ValueTableWriter writer, PrivateVariableEntityMap entityMap) {
    VariableEntity privateEntity = entityMap.privateEntity(publicEntity);

    ValueSetWriter vsw = writer.writeValueSet(publicEntity);
    try {
      // Copy all other private variable values
      DatasourceCopier.Builder.newCopier().dontCopyMetadata().build()
          .copy(privateView, privateView.getValueSet(privateEntity), getIdentifiersValueTable().getName(), vsw);
    } finally {
      try {
        vsw.close();
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      }
    }
    return publicEntity;
  }

  private ValueTable getIdentifiersValueTable() {
    return identifiersTableService.getValueTable();
  }

  private ValueTableWriter writeToKeysTable() {
    return identifiersTableService.createValueTableWriter();
  }

  private boolean isIdentifierVariable(Variable variable) {
    return variable.hasAttribute("identifier") && (variable.getAttribute("identifier").getValue()
        .equals(BooleanType.get().trueValue()) || variable.getAttribute("identifier").getValue().toString()
        .toLowerCase().equals("true"));
  }

  private boolean isIdentifierVariableForUnit(Variable variable, FunctionalUnit unit) {
    return (unit.getSelect() != null && unit.getSelect().select(variable));
  }

  class TransactionalThread extends Thread {

    private Runnable runnable;

    public TransactionalThread(Runnable runnable) {
      this.runnable = runnable;
    }

    public void run() {
      txTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          runnable.run();
        }
      });
    }
  }

}
