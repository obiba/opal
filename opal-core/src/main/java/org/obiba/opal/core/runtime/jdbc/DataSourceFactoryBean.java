/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime.jdbc;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {

  private static final int MIN_POOL_SIZE = 3;

  private static final int MAX_POOL_SIZE = 20;

  private static final int MAX_IDLE = 10;

  private String driverClass;

  private String url;

  private String username;

  private String password;

  @Override
  public DataSource getObject() {
    try {
      ComboPooledDataSource dataSource = new ComboPooledDataSource();
      dataSource.setDriverClass(driverClass);
      dataSource.setJdbcUrl(url);
      dataSource.setUser(username);
      dataSource.setPassword(password);
      dataSource.setMinPoolSize(MIN_POOL_SIZE);
      dataSource.setMaxPoolSize(MAX_POOL_SIZE);
      dataSource.setMaxIdleTime(MAX_IDLE);
      dataSource.setAutoCommitOnClose(false);

      if("com.mysql.jdbc.Driver".equals(driverClass)) {
        dataSource.setPreferredTestQuery("select 1");
      } else if("org.hsqldb.jdbcDriver".equals(driverClass)) {
        dataSource.setPreferredTestQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
      } else {
        throw new IllegalArgumentException("Unsupported JDBC driver: " + driverClass);
      }
      //TODO validation query for PostgreSQL

      return dataSource;
    } catch(PropertyVetoException e) {
      throw new RuntimeException("Cannot create JDBC dataSource", e);
    }
  }

  @Override
  public Class<?> getObjectType() {
    return DataSource.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
