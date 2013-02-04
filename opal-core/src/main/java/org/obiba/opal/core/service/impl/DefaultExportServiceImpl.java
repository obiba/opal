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

import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.DatasourceCopier.Builder;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.magma.views.IncrementalWhereClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;
import org.obiba.opal.core.magma.FunctionalUnitView;
import org.obiba.opal.core.magma.FunctionalUnitView.Policy;
import org.obiba.opal.core.magma.concurrent.LockingActionTemplate;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.common.base.Function;

/**
 * Default implementation of {@link ExportService}.
 */
public class DefaultExportServiceImpl implements ExportService {

  private final ThreadFactory threadFactory;

  private final TransactionTemplate txTemplate;

  private final FunctionalUnitService functionalUnitService;

  private final IdentifiersTableService identifiersTableService;

  @Autowired
  public DefaultExportServiceImpl(ThreadFactory threadFactory, TransactionTemplate txTemplate,
      FunctionalUnitService functionalUnitService, IdentifiersTableService identifiersTableService) {
    if(threadFactory == null) throw new IllegalArgumentException("threadFactory cannot be null");
    if(txTemplate == null) throw new IllegalArgumentException("txTemplate cannot be null");
    if(functionalUnitService == null) throw new IllegalArgumentException("functionalUnitService cannot be null");
    if(identifiersTableService == null) throw new IllegalArgumentException("identifiersTableService cannot be null");

    this.threadFactory = threadFactory;
    this.txTemplate = txTemplate;
    this.functionalUnitService = functionalUnitService;
    this.identifiersTableService = identifiersTableService;
  }

  public Builder newCopier(Datasource destinationDatasource,
      @Nullable Function<VariableEntity, VariableEntity> entityMapper) {
    return DatasourceCopier.Builder.newCopier().withLoggingListener().withThroughtputListener();
  }

  @Override
  public Builder newCopier(Datasource destinationDatasource) {
    return newCopier(destinationDatasource, null);
  }

  @Override
  public void exportTablesToDatasource(String unitName, List<String> sourceTableNames, String destinationDatasourceName,
      boolean incremental) throws InterruptedException {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName must not be null or empty");
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    Set<ValueTable> sourceTables = getValueTablesByName(sourceTableNames);
    exportTablesToDatasource(unitName, sourceTables, destinationDatasource, newCopier(destinationDatasource),
        incremental);
  }

  @Override
  public void exportTablesToDatasource(String unitName, List<String> sourceTableNames, String destinationDatasourceName,
      DatasourceCopier.Builder datasourceCopier, boolean incremental) throws InterruptedException {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName must not be null or empty");
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(destinationDatasourceName);
    Set<ValueTable> sourceTables = getValueTablesByName(sourceTableNames);
    exportTablesToDatasource(unitName, sourceTables, destinationDatasource, datasourceCopier, incremental);
  }

  @Override
  public void exportTablesToDatasource(String unitName, Set<ValueTable> sourceTables, Datasource destinationDatasource,
      DatasourceCopier.Builder datasourceCopier, boolean incremental) throws InterruptedException {
    Assert.notEmpty(sourceTables, "sourceTables must not be null or empty");
    Assert.notNull(destinationDatasource, "destinationDatasource must not be null");
    Assert.notNull(datasourceCopier, "datasourceCopier must not be null");

    FunctionalUnit unit = unitName == null ? null : validateFunctionalUnit(unitName);

    validateSourceDatasourceNotEqualDestinationDatasource(sourceTables, destinationDatasource);

    try {
      new ExportActionTemplate(sourceTables, destinationDatasource, datasourceCopier, incremental, unit).execute();
    } catch(InvocationTargetException ex) {
      if(ex.getCause() instanceof ExportException) {
        throw (ExportException) ex.getCause();
      }
      if(ex.getCause() instanceof InterruptedException) {
        throw (InterruptedException) ex.getCause();
      }
      throw new RuntimeException(ex.getCause());
    }
  }

