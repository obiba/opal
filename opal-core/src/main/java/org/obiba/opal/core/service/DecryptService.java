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
import java.io.IOException;

/**
 * Service for decrypt operations.
 */
public interface DecryptService {
  /**
   * Decrypts data into an Opal datasource.
   * 
   * @param unitName name of the functional unit (this determines this keystore should be used)
   * @param datasourceName name of the destination datasource
   * @param file data file to be decrypted
   * @throws NoSuchFunctionalUnitException if the specified unit does not exist
   * @throws IllegalArgumentException if the specified datasource does not exist
   * @throws IOException if the specified file does not exist or is not a normal file
   */
  public void decryptData(String unitName, String datasourceName, File file) throws NoSuchFunctionalUnitException, IllegalArgumentException, IOException;

  /**
   * Equivalent to <code>decryptData(FunctionalUnit.OPAL_INSTANCE, datasourceName, file)</code>.
   */
  public void decryptData(String datasourceName, File file) throws NoSuchFunctionalUnitException, IllegalArgumentException, IOException;

}
