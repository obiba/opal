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
import java.util.List;

import org.obiba.magma.audit.hibernate.HibernateVariableEntityAuditLogManager;
import org.obiba.opal.core.service.ExportService;

/**
 * Default implementation of {@link ExportService}.
 */
public class DefaultExportServiceImpl implements ExportService {

  private HibernateVariableEntityAuditLogManager auditLogManager;

  public void setAuditLogManager(HibernateVariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  public void exportTablesToDatasource(List<String> fromTableNames, String destinationDatasourceName) {
    throw new UnsupportedOperationException("Exporting to an existing datasource destination is not currently supported.");
  }

  public void exportTablesToExcelFile(List<String> fromTableNames, File destinationExcelFile) {
    throw new UnsupportedOperationException("Exporting to an Excel file is not currently supported.");
  }

}
