package org.obiba.opal.core.domain.database;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
  private String datasource;

  @Nonnull
  public String getDatasource() {
    return datasource;
  }

  public void setDatasource(@Nonnull String datasource) {
    this.datasource = datasource;
  }

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
}
