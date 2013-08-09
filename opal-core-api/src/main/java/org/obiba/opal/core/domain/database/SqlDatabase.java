package org.obiba.opal.core.domain.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.common.base.Strings;

@Entity
@Table(name = "database_sql")
public class SqlDatabase extends Database {

  private static final long serialVersionUID = 2224211432668278011L;

  /**
   * jdbc:{mysql|mariadb|postgresql}://{hostname}:{port}/{databaseName}
   */
  @Nonnull
  @Column(nullable = false, unique = true)
  private String url;

  @Nonnull
  private String driverClass;

  @Nonnull
  private String username;

  private String password;

  private String properties;

  /**
   * datasource name that can be built on this database: hibernate, jdbc or limesurvey
   */
  @Nonnull
  private String magmaDatasourceType;

  @Nonnull
  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(@Nonnull String driverClass) {
    this.driverClass = driverClass;
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

  public void setProperties(String properties) {
    this.properties = properties;
  }

  public Properties readProperties() {
    Properties prop = new Properties();
    try {
      if(!Strings.isNullOrEmpty(properties)) {
        prop.load(new ByteArrayInputStream(properties.getBytes()));
      }
    } catch(IOException e) {
      // can't really happen
    }
    return prop;
  }

  @Nonnull
  public String getUrl() {
    return url;
  }

  public void setUrl(@Nonnull String url) {
    this.url = url;
  }

  @Nonnull
  public String getUsername() {
    return username;
  }

  public void setUsername(@Nonnull String username) {
    this.username = username;
  }

  @Nonnull
  public String getMagmaDatasourceType() {
    return magmaDatasourceType;
  }

  public void setMagmaDatasourceType(@Nonnull String magmaDatasourceType) {
    this.magmaDatasourceType = magmaDatasourceType;
  }
}
