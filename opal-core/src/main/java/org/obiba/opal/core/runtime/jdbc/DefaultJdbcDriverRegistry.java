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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

@Component
public class DefaultJdbcDriverRegistry implements JdbcDriverRegistry {

  private final Map<String, String> driverClassToName = ImmutableMap
      .of("com.mysql.jdbc.Driver", "MySQL", "org.hsqldb.jdbc.JDBCDriver", "HSQLDB");

  private final Map<String, String> driverClassToUrlTemplate = ImmutableMap
      .of("com.mysql.jdbc.Driver", "jdbc:mysql://{hostname}:{port}/{databaseName}", "org.hsqldb.jdbc.JDBCDriver",
          "jdbc:hsqldb:file:{databaseName};shutdown=true;hsqldb.tx=mvcc");

  @Override
  public Iterable<Driver> listDrivers() {
    return Collections.list(DriverManager.getDrivers());
  }

  @Override
  public String getDriverName(Driver driver) {
    return Objects.firstNonNull(driverClassToName.get(driver.getClass().getName()), driver.getClass().getName());
  }

  @Override
  public String getJdbcUrlTemplate(Driver driver) {
    return Objects.firstNonNull(driverClassToUrlTemplate.get(driver.getClass().getName()), "");
  }

  @Override
  public void addDriver(String filename, InputStream jarFile) throws IOException {
    FileOutputStream fos = new FileOutputStream(new File(System.getenv("OPAL_HOME") + "/ext", filename));
    try {
      ByteStreams.copy(jarFile, fos);
    } finally {
      Closeables.closeQuietly(jarFile);
      Closeables.closeQuietly(fos);
    }
  }
}
