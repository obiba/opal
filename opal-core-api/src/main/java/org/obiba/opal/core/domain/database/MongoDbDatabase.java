package org.obiba.opal.core.domain.database;

import javax.annotation.Nonnull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.magma.datasource.mongodb.MongoDBFactory;

@SuppressWarnings("ParameterHidesMemberVariable")
public class MongoDbDatabase extends Database {

  /**
   * mongodb://{hostname}:{port}/{databaseName}
   */
  @Nonnull
  @NotBlank
  private String url;

  private String username;

  private String password;

  private String properties;

  public MongoDBFactory createMongoDBFactory() {
    return new MongoDBFactory(url);
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
