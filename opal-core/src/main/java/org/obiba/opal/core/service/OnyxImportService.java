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
import java.util.Date;
import java.util.List;

/**
 * This service provides coarse-grained methods for importing data.
 * 
 * All the business logic related to imports is encapsulated by these methods, including both updates to the Participant
 * Key database (which stores participant identifiers) and the Opal database (which stores the imported variables).
 */
public interface OnyxImportService {

  /**
   * Imports all data files, from the default import repository, exploring sub directories by date order, and site name
   * by alphabetical order.
   * 
   * @param username user name (required for authentication)
   * @param password password (required for authentication)
   */
  public void importData(String username, String password);

  /**
   * Imports data for the specified date and/or site.
   * 
   * If tags are specified, applies these tags to created, updated elements (DEF, Variable) and added data.
   * 
   * @param username user name (required for authentication)
   * @param password password (required for authentication)
   * @param date specifies the date of the data to import (if <code>null</code> all dates included)
   * @param site specifies the site origin of the data to import (if <code>null</code> all sites included)
   * @param tags tags to be applied to created, updated elements (DEF, Variable) and added data (if <code>null</code>
   * no tags applied)
   */
  public void importData(String username, String password, Date date, String site, List<String> tags);

  /**
   * Imports data from the specified source file or directory.
   * 
   * If tags are specified, applies these tags to created, updated elements (DEF, Variable) and added data.
   * 
   * @param username user name (required for authentication)
   * @param password password (required for authentication)
   * @param date specifies the date of the data to import (if <code>null</code> all dates included)
   * @param site specifies the site origin of the data to import (if <code>null</code> all sites included)
   * @param tags tags to be applied to created, updated elements (DEF, Variable) and added data (if <code>null</code>
   * no tags applied)
   * @param source source of data to import (either the data file or the import directory)
   */
  public void importData(String username, String password, List<String> tags, File source);
}
