package org.obiba.opal.web.database;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.web.model.Magma;

import com.google.common.base.Strings;

import static org.obiba.opal.web.model.Database.DatabaseDto;
import static org.obiba.opal.web.model.Database.MongoDbSettingsDto;
import static org.obiba.opal.web.model.Database.SqlSettingsDto;

public class Dtos {

  private Dtos() {}

  public static Database fromDto(DatabaseDto dto) {
    Database db = new Database();
    db.setDefaultStorage(dto.getDefaultStorage());
    db.setName(dto.getName());
    db.setUsage(Database.Usage.valueOf(dto.getUsage().name()));
    db.setUsedForIdentifiers(dto.getUsedForIdentifiers());
    if(dto.hasSqlSettings()) db.setSqlSettings(fromDto(dto.getSqlSettings()));
    if(dto.hasMongoDbSettings()) db.setMongoDbSettings(fromDto(dto.getMongoDbSettings()));
    return db;
  }

  @Nullable
  private static SqlSettings fromDto(@NotNull SqlSettingsDto dto) {
    SqlSettings settings = new SqlSettings();
    settings.setDriverClass(dto.getDriverClass());
    settings.setSqlSchema(SqlSettings.SqlSchema.valueOf(dto.getSqlSchema().name()));
    settings.setUrl(dto.getUrl());
    settings.setUsername(dto.getUsername());
    settings.setPassword(dto.getPassword());
    settings.setProperties(dto.getProperties());
    if(dto.hasJdbcDatasourceSettings()) {
      settings.setJdbcDatasourceSettings(fromDto(dto.getJdbcDatasourceSettings()));
    }
    if(dto.hasLimesurveyDatasourceSettings()) {
      settings.setLimesurveyDatasourceSettings(fromDto(dto.getLimesurveyDatasourceSettings()));
    }
    return settings;
  }

  @Nullable
  private static SqlSettings.LimesurveyDatasourceSettings fromDto(
      @NotNull SqlSettingsDto.LimesurveyDatasourceSettingsDto dto) {
    return new SqlSettings.LimesurveyDatasourceSettings(dto.getTablePrefix());
  }

  @Nullable
  private static JdbcDatasourceSettings fromDto(@NotNull Magma.JdbcDatasourceSettingsDto dto) {
    JdbcDatasourceSettings jdbcSettings = new JdbcDatasourceSettings();
    jdbcSettings.setDefaultEntityType(dto.getDefaultEntityType());
    jdbcSettings.setDefaultCreatedTimestampColumnName(dto.getDefaultCreatedTimestampColumnName());
    jdbcSettings.setDefaultUpdatedTimestampColumnName(dto.getDefaultUpdatedTimestampColumnName());
    jdbcSettings.setUseMetadataTables(dto.getUseMetadataTables());
    return jdbcSettings;
  }

  @Nullable
  private static MongoDbSettings fromDto(@NotNull MongoDbSettingsDto dto) {
    MongoDbSettings settings = new MongoDbSettings();
    settings.setUrl(dto.getUrl());
    settings.setUsername(dto.getUsername());
    settings.setPassword(dto.getPassword());
    settings.setProperties(dto.getProperties());

    if (dto.hasBatchSize()) settings.setBatchSize(dto.getBatchSize());

    return settings;
  }

  public static DatabaseDto asDto(Database db, boolean hasDatasource) {
    return asDto(db, hasDatasource, true);
  }

  public static DatabaseDto asDto(Database db, boolean hasDatasource, boolean withSettings) {
    DatabaseDto.Builder builder = DatabaseDto.newBuilder();
    builder.setName(db.getName());
    builder.setDefaultStorage(db.isDefaultStorage());
    builder.setHasDatasource(hasDatasource);
    builder.setUsedForIdentifiers(db.isUsedForIdentifiers());
    builder.setUsage(DatabaseDto.Usage.valueOf(db.getUsage().name()));

    if(withSettings) {
      SqlSettings sqlSettings = db.getSqlSettings();
      if(sqlSettings != null) {
        builder.setSqlSettings(asDto(sqlSettings));
      }
      MongoDbSettings mongoDbSettings = db.getMongoDbSettings();
      if(mongoDbSettings != null) {
        builder.setMongoDbSettings(asDto(mongoDbSettings));
      }
    }
    return builder.build();
  }

  private static SqlSettingsDto.Builder asDto(SqlSettings db) {
    SqlSettingsDto.Builder builder = SqlSettingsDto.newBuilder() //
        .setDriverClass(db.getDriverClass()) //
        .setSqlSchema(SqlSettingsDto.SqlSchema.valueOf(db.getSqlSchema().name())) //
        .setUrl(db.getUrl()) //
        .setUsername(db.getUsername());

    if(!Strings.isNullOrEmpty(db.getProperties())) builder.setProperties(db.getProperties());
    switch(db.getSqlSchema()) {
      case JDBC:
        JdbcDatasourceSettings jdbcSettings = db.getJdbcDatasourceSettings();
        if(jdbcSettings != null) {
          builder.setJdbcDatasourceSettings(asDto(jdbcSettings));
        }
        break;
      case LIMESURVEY:
        SqlSettings.LimesurveyDatasourceSettings limesurveySettings = db.getLimesurveyDatasourceSettings();
        if(limesurveySettings != null) {
          builder.setLimesurveyDatasourceSettings(asDto(limesurveySettings));
        }
        break;
    }
    return builder;
  }

  private static SqlSettingsDto.LimesurveyDatasourceSettingsDto.Builder asDto(
      SqlSettings.LimesurveyDatasourceSettings limesurveySettings) {
    SqlSettingsDto.LimesurveyDatasourceSettingsDto.Builder builder = SqlSettingsDto.LimesurveyDatasourceSettingsDto
        .newBuilder();
    String tablePrefix = limesurveySettings.getTablePrefix();
    if(!Strings.isNullOrEmpty(tablePrefix)) builder.setTablePrefix(tablePrefix);
    return builder;
  }

  private static Magma.JdbcDatasourceSettingsDto.Builder asDto(JdbcDatasourceSettings jdbcSettings) {
    Magma.JdbcDatasourceSettingsDto.Builder builder = Magma.JdbcDatasourceSettingsDto.newBuilder();
    builder.setDefaultEntityType(jdbcSettings.getDefaultEntityType());
    String defaultCreatedTimestampColumnName = jdbcSettings.getDefaultCreatedTimestampColumnName();
    if(!Strings.isNullOrEmpty(defaultCreatedTimestampColumnName)) {
      builder.setDefaultCreatedTimestampColumnName(defaultCreatedTimestampColumnName);
    }
    String defaultUpdatedTimestampColumnName = jdbcSettings.getDefaultUpdatedTimestampColumnName();
    if(!Strings.isNullOrEmpty(defaultUpdatedTimestampColumnName)) {
      builder.setDefaultUpdatedTimestampColumnName(defaultUpdatedTimestampColumnName);
    }
    builder.setUseMetadataTables(jdbcSettings.isUseMetadataTables());
    return builder;
  }

  private static MongoDbSettingsDto.Builder asDto(MongoDbSettings db) {
    MongoDbSettingsDto.Builder builder = MongoDbSettingsDto.newBuilder() //
        .setUrl(db.getUrl());
    if(!Strings.isNullOrEmpty(db.getUsername())) builder.setUsername(db.getUsername());
    if(!Strings.isNullOrEmpty(db.getProperties())) builder.setProperties(db.getProperties());

    builder.setBatchSize(db.getBatchSize());

    return builder;
  }

}
