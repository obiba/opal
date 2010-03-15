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

import java.io.File;
import java.util.List;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.DatasourceCopier;

/**
 * Service for export operations. Export allows Magma tables to be copied to an existing Datasource or to an Excel file.
 */
public interface ExportService {

  /**
   * Get a new datasource builder, with default logging facilities.
   * @param destinationDatasource
   * @return
   */
  public DatasourceCopier.Builder newCopier(Datasource destinationDatasource);

  /**
   * Export tables to an existing Datasource. This export operation will be logged.
   * @param sourceTableNames tables to export.
   * @param destinationDatasourceName tables will be copied to this existing Datasource.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws NoSuchDatasourceException if the destinationDatasourceName does not exist.
   * @throws NoSuchValueTableException if a sourceTableName does not exist.
   * @throws ExportException if the sourceTableNames are not unique or if the datasource of a sourceTableName matches
   * the destinationDatasource.
   */
  public void exportTablesToDatasource(List<String> sourceTableNames, String destinationDatasourceName, boolean incremental);

  /**
   * Export tables to an existing Datasource using the provided {@link DatasourceCopier}. If logging is required ensure
   * that the {@code DatasourceCopier} is configured with an appropriate logger.
   * @param sourceTableNames tables to export.
   * @param destinationDatasourceName tables will be copied to this existing Datasource.
   * @param datasourceCopier copier used to perform the copy.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws NoSuchDatasourceException if the destinationDatasourceName does not exist.
   * @throws NoSuchValueTableException if a sourceTableName does not exist.
   * @throws ExportException if the sourceTableNames are not unique or if the datasource of a sourceTableName matches
   * the destinationDatasource.
   */
  public void exportTablesToDatasource(List<String> sourceTableNames, String destinationDatasourceName, DatasourceCopier datasourceCopier, boolean incremental);

  /**
   * Export tables to the provided {@link Datasource} using the provided {@link DatasourceCopier}. If logging is
   * required ensure that the {@code DatasourceCopier} is configured with an appropriate logger. It is the
   * responsibility of the caller to remove the {@code destinationDatasource} from {@code Magama}.
   * @param sourceTables tables to export.
   * @param destinationDatasource tables will be copied to this existing Datasource.
   * @param datasourceCopier copier used to perform the copy.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws ExportException if the datasource of a sourceTable matches the destinationDatasource.
   */
  public void exportTablesToDatasource(Set<ValueTable> sourceTables, Datasource destinationDatasource, DatasourceCopier datasourceCopier, boolean incremental);

  /**
   * Export tables to an Excel file. This export operation will be logged.
   * @param sourceTableNames tables to export.
   * @param destinationExcelFilename tables will be copied to this Excel file.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws UnsupportedOperationException Exporting to an Excel file is not currently implemented.
   */
  public void exportTablesToExcelFile(List<String> sourceTableNames, File destinationExcelFile, boolean incremental);

  /**
   * Export tables to an Excel file. This export operation will be logged.
   * @param sourceTableNames tables to export.
   * @param destinationExcelFilename tables will be copied to this Excel file.
   * @param datasourceCopier copier used to perform the copy.
   * @param incremental if <code>true</code> the tables are exported incrementally (updates only)
   * @throws UnsupportedOperationException Exporting to an Excel file is not currently implemented.
   */
  public void exportTablesToExcelFile(List<String> sourceTableNames, File destinationExcelFile, DatasourceCopier datasourceCopier, boolean incremental);

}
