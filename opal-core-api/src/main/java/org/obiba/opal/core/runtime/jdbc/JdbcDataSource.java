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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import com.google.common.base.Strings;

@SuppressWarnings("UnusedDeclaration")
public class JdbcDataSource {

  private String name;

  private String url;

  private String driverClass;

  private String username;

  private String password;

  private String properties;

  private boolean editable = true;

  public JdbcDataSource() {
  }

  public JdbcDataSource(String name, String url, String driverClass, String username, String password,
      String properties) {
    this.name = name;
    this.url = url;
    this.driverClass = driverClass;
    this.username = username;
    this.password = password;
    this.properties = properties;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getProperties() {
    return properties;
  }

  public Properties readProperties() {
    Properties prop = new Properties();
    try {
      if(Strings.isNullOrEmpty(getProperties()) == false) {
        prop.load(new ByteArrayInputStream(getProperties().getBytes()));
      }
    } catch(IOException e) {
      // can't really happen
    }
    return prop;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }

  public boolean isEditable() {
    return editable;
  }

  public JdbcDataSource immutable() {
    editable = false;
    return this;
  }

  public JdbcDataSource mutable() {
    editable = true;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(this == obj) return true;
    if(obj instanceof JdbcDataSource) {
      return ((JdbcDataSource) obj).name.equals(name);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
