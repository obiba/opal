package org.obiba.opal.core.domain.database;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.validator.Unique;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@Unique(compoundProperties = @Unique.CompoundProperty(name = "url",
    properties = { "sqlSettings.url", "mongoDbSettings.url" }))
public class Database extends AbstractTimestamped implements HasUniqueProperties {

  public enum Usage {
    IMPORT, STORAGE, EXPORT
  }

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  private Usage usage;

  private boolean defaultStorage;

  private boolean usedForIdentifiers;

  @Nullable
  private SqlSettings sqlSettings;

  @Nullable
  private MongoDbSettings mongoDbSettings;

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(name);
  }

  public boolean isDefaultStorage() {
    return defaultStorage;
  }

  public void setDefaultStorage(boolean defaultStorage) {
    this.defaultStorage = defaultStorage;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public Usage getUsage() {
    return usage;
  }

  public void setUsage(@NotNull Usage usage) {
    this.usage = usage;
  }

  public boolean isUsedForIdentifiers() {
    return usedForIdentifiers;
  }

  public void setUsedForIdentifiers(boolean usedForIdentifiers) {
    this.usedForIdentifiers = usedForIdentifiers;
  }

  @Nullable
  public SqlSettings getSqlSettings() {
    return sqlSettings;
  }

  public void setSqlSettings(@Nullable SqlSettings sqlSettings) {
    this.sqlSettings = sqlSettings;
  }

  public boolean hasSqlSettings() {
    return sqlSettings != null;
  }

  @Nullable
  public MongoDbSettings getMongoDbSettings() {
    return mongoDbSettings;
  }

  public void setMongoDbSettings(@Nullable MongoDbSettings mongoDbSettings) {
    this.mongoDbSettings = mongoDbSettings;
  }

  public boolean hasMongoDbSettings() {
    return mongoDbSettings != null;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    //noinspection SimplifiableIfStatement
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equal(name, ((Database) obj).name);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("defaultStorage", defaultStorage).add("name", name).add("usage", usage)
        .add("usedForIdentifiers", usedForIdentifiers).toString();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private Database database;

    private MongoDbSettings.Builder mongoDbSettingsBuilder;

    private SqlSettings.Builder sqlSettingsBuilder;

    private Builder() {
    }

    public static Builder create() {
      Builder builder = new Builder();
      builder.database = new Database();
      return builder;
    }

    public Builder defaultStorage(boolean defaultStorage) {
      database.defaultStorage = defaultStorage;
      return this;
    }

    public Builder name(String name) {
      database.name = name;
      return this;
    }

    public Builder usage(Usage usage) {
      database.usage = usage;
      return this;
    }

    public Builder usedForIdentifiers(boolean usedForIdentifiers) {
      database.usedForIdentifiers = usedForIdentifiers;
      return this;
    }

    public Builder mongoDbSettings(MongoDbSettings mongoDbSettings) {
      database.mongoDbSettings = mongoDbSettings;
      return this;
    }

    public Builder mongoDbSettings(MongoDbSettings.Builder mongoDbSettingsBuilder) {
      this.mongoDbSettingsBuilder = mongoDbSettingsBuilder;
      return this;
    }

    public Builder sqlSettings(SqlSettings sqlSettings) {
      database.sqlSettings = sqlSettings;
      return this;
    }

    public Builder sqlSettings(SqlSettings.Builder sqlSettingsBuilder) {
      this.sqlSettingsBuilder = sqlSettingsBuilder;
      return this;
    }

    public Database build() {
      if(sqlSettingsBuilder != null) database.sqlSettings = sqlSettingsBuilder.build();
      if(mongoDbSettingsBuilder != null) database.mongoDbSettings = mongoDbSettingsBuilder.build();
      return database;
    }
  }

}
