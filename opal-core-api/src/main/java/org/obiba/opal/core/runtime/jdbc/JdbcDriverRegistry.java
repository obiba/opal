/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Driver;

public interface JdbcDriverRegistry {

  public Iterable<Driver> listDrivers();

  public String getDriverName(Driver driver);

  public String getJdbcUrlTemplate(Driver driver);

  public void addDriver(String filename, InputStream jarFile) throws IOException;

}
