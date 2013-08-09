package org.obiba.opal.core.domain.database;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "database_mongodb")
public class MongoDbDatabase extends Database {

  private static final long serialVersionUID = 2224211432668278011L;

  /**
   * mongodb://{hostname}:{port}/{databaseName}
   */
  @Nonnull
  @Column(nullable = false, unique = true)
  private String url;

  private String username;

  private String password;

  private String properties;

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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
