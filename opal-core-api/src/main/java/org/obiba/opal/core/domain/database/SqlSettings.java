package org.obiba.opal.core.domain.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.opal.core.domain.AbstractOrientDbTimestampedEntity;

import com.google.common.base.Objects;

public class SqlSettings {

  public enum SqlSchema {
    HIBERNATE, JDBC, LIMESURVEY
  }

  /**
   * jdbc:{mysql|hsqldb|postgresql}://{hostname}:{port}/{databaseName}
   */
  @Nonnull
  @NotBlank
  private String url;

  @Nonnull
  @NotBlank
  private String driverClass;

  @Nonnull
  @NotBlank
  private String username;

  private String password;

  private String properties;

  @Nonnull
  private SqlSchema sqlSchema;

  @Nullable
  private JdbcDatasourceSettings jdbcDatasourceSettings;

  @Nullable
  private LimesurveyDatasourceSettings limesurveyDatasourceSettings;

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

  @Nonnull
  public SqlSchema getSqlSchema() {
    return sqlSchema;
  }

  public void setSqlSchema(@Nonnull SqlSchema sqlSchema) {
    this.sqlSchema = sqlSchema;
  }

  @Nullable
  public JdbcDatasourceSettings getJdbcDatasourceSettings() {
    return jdbcDatasourceSettings;
  }

  public void setJdbcDatasourceSettings(@Nullable JdbcDatasourceSettings jdbcDatasourceSettings) {
    this.jdbcDatasourceSettings = jdbcDatasourceSettings;
  }

  @Nullable
  public LimesurveyDatasourceSettings getLimesurveyDatasourceSettings() {
    return limesurveyDatasourceSettings;
  }

  public void setLimesurveyDatasourceSettings(@Nullable LimesurveyDatasourceSettings limesurveyDatasourceSettings) {
    this.limesurveyDatasourceSettings = limesurveyDatasourceSettings;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).omitNullValues().add("driverClass", driverClass).add("url", url)
        .add("username", username).add("password", password).add("properties", properties)
        .add("magmaDatasourceType", sqlSchema).toString();
  }

  public static class LimesurveyDatasourceSettings extends AbstractOrientDbTimestampedEntity {

    private String tablePrefix;

    public LimesurveyDatasourceSettings() {
    }

    public LimesurveyDatasourceSettings(String tablePrefix) {
      this.tablePrefix = tablePrefix;
    }

    public String getTablePrefix() {
      return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
      this.tablePrefix = tablePrefix;
    }
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

    public Builder limesurveyDatasourceSettings(LimesurveyDatasourceSettings limesurveyDatasourceSettings) {
      settings.limesurveyDatasourceSettings = limesurveyDatasourceSettings;
      return this;
    }

    public SqlSettings build() {
      return settings;
    }

  }

}
