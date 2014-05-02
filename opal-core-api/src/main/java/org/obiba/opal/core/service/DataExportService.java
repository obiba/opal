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

import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.DatasourceCopier;

/**
 * Service for export operations. Export allows Magma tables to be copied to an existing Datasource or to an Excel file.
 */
public interface DataExportService {

  /**
   * Get a new datasource builder, with default logging facilities.
   *
   * @param destinationDatasource
   * @return
   */
  DatasourceCopier.Builder newCopier(Datasource destinationDatasource);

  /**
   * Export tables to the provided {@link Datasource} using the provided {@link DatasourceCopier}. If logging is
   * required ensure that the {@code DatasourceCopier} is configured with an appropriate logger. It is the
   * responsibility of the caller to remove the {@code destinationDatasource} from {@code Magma}.
   *
   * @param idMapping the variable name in the identifiers table
   * @param sourceTables tables to export.
   * @param destinationDatasource tables will be copied to this existing Datasource.
   * @param datasourceCopier copier used to perform the copy.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @param progressListener
   * @throws NoSuchIdentifiersMappingException if a unit has been specified that does not exist
   * @throws ExportException if the datasource of a sourceTable matches the destinationDatasource.
   * @throws InterruptedException if the current thread was interrupted
   */
  void exportTablesToDatasource(@Nullable String idMapping, @NotNull Set<ValueTable> sourceTables,
      @NotNull Datasource destinationDatasource, @NotNull DatasourceCopier.Builder datasourceCopier,
      boolean incremental, @Nullable DatasourceCopierProgressListener progressListener) throws InterruptedException;

}
