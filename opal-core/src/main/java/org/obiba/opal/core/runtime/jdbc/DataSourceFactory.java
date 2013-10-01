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

import org.obiba.opal.core.domain.database.SqlDatabase;
import org.springframework.stereotype.Component;

import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;

@Component
public class DataSourceFactory {

  public DataSource createDataSource(@Nonnull SqlDatabase database) {
    AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
    dataSource.setUniqueResourceName(database.getName());
    dataSource.setDriverClassName(database.getDriverClass());
    dataSource.setUrl(database.getUrl());
    dataSource.setUser(database.getUsername());
    dataSource.setPassword(database.getPassword());
    dataSource.setMinPoolSize(3);
    dataSource.setMaxPoolSize(50);

    if("com.mysql.jdbc.Driver".equals(database.getDriverClass())) {
      dataSource.setTestQuery("select 1");
    } else if("org.hsqldb.jdbcDriver".equals(database.getDriverClass())) {
      dataSource.setTestQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    }
    //TODO validation query for PostgreSQL

    return dataSource;
  }

}
