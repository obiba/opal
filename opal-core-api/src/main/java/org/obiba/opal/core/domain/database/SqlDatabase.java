package org.obiba.opal.core.domain.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

@SuppressWarnings("ParameterHidesMemberVariable")
public class SqlDatabase extends Database {

  private static final long serialVersionUID = 2224211432668278011L;

  public static final String MAGMA_HIBERNATE_DATASOURCE = "hibernate";

  public static final String MAGMA_JDBC_DATASOURCE = "jdbc";

  public static final String MAGMA_LIMESURVEY_DATASOURCE = "limesurvey";

  /**
   * jdbc:{mysql|mariadb|postgresql}://{hostname}:{port}/{databaseName}
   */
  @Nonnull
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

  public boolean isHibernateDatasourceType() {
    return MAGMA_HIBERNATE_DATASOURCE.equals(magmaDatasourceType);
  }

  public boolean isJdbcDatasourceType() {
    return MAGMA_JDBC_DATASOURCE.equals(magmaDatasourceType);
  }

  public boolean isLimesurveyDatasourceType() {
    return MAGMA_LIMESURVEY_DATASOURCE.equals(magmaDatasourceType);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).omitNullValues().add("driverClass", driverClass).add("url", url)
        .add("username", username).add("password", password).add("properties", properties)
        .add("magmaDatasourceType", magmaDatasourceType).toString();
  }

  public static class Builder extends Database.Builder<SqlDatabase, Builder> {

    @Override
    protected SqlDatabase createDatabase() {
      return new SqlDatabase();
    }

    @Override
    protected Builder createBuilder() {
      return this;
    }

    public Builder driverClass(String driverClass) {
      database.driverClass = driverClass;
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

    public Builder password(String password) {
      database.password = password;
      return this;
    }

    public Builder properties(String properties) {
      database.properties = properties;
      return this;
    }

    public Builder magmaDatasourceType(String magmaDatasourceType) {
      database.magmaDatasourceType = magmaDatasourceType;
      return this;
    }

    public SqlDatabase build() {
      return database;
    }

  }

}
