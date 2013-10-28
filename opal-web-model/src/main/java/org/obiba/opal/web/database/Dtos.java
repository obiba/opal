package org.obiba.opal.web.database;

import javax.annotation.Nullable;

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
    db.setEditable(dto.getEditable());
    db.setDefaultStorage(dto.getDefaultStorage());
    db.setDescription(dto.getDescription());
    db.setName(dto.getName());
    db.setUsage(Database.Usage.valueOf(dto.getUsage().name()));
    db.setUsedForIdentifiers(dto.getUsedForIdentifiers());
    db.setSqlSettings(fromDto(dto.getSqlSettings()));
    db.setMongoDbSettings(fromDto(dto.getMongoDbSettings()));
    return db;
  }

  @Nullable
  private static SqlSettings fromDto(@Nullable SqlSettingsDto dto) {
    if(dto == null) return null;

    SqlSettings settings = new SqlSettings();
    settings.setDriverClass(dto.getDriverClass());
    settings.setSqlSchema(SqlSettings.SqlSchema.valueOf(dto.getSqlSchema().name()));
    settings.setUrl(dto.getUrl());
    settings.setUsername(dto.getUsername());
    settings.setPassword(dto.getPassword());
    settings.setProperties(dto.getProperties());
    settings.setJdbcDatasourceSettings(fromDto(dto.getJdbcDatasourceSettings()));
    settings.setLimesurveyDatasourceSettings(fromDto(dto.getLimesurveyDatasourceSettings()));
    return settings;
  }

  @Nullable
  private static SqlSettings.LimesurveyDatasourceSettings fromDto(
      @Nullable SqlSettingsDto.LimesurveyDatasourceSettingsDto dto) {
    return dto == null ? null : new SqlSettings.LimesurveyDatasourceSettings(dto.getTablePrefix());
  }

  @Nullable
  private static JdbcDatasourceSettings fromDto(@Nullable Magma.JdbcDatasourceSettingsDto dto) {
    if(dto == null) return null;
    JdbcDatasourceSettings jdbcSettings = new JdbcDatasourceSettings();
    jdbcSettings.setDefaultEntityType(dto.getDefaultEntityType());
    jdbcSettings.setDefaultCreatedTimestampColumnName(dto.getDefaultCreatedTimestampColumnName());
    jdbcSettings.setDefaultUpdatedTimestampColumnName(dto.getDefaultUpdatedTimestampColumnName());
    jdbcSettings.setUseMetadataTables(dto.getUseMetadataTables());
    return jdbcSettings;
  }

  @Nullable
  private static MongoDbSettings fromDto(@Nullable MongoDbSettingsDto dto) {
    if(dto == null) return null;
    MongoDbSettings settings = new MongoDbSettings();
    settings.setUrl(dto.getUrl());
    settings.setUsername(dto.getUsername());
    settings.setPassword(dto.getPassword());
    settings.setProperties(dto.getProperties());
    return settings;
  }

  public static DatabaseDto asDto(Database db) {
    DatabaseDto.Builder builder = DatabaseDto.newBuilder();
    builder.setName(db.getName());
    if(!Strings.isNullOrEmpty(db.getDescription())) builder.setDescription(db.getDescription());
    builder.setDefaultStorage(db.isDefaultStorage());
    builder.setEditable(db.isEditable());
    builder.setUsedForIdentifiers(db.isUsedForIdentifiers());
    builder.setUsage(DatabaseDto.Usage.valueOf(db.getUsage().name()));

    SqlSettings sqlSettings = db.getSqlSettings();
    if(sqlSettings != null) {
      return builder.setSqlSettings(asDto(sqlSettings)).build();
    }
    MongoDbSettings mongoDbSettings = db.getMongoDbSettings();
    if(mongoDbSettings != null) {
      return builder.setMongoDbSettings(asDto(mongoDbSettings)).build();
    }
    throw new IllegalArgumentException("Unsupported database class " + db.getClass());
  }

  private static SqlSettingsDto.Builder asDto(SqlSettings db) {
    SqlSettingsDto.Builder builder = SqlSettingsDto.newBuilder() //
        .setDriverClass(db.getDriverClass()) //
        .setSqlSchema(SqlSettingsDto.SqlSchema.valueOf(db.getSqlSchema().name())) //
        .setUrl(db.getUrl()) //
        .setUsername(db.getUsername());
    if(!Strings.isNullOrEmpty(db.getPassword())) builder.setPassword(db.getPassword());
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
    if(!Strings.isNullOrEmpty(db.getPassword())) builder.setPassword(db.getPassword());
    if(!Strings.isNullOrEmpty(db.getProperties())) builder.setProperties(db.getProperties());
    return builder;
  }

}
