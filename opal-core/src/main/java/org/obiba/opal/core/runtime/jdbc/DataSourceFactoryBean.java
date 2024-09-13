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

import com.google.common.base.Strings;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {

  private static final Logger log = LoggerFactory.getLogger(DataSourceFactoryBean.class);

  private static final int MIN_POOL_SIZE = 3;

  private static final int MAX_POOL_SIZE = 100;

  private static final int MAX_IDLE = 10;

  protected String driverClass;

  protected String url;

  protected String username;

  protected String password;

  protected int maxPoolSize = MAX_POOL_SIZE;

  protected String connectionProperties;

  @Override
  public DataSource getObject() {
    log.debug("Configure DataSource for {}", url);
    BasicDataSource dataSource = new BasicDataSource();

    dataSource.setDriverClassName(driverClass);
    dataSource.setUrl(url);
    setConnectionProperties(dataSource);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setInitialSize(MIN_POOL_SIZE);
    dataSource.setMaxTotal(maxPoolSize);
    dataSource.setMaxIdle(MAX_IDLE);
    dataSource.setTestOnBorrow(true);
    dataSource.setTestWhileIdle(false);
    dataSource.setTestOnReturn(false);
    dataSource.setDefaultAutoCommit(false);
    dataSource.setValidationQuery(guessValidationQuery());

    return dataSource;
  }

  private String guessValidationQuery() {
    switch(driverClass) {
      case "oracle.jdbc.driver.OracleDriver":
        return "select 1 from dual";
      case "com.ibm.db2.jcc.DB2Driver":
        return "select 1 from sysibm.sysdummy1";
      default:
        return "select 1";
    }
  }

  private void setConnectionProperties(BasicDataSource dataSource) {
    if("com.mysql.jdbc.Driver".equals(driverClass) || "org.mariadb.jdbc.Driver".equals(driverClass)) {
      if(Strings.isNullOrEmpty(connectionProperties)) {
        connectionProperties = "characterEncoding=UTF-8";
      } else {
        connectionProperties += ";characterEncoding=UTF-8";
      }
    }
    if(!Strings.isNullOrEmpty(connectionProperties)) {
      dataSource.setConnectionProperties(connectionProperties);
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

  public void setMaxPoolSize(Integer maxPoolSize) {
    if (maxPoolSize != null) this.maxPoolSize = maxPoolSize;
  }

  public void setConnectionProperties(String connectionProperties) {
    this.connectionProperties = connectionProperties;
  }

  protected String getUrl() {
    return url;
  }

}
