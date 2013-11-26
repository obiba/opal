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

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {

  private static final Logger log = LoggerFactory.getLogger(DataSourceFactoryBean.class);

  private static final int MIN_POOL_SIZE = 3;

  private static final int MAX_POOL_SIZE = 30;

  private static final int MAX_IDLE = 10;

  private String driverClass;

  private String url;

  private String username;

  private String password;

  @Override
  public DataSource getObject() {
    log.debug("Configure DataSource for {}", url);
    PoolConfiguration config = new PoolProperties();
    config.setDriverClassName(driverClass);
    config.setUrl(url);
    config.setUsername(username);
    config.setPassword(password);
//    config.setInitialSize(MIN_POOL_SIZE);
//    config.setMaxActive(MAX_POOL_SIZE);
    config.setMaxIdle(MAX_IDLE);
    config.setTestOnBorrow(true);
    config.setTestWhileIdle(false);
    config.setTestOnReturn(false);
    config.setDefaultAutoCommit(false);
    config.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
        "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;" +
        "org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport");

    if("com.mysql.jdbc.Driver".equals(driverClass)) {
      config.setValidationQuery("select 1");
    } else if("org.hsqldb.jdbcDriver".equals(driverClass)) {
      config.setValidationQuery("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    } else {
      //TODO validation query for PostgreSQL
      throw new IllegalArgumentException("Unsupported JDBC driver: " + driverClass);
    }
    return new org.apache.tomcat.jdbc.pool.DataSource(config);
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
