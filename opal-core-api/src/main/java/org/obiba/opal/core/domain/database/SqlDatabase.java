package org.obiba.opal.core.domain.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractOrientDbTimestampedEntity;
import org.obiba.opal.core.validator.Unique;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

@SuppressWarnings("ParameterHidesMemberVariable")
@Unique(properties = "url")
public class SqlDatabase extends Database {

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

  @OneToOne(orphanRemoval = true)
  private JdbcDatasourceSettings jdbcDatasourceSettings;

  @OneToOne(orphanRemoval = true)
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
  public SqlSchema getSqlSchema() {
    return sqlSchema;
  }

  public void setSqlSchema(@Nonnull SqlSchema sqlSchema) {
    this.sqlSchema = sqlSchema;
  }

  public JdbcDatasourceSettings getJdbcDatasourceSettings() {
    return jdbcDatasourceSettings;
  }

  public void setJdbcDatasourceSettings(JdbcDatasourceSettings jdbcDatasourceSettings) {
    this.jdbcDatasourceSettings = jdbcDatasourceSettings;
  }

  public LimesurveyDatasourceSettings getLimesurveyDatasourceSettings() {
    return limesurveyDatasourceSettings;
  }

  public void setLimesurveyDatasourceSettings(LimesurveyDatasourceSettings limesurveyDatasourceSettings) {
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

  public static class JdbcDatasourceSettings extends AbstractOrientDbTimestampedEntity {

    private String defaultEntityType;

    private Set<String> mappedTables;

    @OneToMany(orphanRemoval = true)
    private Set<JdbcValueTableSettings> tableSettings;

    private boolean useMetadataTables;

    private String defaultCreatedTimestampColumnName;

    private String defaultUpdatedTimestampColumnName;

    public String getDefaultCreatedTimestampColumnName() {
      return defaultCreatedTimestampColumnName;
    }

    public void setDefaultCreatedTimestampColumnName(String defaultCreatedTimestampColumnName) {
      this.defaultCreatedTimestampColumnName = defaultCreatedTimestampColumnName;
    }

    public String getDefaultEntityType() {
      return defaultEntityType;
    }

    public void setDefaultEntityType(String defaultEntityType) {
      this.defaultEntityType = defaultEntityType;
    }

    public String getDefaultUpdatedTimestampColumnName() {
      return defaultUpdatedTimestampColumnName;
    }

    public void setDefaultUpdatedTimestampColumnName(String defaultUpdatedTimestampColumnName) {
      this.defaultUpdatedTimestampColumnName = defaultUpdatedTimestampColumnName;
    }

    public Set<String> getMappedTables() {
      return mappedTables;
    }

    public void setMappedTables(Set<String> mappedTables) {
      this.mappedTables = mappedTables;
    }

    public Set<JdbcValueTableSettings> getTableSettings() {
      return tableSettings;
    }

    public void setTableSettings(Set<JdbcValueTableSettings> tableSettings) {
      this.tableSettings = tableSettings;
    }

    public boolean isUseMetadataTables() {
      return useMetadataTables;
    }

    public void setUseMetadataTables(boolean useMetadataTables) {
      this.useMetadataTables = useMetadataTables;
    }

    public static class JdbcValueTableSettings extends AbstractOrientDbTimestampedEntity {

      private String sqlTableName;

      private String magmaTableName;

      private String entityType;

      private List<String> entityIdentifierColumns;

      private String createdTimestampColumnName;

      private String updatedTimestampColumnName;

      public String getCreatedTimestampColumnName() {
        return createdTimestampColumnName;
      }

      public void setCreatedTimestampColumnName(String createdTimestampColumnName) {
        this.createdTimestampColumnName = createdTimestampColumnName;
      }

      public List<String> getEntityIdentifierColumns() {
        return entityIdentifierColumns;
      }

      public void setEntityIdentifierColumns(List<String> entityIdentifierColumns) {
        this.entityIdentifierColumns = entityIdentifierColumns;
      }

      public String getEntityType() {
        return entityType;
      }

      public void setEntityType(String entityType) {
        this.entityType = entityType;
      }

      public String getMagmaTableName() {
        return magmaTableName;
      }

      public void setMagmaTableName(String magmaTableName) {
        this.magmaTableName = magmaTableName;
      }

      public String getSqlTableName() {
        return sqlTableName;
      }

      public void setSqlTableName(String sqlTableName) {
        this.sqlTableName = sqlTableName;
      }

      public String getUpdatedTimestampColumnName() {
        return updatedTimestampColumnName;
      }

      public void setUpdatedTimestampColumnName(String updatedTimestampColumnName) {
        this.updatedTimestampColumnName = updatedTimestampColumnName;
      }

    }
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

    public Builder sqlSchema(SqlSchema sqlSchema) {
      database.sqlSchema = sqlSchema;
      return this;
    }

    public Builder jdbcDatasourceSettings(JdbcDatasourceSettings jdbcDatasourceSettings) {
      database.jdbcDatasourceSettings = jdbcDatasourceSettings;
      return this;
    }

    public Builder limesurveyDatasourceSettings(LimesurveyDatasourceSettings limesurveyDatasourceSettings) {
      database.limesurveyDatasourceSettings = limesurveyDatasourceSettings;
      return this;
    }

    public SqlDatabase build() {
      return database;
    }

  }

}
