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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.DatasourceCopier.Builder;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.magma.views.IncrementalWhereClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.obiba.opal.core.magma.IdentifiersMappingView.Policy;
import org.obiba.opal.core.magma.concurrent.LockingActionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Strings;

/**
 * Default implementation of {@link DataExportService}.
 */
@Component
public class DataExportServiceImpl implements DataExportService {

  @NotNull
  private final ThreadFactory threadFactory;

  @NotNull
  private final TransactionTemplate txTemplate;

  @NotNull
  private final IdentifiersTableService identifiersTableService;

  @Autowired
  public DataExportServiceImpl(@NotNull ThreadFactory threadFactory, @NotNull TransactionTemplate txTemplate,
      @NotNull IdentifiersTableService identifiersTableService) {
    this.threadFactory = threadFactory;
    this.txTemplate = txTemplate;
    this.identifiersTableService = identifiersTableService;
  }

  @Override
  public Builder newCopier(Datasource destinationDatasource) {
    return DatasourceCopier.Builder.newCopier().withLoggingListener().withThroughtputListener();
  }

  @Override
  public void exportTablesToDatasource(@Nullable String idMapping, @NotNull Set<ValueTable> sourceTables,
      @NotNull Datasource destinationDatasource, @NotNull DatasourceCopier.Builder datasourceCopier,
      boolean incremental, @Nullable DatasourceCopierProgressListener progressListener) throws InterruptedException {
    if(!Strings.isNullOrEmpty(idMapping) && !identifiersTableService.hasIdentifiersMapping(idMapping))
      throw new NoSuchIdentifiersMappingException(idMapping);

    validateSourceDatasourceNotEqualDestinationDatasource(sourceTables, destinationDatasource);

    try {
      new ExportActionTemplate(sourceTables, destinationDatasource, datasourceCopier, incremental, idMapping,
          progressListener).execute();
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

  private void validateSourceDatasourceNotEqualDestinationDatasource(@NotNull Iterable<ValueTable> sourceTables,
      @NotNull Datasource destinationDatasource) {
    for(ValueTable sourceTable : sourceTables) {
      if(sourceTable.getDatasource().equals(destinationDatasource)) {
        throw new ExportException(
            "Cannot export when datasource of source table '" + sourceTable.getDatasource().getName() + "." +
                sourceTable.getName() + "' matches the destination datasource '" + destinationDatasource.getName() +
                "'."
        );
      }
    }
  }

  private class ExportActionTemplate extends LockingActionTemplate {

    @NotNull
    private final Set<ValueTable> sourceTables;

    @NotNull
    private final Datasource destinationDatasource;

    @NotNull
    private final Builder datasourceCopier;

    private final boolean incremental;

    @Nullable
    private final String idMapping;

    @Nullable
    private final DatasourceCopierProgressListener progressListener;

    private ExportActionTemplate(@NotNull Set<ValueTable> sourceTables, @NotNull Datasource destinationDatasource,
        @NotNull Builder datasourceCopier, boolean incremental, @Nullable String idMapping,
        @Nullable DatasourceCopierProgressListener progressListener) {
      this.sourceTables = sourceTables;
      this.destinationDatasource = destinationDatasource;
      this.datasourceCopier = datasourceCopier;
      this.incremental = incremental;
      this.idMapping = idMapping;
      this.progressListener = progressListener;
    }

    @NotNull
    @Override
    protected Set<String> getLockNames() {
      Set<String> tablesToLock = new TreeSet<>();
      for(ValueTable valueTable : sourceTables) {
        tablesToLock.add(destinationDatasource.getName() + "." + valueTable.getName());
      }
      return tablesToLock;
    }

    @Override
    protected TransactionTemplate getTransactionTemplate() {
      return txTemplate;
    }

    @NotNull
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

      private void exportTableToDatasource(@NotNull ValueTable table) throws InterruptedException, IOException {
        if(Thread.interrupted()) {
          throw new InterruptedException("Thread interrupted");
        }

        // If the incremental option was specified, create an incremental view of the table (leaving out what has
        // already been exported).
        ValueTable tableToCopy = incremental ? getIncrementalView(table, destinationDatasource) : table;

        // If the table contains an entity that requires key separation, create a "unit view" of the table (replace
        // public identifiers with private, unit-specific identifiers).
        if(!Strings.isNullOrEmpty(idMapping) &&
            identifiersTableService.hasIdentifiersMapping(tableToCopy.getEntityType(), idMapping)) {
          // Make a view that converts opal identifiers to unit identifiers
          tableToCopy = new IdentifiersMappingView(idMapping, Policy.UNIT_IDENTIFIERS_ARE_PUBLIC, tableToCopy,
              identifiersTableService.getIdentifiersTable(tableToCopy.getEntityType()));
        }

        // Go ahead and copy the result to the destination datasource.
        MultithreadedDatasourceCopier.Builder.newCopier().from(tableToCopy).to(destinationDatasource)
            .withCopier(datasourceCopier).withReaders(4).withProgressListener(progressListener)
            .withThreads(threadFactory).build().copy();
      }

      @NotNull
      private ValueTable getIncrementalView(@NotNull ValueTable valueTable, @NotNull Datasource destination) {
        WhereClause whereClause = new IncrementalWhereClause(destination.getName() + "." + valueTable.getName());
        return View.Builder.newView(valueTable.getName(), valueTable).where(whereClause).build();
      }
    }
  }
}
