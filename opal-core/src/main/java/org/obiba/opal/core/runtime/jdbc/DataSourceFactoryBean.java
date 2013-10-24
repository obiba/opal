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

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;

import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {

  private static final int MIN_POOL_SIZE = 3;

  private static final int MAX_POOL_SIZE = 20;

  private static final int MAX_IDLE = 10;

  private String name;

  private String driverClass;

  private String url;

  private String username;

  private String password;

  @Override
  public DataSource getObject() {
    AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
    dataSource.setUniqueResourceName(name);
    dataSource.setDriverClassName(driverClass);
    dataSource.setUrl(url);
    dataSource.setUser(username);
    dataSource.setPassword(password);
    dataSource.setMinPoolSize(MIN_POOL_SIZE);
    dataSource.setMaxPoolSize(MAX_POOL_SIZE);
    dataSource.setMaxIdleTime(MAX_IDLE);

    if("com.mysql.jdbc.Driver".equals(driverClass)) {
      dataSource.setTestQuery("select 1");
    } else if("org.hsqldb.jdbcDriver".equals(driverClass)) {
      dataSource.setTestQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    } else {
      throw new IllegalArgumentException("Unsupported JDBC driver: " + driverClass);
    }
    //TODO validation query for PostgreSQL
    return dataSource;
  }

  @Override
  public Class<?> getObjectType() {
    return DataSource.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  public void setName(String name) {
    this.name = name;
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
