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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.DatasourceCopier.Builder;
import org.obiba.magma.views.IncrementalWhereClause;
import org.obiba.magma.views.View;
import org.obiba.opal.core.magma.FunctionalUnitView;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link ExportService}.
 */
@Transactional
public class DefaultExportServiceImpl implements ExportService {

  private OpalRuntime opalRuntime;

  private VariableEntityAuditLogManager auditLogManager;

  /** Configured through org.obiba.opal.keys.tableReference */
  private String keysTableReference;

  /** Configured through org.obiba.opal.keys.entityType */
  private String keysTableEntityType;

  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  public void setAuditLogManager(VariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  public void setKeysTableReference(String keysTableReference) {
    this.keysTableReference = keysTableReference;
  }

  public void setKeysTableEntityType(String keysTableEntityType) {
    this.keysTableEntityType = keysTableEntityType;
  }

  public void exportTablesToDatasource(String unitName, List<String> sourceTableNames, String destinationDatasourceName, boolean incremental) {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName must not be null or empty");
    Datasource destinationDatasource = getDatasourceByName(destinationDatasourceName);
    Set<ValueTable> sourceTables = getValueTablesByName(sourceTableNames);
    DatasourceCopier datasourceCopier = newCopier(destinationDatasource).build();
    exportTablesToDatasource(unitName, sourceTables, destinationDatasource, datasourceCopier, incremental);
  }

  public void exportTablesToDatasource(String unitName, List<String> sourceTableNames, String destinationDatasourceName, DatasourceCopier datasourceCopier, boolean incremental) {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName must not be null or empty");
    Datasource destinationDatasource = getDatasourceByName(destinationDatasourceName);
    Set<ValueTable> sourceTables = getValueTablesByName(sourceTableNames);
    exportTablesToDatasource(unitName, sourceTables, destinationDatasource, datasourceCopier, incremental);
  }

  public void exportTablesToDatasource(String unitName, Set<ValueTable> sourceTables, Datasource destinationDatasource, DatasourceCopier datasourceCopier, boolean incremental) {
    Assert.notEmpty(sourceTables, "sourceTables must not be null or empty");
    Assert.notNull(destinationDatasource, "destinationDatasource must not be null");
    Assert.notNull(datasourceCopier, "datasourceCopier must not be null");

    FunctionalUnit unit = null;
    if(unitName != null) {
      unit = validateFunctionalUnit(unitName);
    }

    validateSourceDatasourceNotEqualDestinationDatasource(sourceTables, destinationDatasource);

    try {
      for(ValueTable table : sourceTables) {
        // If the table contains an entity that requires key separation, create a "unit view" of the table (replace
        // public identifiers with private, unit-specific identifiers).
        table = (unit != null) && table.isForEntityType(keysTableEntityType) ? getUnitView(unit, table) : table;

        // If the incremental option was specified, create an incremental view of the table (leaving out what has
        // already been exported).
        table = incremental ? getIncrementalView(table, destinationDatasource) : table;

        // Go ahead and copy the result to the destination datasource.
        datasourceCopier.copy(table, destinationDatasource);
      }
    } catch(IOException ex) {
      // When implementing the ExcelDatasource:
      // Determine if this the ExcelDatasource. If yes then display the filename.
      throw new ExportException("An error was encountered while exporting to datasource '" + destinationDatasource + "'.", ex);
    }
  }

  public void exportTablesToExcelFile(String unitName, List<String> sourceTableNames, File destinationExcelFile, boolean incremental) {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.notNull(destinationExcelFile, "destinationExcelFile must not be null");

    Datasource outputDatasource = buildExcelDatasource(destinationExcelFile);
    try {
      // Create a DatasourceCopier that will copy only the metadata and export.
      exportTablesToDatasource(unitName, sourceTableNames, outputDatasource.getName(), incremental);
    } finally {
      MagmaEngine.get().removeDatasource(outputDatasource);
    }

  }

  public void exportTablesToExcelFile(String unitName, List<String> sourceTableNames, File destinationExcelFile, DatasourceCopier datasourceCopier, boolean incremental) {
    Assert.notEmpty(sourceTableNames, "sourceTableNames must not be null or empty");
    Assert.notNull(destinationExcelFile, "destinationExcelFile must not be null");

    Datasource outputDatasource = buildExcelDatasource(destinationExcelFile);
    try {
      // Create a DatasourceCopier that will copy only the metadata and export.
      exportTablesToDatasource(unitName, sourceTableNames, outputDatasource.getName(), datasourceCopier, incremental);
    } finally {
      MagmaEngine.get().removeDatasource(outputDatasource);
    }

  }

  private View getUnitView(FunctionalUnit unit, ValueTable valueTable) {
    return new FunctionalUnitView(unit, valueTable, lookupKeysTable());
  }

  private ValueTable lookupKeysTable() {
    return MagmaEngineTableResolver.valueOf(keysTableReference).resolveTable();
  }

  private Datasource buildExcelDatasource(File destinationExcelFile) {
    Datasource outputDatasource = new ExcelDatasource(destinationExcelFile.getName(), destinationExcelFile);
    MagmaEngine.get().addDatasource(outputDatasource);

    return outputDatasource;
  }

  private Datasource getDatasourceByName(String datasourceName) {
    return MagmaEngine.get().getDatasource(datasourceName);
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
    IncrementalWhereClause whereClause = new IncrementalWhereClause(destination.getName() + "." + valueTable.getName());
    whereClause.setAuditLogManager(auditLogManager);

    // Cache the where clause as it is quite expensive. This is ok since the incremental view is transient: used this
    // time only then thrown away.
    return View.Builder.newView(valueTable.getName(), valueTable).where(whereClause).cacheWhere().build();
  }

  public Builder newCopier(Datasource destinationDatasource) {
    return DatasourceCopier.Builder.newCopier().withLoggingListener().withThroughtputListener().withVariableEntityCopyEventListener(auditLogManager, destinationDatasource);
  }

}
