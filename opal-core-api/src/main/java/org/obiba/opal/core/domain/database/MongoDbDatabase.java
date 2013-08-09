package org.obiba.opal.core.domain.database;

import java.net.UnknownHostException;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.mongodb.MongoClient;

@SuppressWarnings("ParameterHidesMemberVariable")
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

  public MongoClient createMongoClient() throws UnknownHostException {
    return new MongoClient(url);
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public static class Builder extends Database.Builder<MongoDbDatabase, Builder> {

    @Override
    protected MongoDbDatabase createDatabase() {
      return new MongoDbDatabase();
    }

    @Override
    protected Builder createBuilder() {
      return this;
    }

    public Builder password(String password) {
      database.password = password;
      return this;
    }

    public Builder url(String url) {
      database.url = url;
      return this;
    }

    public Builder username(String username) {
      database.username = username;
      return this;
    }

    public Builder properties(String properties) {
      database.properties = properties;
      return this;
    }

    public MongoDbDatabase build() {
      return database;
    }

  }

}
