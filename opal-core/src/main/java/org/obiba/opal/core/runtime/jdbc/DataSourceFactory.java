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

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.springframework.stereotype.Component;

@Component
public class DataSourceFactory {

  public DataSource createDataSource(@Nonnull Database database) {
    DataSourceFactoryBean factoryBean = new DataSourceFactoryBean();
    factoryBean.setName(database.getName());

    SqlSettings sqlSettings = database.getSqlSettings();
    if(sqlSettings == null) {
      throw new IllegalArgumentException("Cannot create a JDBC DataSource without SqlSettings");
    }
    factoryBean.setDriverClass(sqlSettings.getDriverClass());
    factoryBean.setUrl(sqlSettings.getUrl());
    factoryBean.setUsername(sqlSettings.getUsername());
    factoryBean.setPassword(sqlSettings.getPassword());
    return factoryBean.getObject();
  }

}
