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

import com.google.common.base.MoreObjects;
import jakarta.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;

public class SqlSettings {

  public enum SqlSchema {
    HIBERNATE, JDBC
  }

  /**
   * jdbc:{mysql|postgresql}://{hostname}:{port}/{databaseName}
   */
  @NotNull
  @NotBlank
  private String url;

  @NotNull
  @NotBlank
  private String driverClass;

  @NotNull
  @NotBlank
  private String username;

  private String password;

  private String properties;

  @NotNull
  private SqlSchema sqlSchema;

  @Nullable
  private JdbcDatasourceSettings jdbcDatasourceSettings;

  @NotNull
  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(@NotNull String driverClass) {
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

  @NotNull
  public String getUrl() {
    return url;
  }

  public void setUrl(@NotNull String url) {
    this.url = url;
  }

  @NotNull
  public String getUsername() {
    return username;
  }

  public void setUsername(@NotNull String username) {
    this.username = username;
  }

  @NotNull
  public SqlSchema getSqlSchema() {
    return sqlSchema;
  }

  public void setSqlSchema(@NotNull SqlSchema sqlSchema) {
    this.sqlSchema = sqlSchema;
  }

  @Nullable
  public JdbcDatasourceSettings getJdbcDatasourceSettings() {
    return jdbcDatasourceSettings;
  }

  public void setJdbcDatasourceSettings(@Nullable JdbcDatasourceSettings jdbcDatasourceSettings) {
    this.jdbcDatasourceSettings = jdbcDatasourceSettings;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues().add("driverClass", driverClass).add("url", url)
        .add("username", username).add("password", password).add("properties", properties)
        .add("magmaDatasourceType", sqlSchema).toString();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private SqlSettings settings;

    private Builder() {
    }

    public static Builder create() {
      Builder builder = new Builder();
      builder.settings = new SqlSettings();
      return builder;
    }

    public Builder driverClass(String driverClass) {
      settings.driverClass = driverClass;
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

    public Builder password(String password) {
      settings.password = password;
      return this;
    }

    public Builder properties(String properties) {
      settings.properties = properties;
      return this;
    }

    public Builder sqlSchema(SqlSchema sqlSchema) {
      settings.sqlSchema = sqlSchema;
      return this;
    }

    public Builder jdbcDatasourceSettings(JdbcDatasourceSettings jdbcDatasourceSettings) {
      settings.jdbcDatasourceSettings = jdbcDatasourceSettings;
      return this;
    }

    public SqlSettings build() {
      return settings;
    }

  }

}
