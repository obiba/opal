/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import com.google.common.collect.ImmutableSet;
import org.obiba.magma.*;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link DataImportService}.
 */
@Component
public class DataImportServiceImpl implements DataImportService {

  private static final Logger log = LoggerFactory.getLogger(DataImportServiceImpl.class);

  @Autowired
  private TransactionTemplate txTemplate;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifierService identifierService;

  @Autowired
  private IdentifierGenerator identifierGenerator;

  @Override
  public void importData(@NotNull String sourceDatasourceName, String destinationDatasourceName,
                         String idMapping, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier,
                         DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NoSuchDatasourceException, NoSuchValueTableException, IOException,
      InterruptedException {
    Assert.hasText(sourceDatasourceName, "sourceDatasourceName is null or empty");

    Datasource sourceDatasource = getDatasourceOrTransientDatasource(sourceDatasourceName);
    try {
      importData(sourceDatasource.getValueTables(), destinationDatasourceName, idMapping, allowIdentifierGeneration,
          ignoreUnknownIdentifier, progressListener);
    } finally {
      MagmaEngine.get().removeTransientDatasource(sourceDatasource.getName());
    }
  }

  @Override
  public void importData(@NotNull List<String> sourceTableNames, String destinationDatasourceName,
                         String idMapping, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier,
                         DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NoSuchDatasourceException, NoSuchValueTableException,
      NonExistentVariableEntitiesException, IOException, InterruptedException {
    Assert.notNull(sourceTableNames, "sourceTableNames is null");
    Assert.notEmpty(sourceTableNames, "sourceTableNames is empty");

    ImmutableSet.Builder<ValueTable> builder = ImmutableSet.builder();
    for (String tableName : sourceTableNames) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      Datasource ds = getDatasourceOrTransientDatasource(resolver.getDatasourceName());
      builder.add(ds.getValueTable(resolver.getTableName()));
    }
    Set<ValueTable> sourceTables = builder.build();
    try {
      importData(sourceTables, destinationDatasourceName, idMapping, allowIdentifierGeneration, ignoreUnknownIdentifier,
          progressListener);
    } finally {
      for (ValueTable table : sourceTables) {
        MagmaEngine.get().removeTransientDatasource(table.getDatasource().getName());
      }
    }
  }

  @Override
  public void importData(Set<ValueTable> sourceTables, @NotNull String destinationDatasourceName,
                         String idMapping, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier,
                         DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NonExistentVariableEntitiesException, IOException,
      InterruptedException {
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName is null or empty");

    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    CopyValueTablesOptions copyValueTableOptions = new CopyValueTablesOptions(sourceTables, destinationDatasource, idMapping,
        allowIdentifierGeneration, ignoreUnknownIdentifier);
    copyValueTables(copyValueTableOptions, progressListener);
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
   * @param copyValueTablesOptions
   * @param progressListener
   * @throws IOException
   * @throws InterruptedException
   */
  @SuppressWarnings("ChainOfInstanceofChecks")
  private void copyValueTables(CopyValueTablesOptions copyValueTablesOptions, DatasourceCopierProgressListener progressListener)
      throws IOException, InterruptedException {
    try {
      new CopyValueTablesLockingAction(identifiersTableService, identifierService, identifierGenerator, txTemplate,
          copyValueTablesOptions, progressListener).execute();
    } catch (InvocationTargetException ex) {
      if (ex.getCause() instanceof IOException) {
        throw (IOException) ex.getCause();
      }
      if (ex.getCause() instanceof InterruptedException) {
        throw (InterruptedException) ex.getCause();
      }
      throw new RuntimeException(ex.getCause());
    }
  }

}