  private Set<ValueTable> getValueTablesByName(Iterable<String> tableNames)
      throws NoSuchDatasourceException, NoSuchValueTableException, ExportException {
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
    FunctionalUnit unit = functionalUnitService.getFunctionalUnit(unitName);
    if(unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
    return unit;
  }

  private void validateSourceDatasourceNotEqualDestinationDatasource(Iterable<ValueTable> sourceTables,
      Datasource destinationDatasource) {
    for(ValueTable sourceTable : sourceTables) {
      if(sourceTable.getDatasource().equals(destinationDatasource)) {
        throw new ExportException(
            "Cannot export when datasource of source table '" + sourceTable.getDatasource().getName() + "." +
                sourceTable.getName() + "' matches the destination datasource '" + destinationDatasource.getName() +
                "'.");
      }
    }
  }

  private class ExportActionTemplate extends LockingActionTemplate {

    private final Set<ValueTable> sourceTables;

    private final Datasource destinationDatasource;

    private final Builder datasourceCopier;

    private final boolean incremental;

    private final FunctionalUnit unit;

    private ExportActionTemplate(Set<ValueTable> sourceTables, Datasource destinationDatasource,
        Builder datasourceCopier, boolean incremental, @Nullable FunctionalUnit unit) {
      this.sourceTables = sourceTables;
      this.destinationDatasource = destinationDatasource;
      this.datasourceCopier = datasourceCopier;
      this.incremental = incremental;
      this.unit = unit;
    }

    @Override
    protected Set<String> getLockNames() {
      Set<String> tablesToLock = new TreeSet<String>();
      for(ValueTable valueTable : sourceTables) {
        tablesToLock.add(destinationDatasource.getName() + "." + valueTable.getName());
      }
      return tablesToLock;
    }

    @Override
    protected TransactionTemplate getTransactionTemplate() {
      return txTemplate;
    }

    @Override
    protected Action getAction() {
      return new ExportAction();
    }

    private class ExportAction implements Action {
      @Override
      public void execute() throws Exception {
        try {
          for(ValueTable table : sourceTables) {
            exportTableToDatasource(table);
          }
        } catch(IOException ex) {
          // When implementing the ExcelDatasource:
          // Determine if this the ExcelDatasource. If yes then display the filename.
          throw new ExportException(
              "An error was encountered while exporting to datasource '" + destinationDatasource + "'.", ex);
        }
      }

      private void exportTableToDatasource(ValueTable table) throws InterruptedException, IOException {
        if(Thread.interrupted()) {
          throw new InterruptedException("Thread interrupted");
        }

        // If the incremental option was specified, create an incremental view of the table (leaving out what has
        // already been exported).
        ValueTable tableToCopy = incremental ? getIncrementalView(table, destinationDatasource) : table;

        // If the table contains an entity that requires key separation, create a "unit view" of the table (replace
        // public identifiers with private, unit-specific identifiers).
        if(unit != null && tableToCopy.isForEntityType(identifiersTableService.getEntityType())) {
          // Make a view that converts opal identifiers to unit identifiers
          tableToCopy = new FunctionalUnitView(unit, Policy.UNIT_IDENTIFIERS_ARE_PUBLIC, tableToCopy,
              getIdentifiersValueTable());
        }

        // Go ahead and copy the result to the destination datasource.
        MultithreadedDatasourceCopier.Builder.newCopier().from(tableToCopy).to(destinationDatasource)
            .withCopier(datasourceCopier).withReaders(4).withThreads(threadFactory).build().copy();
      }

      private ValueTable getIdentifiersValueTable() {
        return identifiersTableService.getValueTable();
      }

      private ValueTable getIncrementalView(ValueTable valueTable, Datasource destination) {
        WhereClause whereClause = new IncrementalWhereClause(destination.getName() + "." + valueTable.getName());
        return View.Builder.newView(valueTable.getName(), valueTable).where(whereClause).build();
      }
    }
  }
}
