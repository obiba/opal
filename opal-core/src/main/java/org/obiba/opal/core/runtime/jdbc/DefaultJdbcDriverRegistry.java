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
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

@Component
public class DefaultJdbcDriverRegistry implements JdbcDriverRegistry {

  private static final Map<String, String> SUPPORTED_DRIVER_CLASS_TO_NAME = ImmutableMap
      .of("com.mysql.jdbc.Driver", "MySQL", //
          "org.hsqldb.jdbc.JDBCDriver", "HSQLDB");

  private static final Map<String, String> DRIVER_CLASS_TO_URL_TEMPLATE = ImmutableMap
      .of("com.mysql.jdbc.Driver", "jdbc:mysql://{hostname}:{port}/{databaseName}", //
          "org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:file:{databaseName};shutdown=true;hsqldb.tx=mvcc");

  private static final Map<String, String> DRIVER_CLASS_TO_URL_EXAMPLE = ImmutableMap
      .of("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/opal", //
          "org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:file:opal;shutdown=true;hsqldb.tx=mvcc");

  @Override
  public Iterable<Driver> listDrivers() {
    return Iterables.filter(Collections.list(DriverManager.getDrivers()), new Predicate<Driver>() {
      @Override
      public boolean apply(Driver driver) {
        return SUPPORTED_DRIVER_CLASS_TO_NAME.containsKey(driver.getClass().getName());
      }
    });
  }

  @Override
  public String getDriverName(Driver driver) {
    String className = driver.getClass().getName();
    return Objects.firstNonNull(SUPPORTED_DRIVER_CLASS_TO_NAME.get(className), className);
  }

  @Override
  public String getJdbcUrlTemplate(Driver driver) {
    return Objects.firstNonNull(DRIVER_CLASS_TO_URL_TEMPLATE.get(driver.getClass().getName()), "");
  }

  @Override
  public String getJdbcUrlExample(Driver driver) {
    return Objects.firstNonNull(DRIVER_CLASS_TO_URL_EXAMPLE.get(driver.getClass().getName()), "");
  }

  @Override
  public void addDriver(String filename, InputStream jarFile) throws IOException {
    FileOutputStream fos = new FileOutputStream(new File(System.getenv("OPAL_HOME") + "/ext", filename));
    try {
      ByteStreams.copy(jarFile, fos);
    } finally {
      try {
        if(jarFile != null) jarFile.close();
      } catch(IOException ignored) {
      }
      try {
        fos.close();
      } catch(IOException ignored) {
      }
    }
  }
}
