/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.database;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.mongodb.MongoDBDatasource;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;

public class MongoDbSettings {

  /**
   * mongodb://{hostname}:{port}/{databaseName}
   */
  @NotNull
  @NotBlank
  private String url;

  private String username;

  private String password;

  private String properties;

  private int batchSize = 100;

  public MongoDBDatasourceFactory createMongoDBDatasourceFactory(String datasourceName) {
    return createMongoDBDatasourceFactory(datasourceName, null);
  }

  public MongoDBDatasourceFactory createMongoDBDatasourceFactory(String datasourceName, SocketFactoryProvider socketFactoryProvider) {
    MongoDBDatasourceFactory factory = new MongoDBDatasourceFactory(datasourceName, url, username, password,
        properties, socketFactoryProvider);
    factory.setBatchSize(batchSize);

    return factory;
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

  @NotNull
  public String getUrl() {
    return url;
  }

  public void setUrl(@NotNull String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    if (batchSize < 1 || batchSize > MongoDBDatasource.MAX_BATCH_SIZE) throw new IllegalArgumentException("batchSize");
    this.batchSize = batchSize;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private MongoDbSettings settings;

    private Builder() {
    }

    public static Builder create() {
      Builder builder = new Builder();
      builder.settings = new MongoDbSettings();
      return builder;
    }

    public Builder password(String password) {
      settings.password = password;
      return this;
    }

    public Builder url(String url) {
      settings.url = url;
      return this;
    }

    public Builder username(String username) {
      settings.username = username;
      return this;
    }

    public Builder properties(String properties) {
      settings.properties = properties;
      return this;
    }

    public Builder batchSize(int batchSize) {
      settings.batchSize = batchSize;
      return this;
    }

    public MongoDbSettings build() {
      return settings;
    }

  }

}
