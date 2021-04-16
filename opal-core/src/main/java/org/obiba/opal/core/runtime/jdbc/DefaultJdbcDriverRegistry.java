/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.jdbc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

@Component
public class DefaultJdbcDriverRegistry implements JdbcDriverRegistry {

  private static final Map<String, String> SUPPORTED_DRIVER_CLASS_TO_NAME = ImmutableMap
      .of("com.mysql.jdbc.Driver", "MySQL");

  private static final Map<String, String> DRIVER_CLASS_TO_URL_TEMPLATE = ImmutableMap
      .of("com.mysql.jdbc.Driver", "jdbc:mysql://{hostname}:{port}/{databaseName}");

  private static final Map<String, String> DRIVER_CLASS_TO_URL_EXAMPLE = ImmutableMap
      .of("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/opal");

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
    return Optional.ofNullable(SUPPORTED_DRIVER_CLASS_TO_NAME.get(className)).orElse(className);
  }

  @Override
  public String getJdbcUrlTemplate(Driver driver) {
    return Optional.ofNullable(DRIVER_CLASS_TO_URL_TEMPLATE.get(driver.getClass().getName())).orElse("");
  }

  @Override
  public String getJdbcUrlExample(Driver driver) {
    return Optional.ofNullable(DRIVER_CLASS_TO_URL_EXAMPLE.get(driver.getClass().getName())).orElse("");
  }

  @Override
  public void addDriver(String filename, InputStream jarFile) throws IOException {
    try(FileOutputStream fos = new FileOutputStream(new File(System.getenv("OPAL_HOME") + "/ext", filename))) {
      ByteStreams.copy(jarFile, fos);
    } finally {
      try {
        jarFile.close();
      } catch(IOException ignored) {
      }
    }
  }
}
