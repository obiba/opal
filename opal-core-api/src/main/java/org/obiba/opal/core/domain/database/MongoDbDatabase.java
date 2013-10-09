package org.obiba.opal.core.domain.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.apache.http.client.utils.URIBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.obiba.magma.datasource.mongodb.MongoDBFactory;

import com.google.common.base.Strings;

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
    return new MongoDBFactory(getUri().toString());
  }

  public URI getUri() {
    try {
      URIBuilder uriBuilder = new URIBuilder(url);
      if(!Strings.isNullOrEmpty(username)) {
        if(Strings.isNullOrEmpty(password)) {
          uriBuilder.setUserInfo(username);
        } else {
          uriBuilder.setUserInfo(username, password);
        }
      }
      Properties prop = readProperties();
      for(Map.Entry<Object, Object> entry : prop.entrySet()) {
        uriBuilder.addParameter(entry.getKey().toString(), entry.getValue().toString());
      }
      return uriBuilder.build();
    } catch(URISyntaxException e) {
      throw new RuntimeException("Cannot create MongoDB URI", e);
    }
  }

  public String getMongoDbDatabaseName() {
    try {
      URIBuilder uriBuilder = new URIBuilder(url);
      return uriBuilder.getPath().substring(1);
    } catch(URISyntaxException e) {
      throw new RuntimeException("Cannot parse MongoDB URI", e);
    }
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
