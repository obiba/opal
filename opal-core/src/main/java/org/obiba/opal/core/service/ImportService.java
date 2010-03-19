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

import org.apache.commons.vfs.FileObject;
import org.obiba.magma.NoSuchDatasourceException;

/**
 * Service for import-related operations.
 */
public interface ImportService {

  /**
   * Imports data into an Opal datasource.
   * 
   * @param unitName functional unit name
   * @param datasource name of the destination datasource
   * @param file data file to be imported
   * @throws NoSuchDatasourceException if the specified datasource does not exist
   * @throws IllegalArgumentException if the specified file does not exist or is not a normal file
   * @throws IOException on any I/O error
   */
  public void importData(String unitName, String datasourceName, FileObject file) throws NoSuchFunctionalUnitException, IllegalArgumentException, IOException;
}
