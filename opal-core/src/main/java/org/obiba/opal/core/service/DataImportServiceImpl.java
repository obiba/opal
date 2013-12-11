/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.core.support.OnyxDatasource;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Default implementation of {@link DataImportService}.
 */
@Component
public class DataImportServiceImpl implements DataImportService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DataImportServiceImpl.class);

  @Autowired
  private TransactionTemplate txTemplate;

  @Autowired
  private FunctionalUnitService functionalUnitService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifierService identifierService;

  @Autowired
  private ProjectsKeyStoreService projectskeyStoreService;

  @Autowired
  private ProjectService projectService;

  @Override
  public void importData(@Nullable String unitName, @NotNull FileObject sourceFile,
      @NotNull String destinationDatasourceName, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
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
  public void importData(@NotNull String sourceDatasourceName, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws NoSuchFunctionalUnitException, NoSuchDatasourceException, NoSuchValueTableException, IOException,
      InterruptedException {
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    Datasource sourceDatasource = getDatasourceOrTransientDatasource(sourceDatasourceName);
    try {
      importData(sourceDatasource.getValueTables(), destinationDatasourceName, allowIdentifierGeneration,
          ignoreUnknownIdentifier);
    } finally {
      MagmaEngine.get().removeTransientDatasource(sourceDatasource.getName());
    }
  }

  @Override
  public void importData(@NotNull List<String> sourceTableNames, String destinationDatasourceName,
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
      importData(sourceTables, destinationDatasourceName, allowIdentifierGeneration, ignoreUnknownIdentifier);
    } finally {
      for(ValueTable table : sourceTables) {
        MagmaEngine.get().removeTransientDatasource(table.getDatasource().getName());
      }
    }
  }

  @Override
  public void importData(Set<ValueTable> sourceTables, @NotNull String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws NoSuchFunctionalUnitException, NonExistentVariableEntitiesException, IOException, InterruptedException {
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName is null or empty");

    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    copyValueTables(sourceTables, destinationDatasource, allowIdentifierGeneration, ignoreUnknownIdentifier);
  }

  //
  // Private methods
  //

  private Datasource getDatasourceOrTransientDatasource(String datasourceName) throws NoSuchDatasourceException {
    return MagmaEngine.get().hasDatasource(datasourceName)
        ? MagmaEngine.get().getDatasource(datasourceName)
        : MagmaEngine.get().getTransientDatasourceInstance(datasourceName);
  }

  private void copyToDestinationDatasource(FileObject file, Datasource destinationDatasource,
      @Nullable FunctionalUnit unit, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier)
      throws IOException, InterruptedException {

    if(unit != null && unit.getDatasourceEncryptionStrategy() == null) {
      DatasourceEncryptionStrategy encryptionStrategy = new EncryptedSecretKeyDatasourceEncryptionStrategy();
      encryptionStrategy.setKeyProvider(projectskeyStoreService.getKeyStore(projectService.getProject(unit.getName())));
      unit.setDatasourceEncryptionStrategy(encryptionStrategy);
    }

    DatasourceEncryptionStrategy datasourceEncryptionStrategy = unit == null
        ? null
        : unit.getDatasourceEncryptionStrategy();
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
  @SuppressWarnings("ChainOfInstanceofChecks")
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
