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
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.unit.FunctionalUnit;

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
   * Import identifiers using the given participant identifiers provider.
   * @param unitName
   * @param pIdentifier
   * @return the number of identifiers added
   */
  public int importIdentifiers(String unitName, IParticipantIdentifier pIdentifier);

  /**
   * Import identifiers of the provided datasource into Opal identifiers datasource, as values of the unit key variable
   * name.
   * 
   * @param unitName functional unit name
   * @param sourceDatasourceName name of the source datasource
   * @param select an option script for select variables representing identifiers. If not specified unit select clause
   * is used if this one is defined.
   * @throws NoSuchFunctionalUnitException if the specified functional unit does not exist
   * @throws NoSuchDatasourceException if the specified source datasource does not exist
   * @throws NoSuchValueTableException if the specified source datasource does not contain an identifiers table (i.e., a
   * table with the same name as <code>org.obiba.opal.keys.tableReference</code>)
   * @throws IOException on any I/O error
   */
  public void importIdentifiers(String unitName, String sourceDatasourceName, String select) throws NoSuchFunctionalUnitException, NoSuchDatasourceException, NoSuchValueTableException, IOException;

  /**
   * Import identifiers of the provided datasource into Opal identifiers datasource, as values of the key table.
   * 
   * @param sourceDatasourceName name of the source datasource
   * @throws NoSuchDatasourceException if the specified datasource does not exist
   * @throws NoSuchValueTableException if the specified source datasource does not contain an identifiers table (i.e., a
   * table with the same name as <code>org.obiba.opal.keys.tableReference</code>)
   * @throws IOException on any I/O error
   */
  public void importIdentifiers(String sourceDatasourceName) throws NoSuchDatasourceException, NoSuchValueTableException, IOException;

  /**
   * Import identifiers mapping, do not add new entities.
   * 
   * @param unitName the unit to be used to retrieve the entities given the unit's identifier
   * @param units the list of units which identifiers are to be updated (column headers)
   * @param mapping the identifiers (rows in the same order as the list of units)
   * @return
   * @throws NoSuchDatasourceException
   * @throws NoSuchValueTableException
   * @throws IOException
   */
  public int importIdentifiersMapping(String unitName, List<FunctionalUnit> units, List<String[]> mapping) throws NoSuchDatasourceException, NoSuchValueTableException, IOException;
}
