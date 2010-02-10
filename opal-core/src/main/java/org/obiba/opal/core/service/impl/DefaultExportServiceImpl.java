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
import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.audit.hibernate.HibernateVariableEntityAuditLogManager;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.opal.core.service.ExportService;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link ExportService}.
 */
public class DefaultExportServiceImpl implements ExportService {

  private HibernateVariableEntityAuditLogManager auditLogManager;

  public void setAuditLogManager(HibernateVariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  public void exportTablesToDatasource(List<String> fromTableNames, String destinationDatasourceName) {
    Assert.notEmpty(fromTableNames, "fromTableNames must not be null or empty");
    Assert.hasText(destinationDatasourceName, "destinationDatasourceName must not be null or empty");
    Datasource destinationDatasource = getDatasourceByName(destinationDatasourceName);
    List<ValueTable> sourceTables = getValueTablesByName(fromTableNames);
    throw new UnsupportedOperationException("Exporting to an existing datasource destination is not currently supported.");
  }

  public void exportTablesToExcelFile(List<String> fromTableNames, File destinationExcelFile) {
    Assert.notEmpty(fromTableNames, "fromTableNames must not be null or empty");
    Assert.notNull(destinationExcelFile, "destinationExcelFile must not be null");
    throw new UnsupportedOperationException("Exporting to an Excel file is not currently supported.");
  }

  private Datasource getDatasourceByName(String datasourceName) {
    Datasource datasource = MagmaEngine.get().getDatasource(datasourceName);

    if(datasource == null) {
      throw new NoSuchDatasourceException("No such datasource '" + datasourceName + "'.");
    }
    return datasource;
  }

  private List<ValueTable> getValueTablesByName(List<String> tableNames) throws NoSuchDatasourceException, NoSuchValueTableException {
    List<ValueTable> tables = new ArrayList<ValueTable>(tableNames.size());
    for(String tableName : tableNames) {
      // Resolver expects ValueTable names to be delimited with a colon.
      tables.add(MagmaEngineReferenceResolver.valueOf(tableName + ":").resolveTable());
    }
    return tables;
  }

}
