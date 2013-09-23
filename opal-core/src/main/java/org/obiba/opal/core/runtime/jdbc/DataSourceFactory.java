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

import java.util.Properties;

import org.obiba.opal.core.domain.database.SqlDatabase;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import bitronix.tm.resource.jdbc.PoolingDataSource;

@Component
public class DataSourceFactory {

  public PoolingDataSource createDataSource(SqlDatabase database) {
    PoolingDataSource dataSource = new PoolingDataSource();
    dataSource.setClassName(database.getDriverClass());

    if(!Strings.isNullOrEmpty(database.getProperties())) {
      dataSource.setDriverProperties(new Properties(database.readProperties()));
    }

    dataSource.getDriverProperties().setProperty("URL", database.getUrl());
    dataSource.getDriverProperties().setProperty("user", database.getUsername());
    dataSource.getDriverProperties().setProperty("password", database.getPassword());

    if("com.mysql.jdbc.Driver".equals(database.getDriverClass())) {
      dataSource.setTestQuery("select 1");
    } else if("org.hsqldb.jdbcDriver".equals(database.getDriverClass())) {
      dataSource.setTestQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    }
    //TODO validation query for PostgreSQL

    //TODO maxWait
//    if(dataSource.getMaxWait() < 0) {
//      dataSource.setMaxWait(10 * 1000); // Wait for 10 seconds maximum
//    }
    return dataSource;
  }

}
