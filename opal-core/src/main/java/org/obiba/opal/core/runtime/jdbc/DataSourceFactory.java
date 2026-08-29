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

import jakarta.validation.constraints.NotNull;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;

@Component
public class DataSourceFactory {

  @Autowired
  private ApplicationContext applicationContext;

  @Value("${org.obiba.opal.jdbc.maxPoolSize}")
  private Integer maxPoolSize;

  @Value("${OPAL_HOME}/data/h2")
  private File h2Root;

  public DataSourceFactory() {}

  public DataSource createDataSource(@NotNull Database database) {
    DataSourceFactoryBean factoryBean = applicationContext.getAutowireCapableBeanFactory()
        .createBean(DataSourceFactoryBean.class);

    SqlSettings sqlSettings = database.getSqlSettings();

    if(sqlSettings == null) {
      throw new IllegalArgumentException("Cannot create a JDBC DataSource without SqlSettings");
    }

    String driverClass = sqlSettings.getDriverClass();
    factoryBean.setDriverClass(driverClass);
    if(H2DatabaseUrls.isH2(driverClass)) {
      // H2 databases are registered by name only, the file lives in the Opal H2 folder
      factoryBean.setUrl(H2DatabaseUrls.expand(sqlSettings.getUrl(), h2Root));
      // the settings are rejected when the database is registered, check them again on the way to the driver
      H2DatabaseUrls.validateProperties(sqlSettings.getProperties());
    } else {
      factoryBean.setUrl(sqlSettings.getUrl());
    }
    factoryBean.setUsername(sqlSettings.getUsername());
    factoryBean.setPassword(sqlSettings.getPassword());
    factoryBean.setConnectionProperties(sqlSettings.getProperties());
    factoryBean.setMaxPoolSize(maxPoolSize);

    return factoryBean.getObject();
  }

}
