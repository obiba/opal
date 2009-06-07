/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.repository.impl;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;
import org.springframework.beans.factory.FactoryBean;

/**
 * 
 */
public class MySqlRepositoryFactory implements FactoryBean {

  private String databaseName;

  private String serverName;

  private int portNumber;

  private String user;

  private String password;

  private Boolean indexed;

  private Integer maxNumberOfTripleTables;

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public void setPortNumber(int portNumber) {
    this.portNumber = portNumber;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setIndexed(Boolean indexed) {
    this.indexed = indexed;
  }

  public void setMaxNumberOfTripleTables(Integer maxNumberOfTripleTables) {
    this.maxNumberOfTripleTables = maxNumberOfTripleTables;
  }

  public Object getObject() throws Exception {
    MySqlStore mysqlStore = new MySqlStore(databaseName);
    mysqlStore.setServerName(serverName);
    mysqlStore.setPortNumber(portNumber);
    mysqlStore.setUser(user);
    mysqlStore.setPassword(password);

    if(indexed != null) {
      mysqlStore.setIndexed(indexed);
    }

    if(maxNumberOfTripleTables != null) {
      mysqlStore.setMaxNumberOfTripleTables(maxNumberOfTripleTables);
    }

    return new SailRepository(mysqlStore);
  }

  public Class getObjectType() {
    return Repository.class;
  }

  public boolean isSingleton() {
    return false;
  }

}
