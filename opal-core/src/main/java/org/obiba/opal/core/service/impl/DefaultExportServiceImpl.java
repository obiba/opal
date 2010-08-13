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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.magma.support.DatasourceCopier.Builder;
import org.obiba.magma.views.IncrementalWhereClause;
import org.obiba.magma.views.View;
import org.obiba.opal.core.magma.FunctionalUnitView;
import org.obiba.opal.core.magma.FunctionalUnitView.Policy;
import org.obiba.opal.core.magma.concurrent.LockingActionTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.common.base.Function;

/**
 * Default implementation of {@link ExportService}.
 */
public class DefaultExportServiceImpl implements ExportService {

  private final PlatformTransactionManager txManager;

  private final OpalRuntime opalRuntime;

  /** Configured through org.obiba.opal.keys.tableReference */
  private final String keysTableReference;

  /** Configured through org.obiba.opal.keys.entityType */
  private final String keysTableEntityType;

  @Autowired
  public DefaultExportServiceImpl(PlatformTransactionManager txManager, OpalRuntime opalRuntime, @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.keys.tableReference}") String keysTableReference, @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.keys.entityType}") String keysTableEntityType) {
    if(txManager == null) throw new IllegalArgumentException("txManager cannot be null");
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(keysTableReference == null) throw new IllegalArgumentException("keysTableReference cannot be null");
    if(keysTableEntityType == null) throw new IllegalArgumentException("keysTableEntityType cannot be null");

    this.txManager = txManager;
    this.opalRuntime = opalRuntime;
    this.keysTableReference = keysTableReference;
    this.keysTableEntityType = keysTableEntityType;
  }

  public Builder newCopier(Datasource destinationDatasource, Function<VariableEntity, VariableEntity> entityMapper) {
    return DatasourceCopier.Builder.newCopier().withLoggingListener().withThroughtputListener();
  }

  public Builder newCopier(Datasource destinationDatasource) {
    return newCopier(destinationDatasource, null);
  }

  public void exportTablesToDatasource(String unitName, List<String> sourceTableNames, String destinationDatasourceName, boolean incremental) throws InterruptedException {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName must not be null or empty");
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    Set<ValueTable> sourceTables = getValueTablesByName(sourceTableNames);
    exportTablesToDatasource(unitName, sourceTables, destinationDatasource, newCopier(destinationDatasource), incremental);
  }

  public void exportTablesToDatasource(String unitName, List<String> sourceTableNames, String destinationDatasourceName, DatasourceCopier.Builder datasourceCopier, boolean incremental) throws InterruptedException {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName must not be null or empty");
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    Set<ValueTable> sourceTables = getValueTablesByName(sourceTableNames);
    exportTablesToDatasource(unitName, sourceTables, destinationDatasource, datasourceCopier, incremental);
  }

  public void exportTablesToDatasource(final String unitName, final Set<ValueTable> sourceTables, final Datasource destinationDatasource, final DatasourceCopier.Builder datasourceCopier, final boolean incremental) throws InterruptedException {
    Assert.notEmpty(sourceTables, "sourceTables must not be null or empty");
    Assert.notNull(destinationDatasource, "destinationDatasource must not be null");
    Assert.notNull(datasourceCopier, "datasourceCopier must not be null");

    final FunctionalUnit unit = (unitName != null) ? validateFunctionalUnit(unitName) : null;

    validateSourceDatasourceNotEqualDestinationDatasource(sourceTables, destinationDatasource);

    try {
      new LockingActionTemplate() {

        @Override
        protected Set<String> getLockNames() {
          return getTablesToLock(sourceTables, destinationDatasource);
        }

        @Override
        protected TransactionTemplate getTransactionTemplate() {
          return new TransactionTemplate(txManager);
        }

        @Override
        protected Action getAction() {
          return new Action() {
            public void execute() throws Exception {
              try {
                for(ValueTable table : sourceTables) {
                  exportTableToDatasource(destinationDatasource, datasourceCopier, incremental, unit, table);
                }
              } catch(IOException ex) {
                // When implementing the ExcelDatasource:
                // Determine if this the ExcelDatasource. If yes then display the filename.
                throw new ExportException("An error was encountered while exporting to datasource '" + destinationDatasource + "'.", ex);
              }
            }
          };
        }
      }.execute();
    } catch(InvocationTargetException ex) {
      if(ex.getCause() instanceof ExportException) {
        throw (ExportException) (ex.getCause());
      } else if(ex.getCause() instanceof InterruptedException) {
        throw (InterruptedException) (ex.getCause());
      } else {
        throw new RuntimeException(ex.getCause());
      }
    }
  }

  private Set<String> getTablesToLock(Set<ValueTable> sourceTables, Datasource destination) {
    Set<String> tablesToLock = new TreeSet<String>();

    for(ValueTable valueTable : sourceTables) {
      tablesToLock.add(destination.getName() + "." + valueTable.getName());
    }

    return tablesToLock;
  }

  private void exportTableToDatasource(Datasource destinationDatasource, DatasourceCopier.Builder datasourceCopier, boolean incremental, FunctionalUnit unit, ValueTable table) throws InterruptedException, IOException {
    if(Thread.interrupted()) {
      throw new InterruptedException("Thread interrupted");
    }

    // If the incremental option was specified, create an incremental view of the table (leaving out what has
    // already been exported).
    table = incremental ? getIncrementalView(table, destinationDatasource) : table;

    // If the table contains an entity that requires key separation, create a "unit view" of the table (replace
    // public identifiers with private, unit-specific identifiers).
    if((unit != null) && table.isForEntityType(keysTableEntityType)) {
      table = getUnitView(unit, table);
    }

    // Go ahead and copy the result to the destination datasource.
    MultithreadedDatasourceCopier.Builder.newCopier().from(table).to(destinationDatasource).withCopier(datasourceCopier).withReaders(4).withThreads(new ThreadFactory() {

      @Override
      public Thread newThread(Runnable r) {
        return new TransactionalThread(txManager, r);
      }
    }).build().copy();
  }

  private FunctionalUnitView getUnitView(FunctionalUnit unit, ValueTable valueTable) {
    // Make a view that converts opal identifiers to unit identifiers
    return new FunctionalUnitView(unit, Policy.UNIT_IDENTIFIERS_ARE_PUBLIC, valueTable, lookupKeysTable());
  }

  private ValueTable lookupKeysTable() {
    return MagmaEngineTableResolver.valueOf(keysTableReference).resolveTable();
  }

  private Set<ValueTable> getValueTablesByName(List<String> tableNames) throws NoSuchDatasourceException, NoSuchValueTableException, ExportException {
    Set<ValueTable> tables = new HashSet<ValueTable>();
    for(String tableName : tableNames) {
      try {
        if(!tables.add(MagmaEngineTableResolver.valueOf(tableName).resolveTable())) {
          throw new ExportException("Source tables include duplicate '" + tableName + "'.");
        }
      } catch(IllegalArgumentException e) {
        throw new ExportException("Source table '" + tableName + "' does not exist.");
      }
    }
    return tables;
  }

  private FunctionalUnit validateFunctionalUnit(String unitName) {
    FunctionalUnit unit = opalRuntime.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
    return unit;
  }

  private void validateSourceDatasourceNotEqualDestinationDatasource(Set<ValueTable> sourceTables, Datasource destinationDatasource) {
    for(ValueTable sourceTable : sourceTables) {
      if(sourceTable.getDatasource().equals(destinationDatasource)) {
        throw new ExportException("Cannot export when datasource of source table '" + sourceTable.getDatasource().getName() + "." + sourceTable.getName() + "' matches the destintation datasource '" + destinationDatasource.getName() + "'.");
      }
    }
  }

  private View getIncrementalView(ValueTable valueTable, Datasource destination) {
    IncrementalWhereClause whereClause = new IncrementalWhereClause(valueTable.getDatasource().getName() + "." + valueTable.getName(), destination.getName() + "." + valueTable.getName());

    return View.Builder.newView(valueTable.getName(), valueTable).where(whereClause).build();
  }

  static class TransactionalThread extends Thread {

    private PlatformTransactionManager txManager;

    private Runnable runnable;

    public TransactionalThread(PlatformTransactionManager txManager, Runnable runnable) {
      this.txManager = txManager;
      this.runnable = runnable;
    }

    public void run() {
      new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          runnable.run();
        }
      });
    }
  }

}
