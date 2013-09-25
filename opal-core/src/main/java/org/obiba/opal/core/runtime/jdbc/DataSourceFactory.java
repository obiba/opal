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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.managed.BasicManagedDataSource;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.stereotype.Component;

import com.arjuna.ats.jdbc.TransactionalDriver;
import com.google.common.base.Strings;

@Component
public class DataSourceFactory {

  public BasicDataSource createDataSource(SqlDatabase database) {
    BasicManagedDataSource dataSource = new BasicManagedDataSource();
    dataSource.setDriverClassName(TransactionalDriver.class.getName());
    dataSource.setXADataSource(database.getDriverClass());

    if(!Strings.isNullOrEmpty(database.getProperties())) {
      BeanWrapperImpl bw = new BeanWrapperImpl(dataSource);
      // Set values, ignoring unknown/invalid entries
      bw.setPropertyValues(new MutablePropertyValues(database.readProperties()), true, true);
    }

    // Set other properties
    dataSource.setUrl(database.getUrl());
    dataSource.setUsername(database.getUsername());
    dataSource.setPassword(database.getPassword());

    if("com.mysql.jdbc.Driver".equals(database.getDriverClass()) ||
        "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource".equals(database.getDriverClass())) {
      dataSource.setValidationQuery("select 1");
    } else if("org.hsqldb.jdbcDriver".equals(database.getDriverClass())) {
      dataSource.setValidationQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    }
    //TODO validation query for PostgreSQL

    if(dataSource.getMaxWait() < 0) {
      // Wait for 10 seconds maximum
      dataSource.setMaxWait(10 * 1000);
    }
    return dataSource;
  }

}
