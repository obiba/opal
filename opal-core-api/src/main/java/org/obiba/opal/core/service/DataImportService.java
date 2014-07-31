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
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;

/**
 * Service for import-related operations.
 */
public interface DataImportService {

  /**
   * Imports data from a source Opal datasource into a destination Opal datasource. Usually the source datasource will
   * be a transient datasource created temporarily when importing from a non-datasource source such as a csv file or
   * excel file.
   *
   * @param sourceDatasourceName name of the source datasource
   * @param destinationDatasourceName name of the destination datasource
   * @param allowIdentifierGeneration unknown participant will be created at importation time
   * @param ignoreUnknownIdentifier
   * @param progressListener
   * @throws NoSuchIdentifiersMappingException
   * @throws NonExistentVariableEntitiesException if unitName is null and the source entities do not exist as public
   * keys in the opal keys database
   * @throws IOException on any I/O error
   * @throws InterruptedException if the current thread was interrupted
   */
  void importData(String sourceDatasourceName, String destinationDatasourceName, boolean allowIdentifierGeneration,
      boolean ignoreUnknownIdentifier, @Nullable DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NoSuchDatasourceException, NoSuchValueTableException,
      NonExistentVariableEntitiesException, IOException, InterruptedException;

  /**
   * Imports data from a source Opal tables into a destination Opal datasource.
   *
   * @param sourceTableNames
   * @param destinationDatasourceName
   * @param allowIdentifierGeneration
   * @param ignoreUnknownIdentifier
   * @param progressListener
   * @throws NoSuchIdentifiersMappingException
   * @throws NonExistentVariableEntitiesException
   * @throws IOException
   * @throws InterruptedException
   */
  void importData(List<String> sourceTableNames, String destinationDatasourceName, boolean allowIdentifierGeneration,
      boolean ignoreUnknownIdentifier, @Nullable DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NoSuchDatasourceException, NoSuchValueTableException,
      NonExistentVariableEntitiesException, IOException, InterruptedException;

  /**
   * Imports data from a source table into a destination Opal datasource.
   *
   * @param sourceValueTables
   * @param destinationDatasourceName
   * @param allowIdentifierGeneration
   * @param ignoreUnknownIdentifier
   * @param progressListener
   */
  void importData(Set<ValueTable> sourceValueTables, String destinationDatasourceName,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier,
      @Nullable DatasourceCopierProgressListener progressListener)
      throws NoSuchIdentifiersMappingException, NonExistentVariableEntitiesException, IOException, InterruptedException;

}
