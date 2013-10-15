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

  Iterable<Driver> listDrivers();

  String getDriverName(Driver driver);

  String getJdbcUrlTemplate(Driver driver);

  void addDriver(String filename, InputStream jarFile) throws IOException;

  String getJdbcUrlExample(Driver driver);
}
