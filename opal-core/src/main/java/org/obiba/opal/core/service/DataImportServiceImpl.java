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
import java.util.*;

import javax.validation.constraints.NotNull;

import org.obiba.magma.*;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

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
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifierService identifierService;

  @Autowired
  private ValidationService validationService;

  @Override
  public void importData(@NotNull String sourceDatasourceName, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier,
      DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NoSuchDatasourceException, NoSuchValueTableException, IOException,
      InterruptedException {
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    Datasource sourceDatasource = getDatasourceOrTransientDatasource(sourceDatasourceName);
    try {
      importData(sourceDatasource.getValueTables(), destinationDatasourceName, allowIdentifierGeneration,
          ignoreUnknownIdentifier, progressListener);
    } finally {
      MagmaEngine.get().removeTransientDatasource(sourceDatasource.getName());
    }
  }

  @Override
  public void importData(@NotNull List<String> sourceTableNames, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier,
      DatasourceCopierProgressListener progressListener, ValidationService.ValidationListener validationListener)
      throws NoSuchIdentifiersMappingException, NoSuchDatasourceException, NoSuchValueTableException,
      NonExistentVariableEntitiesException, IOException, InterruptedException {
    Assert.notNull(sourceTableNames, "sourceTableNames is null");
    Assert.notEmpty(sourceTableNames, "sourceTableNames is empty");

    ImmutableSet.Builder<ValueTable> builder = ImmutableSet.builder();
    Datasource targetDatasource = getDatasourceOrTransientDatasource(destinationDatasourceName);

    for(String tableName : sourceTableNames) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      Datasource ds = getDatasourceOrTransientDatasource(resolver.getDatasourceName());

      //builder.add(ds.getValueTable(resolver.getTableName()));
      ValueTable valueTable = ds.getValueTable(resolver.getTableName());
      if (validationListener != null) {
          this.validationService.validateData(targetDatasource, valueTable, validationListener);
      }
      builder.add(valueTable);

    }
    Set<ValueTable> sourceTables = builder.build();
    try {
      importData(sourceTables, destinationDatasourceName, allowIdentifierGeneration, ignoreUnknownIdentifier,
          progressListener);
    } finally {
      for(ValueTable table : sourceTables) {
        MagmaEngine.get().removeTransientDatasource(table.getDatasource().getName());
      }
    }
  }

  @Override
  public void importData(Set<ValueTable> sourceTables, @NotNull String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier,
      DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NonExistentVariableEntitiesException, IOException,
      InterruptedException {
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName is null or empty");

    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    copyValueTables(sourceTables, destinationDatasource, allowIdentifierGeneration, ignoreUnknownIdentifier,
        progressListener);
  }

  //
  // Private methods
  //

  private Datasource getDatasourceOrTransientDatasource(String datasourceName) throws NoSuchDatasourceException {
    return MagmaEngine.get().hasDatasource(datasourceName)
        ? MagmaEngine.get().getDatasource(datasourceName)
        : MagmaEngine.get().getTransientDatasourceInstance(datasourceName);
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
      boolean ignoreUnknownIdentifier, DatasourceCopierProgressListener progressListener)
      throws IOException, InterruptedException {
    try {
      new CopyValueTablesLockingAction(identifiersTableService, identifierService, txTemplate, sourceTables,
          destination, allowIdentifierGeneration, ignoreUnknownIdentifier, progressListener).execute();
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
