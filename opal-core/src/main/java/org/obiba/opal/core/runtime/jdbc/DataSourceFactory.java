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

import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceFactory {

  @Autowired
  private ApplicationContext applicationContext;

  @Value("${org.obiba.opal.jdbc.maxPoolSize}")
  private Integer maxPoolSize;

  public DataSourceFactory() {}

  public DataSource createDataSource(@NotNull Database database) {
    DataSourceFactoryBean factoryBean = applicationContext.getAutowireCapableBeanFactory()
        .createBean(DataSourceFactoryBean.class);

    SqlSettings sqlSettings = database.getSqlSettings();

    if(sqlSettings == null) {
      throw new IllegalArgumentException("Cannot create a JDBC DataSource without SqlSettings");
    }

    factoryBean.setDriverClass(sqlSettings.getDriverClass());
    factoryBean.setUrl(sqlSettings.getUrl());
    factoryBean.setUsername(sqlSettings.getUsername());
    factoryBean.setPassword(sqlSettings.getPassword());
    factoryBean.setConnectionProperties(sqlSettings.getProperties());
    factoryBean.setMaxPoolSize(maxPoolSize);

    return factoryBean.getObject();
  }

}
