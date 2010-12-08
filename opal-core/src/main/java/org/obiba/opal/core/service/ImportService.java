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
   * Imports data into an Opal datasource .
   * 
   * @param unitName functional unit name
   * @param datasource name of the destination datasource
   * @param file data file to be imported
   * @throws NoSuchDatasourceException if the specified datasource does not exist
   * @throws IllegalArgumentException if the specified file does not exist or is not a normal file
   * @throws IOException on any I/O error
   * @throws InterruptedException if the current thread was interrupted
   */
  public void importData(String unitName, String datasourceName, FileObject file) throws NoSuchFunctionalUnitException, IllegalArgumentException, IOException, InterruptedException;

  /**
   * Imports data from a source Opal datasource into a destination Opal datasource. Usually the source datasource will
   * be a transient datasource created temporarily when importing from a non-datasource source such as a csv file or
   * excel file.
   * @param unitName functional unit name
   * @param sourceDatasourceName name of the source datasource
   * @param destinationDatasourceName name of the destination datasource
   * @throws NoSuchFunctionalUnitException
   * @throws NonExistentVariableEntitiesException if unitName is null and the source entities do not exist as public
   * keys in the opal keys database
   * @throws IOException on any I/O error
   * @throws InterruptedException if the current thread was interrupted
   */
  public void importData(String unitName, String sourceDatasourceName, String destinationDatasourceName) throws NoSuchFunctionalUnitException, NonExistentVariableEntitiesException, IOException, InterruptedException;

  /**
   * Import identifiers of the provided datasource into Opal identifiers datasource, as values of the unit key variable
   * name.
   * 
   * @param unitName functional unit name
   * @param sourceDatasourceName name of the source datasource
   */
  public void importIdentifiers(String unitName, String sourceDatasourceName);

  /**
   * Import identifiers of the provided datasource into Opal identifiers datasource, as values of the key table.
   * 
   * @param sourceDatasourceName name of the source datasource
   */
  public void importIdentifiers(String sourceDatasourceName);
}
