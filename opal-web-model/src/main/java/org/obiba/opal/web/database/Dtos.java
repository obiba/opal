package org.obiba.opal.web.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.web.model.Magma;

import com.google.common.base.Strings;

import static org.obiba.opal.web.model.Database.DatabaseDto;
import static org.obiba.opal.web.model.Database.MongoDbDatabaseDto;
import static org.obiba.opal.web.model.Database.SqlDatabaseDto;

public class Dtos {

  private Dtos() {}

  public static void fromDto(@Nonnull DatabaseDto dto, @Nonnull Database database) {
    SqlDatabaseDto sqlDto = dto.getExtension(SqlDatabaseDto.settings);
    MongoDbDatabaseDto mongoDto = dto.getExtension(MongoDbDatabaseDto.settings);
    if(sqlDto != null) {
      fromDto(dto, sqlDto, (SqlDatabase) database);
    } else if(mongoDto != null) {
      fromDto(dto, mongoDto, (MongoDbDatabase) database);
    } else {
      throw new IllegalArgumentException("Unsupported DatabaseDto extension");
    }
  }

  public static Database fromDto(DatabaseDto dto) {
    SqlDatabaseDto sqlDto = dto.getExtension(SqlDatabaseDto.settings);
    if(sqlDto != null) return fromDto(dto, sqlDto);
    MongoDbDatabaseDto mongoDto = dto.getExtension(MongoDbDatabaseDto.settings);
    if(mongoDto != null) return fromDto(dto, mongoDto);
    throw new IllegalArgumentException("Unsupported DatabaseDto extension");
  }

  private static void fromDto(Database db, DatabaseDto dto) {
    db.setEditable(dto.getEditable());
    db.setDefaultStorage(dto.getDefaultStorage());
    db.setDescription(dto.getDescription());
    db.setName(dto.getName());
    db.setUsage(Database.Usage.valueOf(dto.getUsage().name()));
    db.setUsedForIdentifiers(dto.getUsedForIdentifiers());
  }

  private static void fromDto(DatabaseDto dto, SqlDatabaseDto sqlDto, SqlDatabase db) {
    fromDto(db, dto);
    db.setDriverClass(sqlDto.getDriverClass());
    db.setSqlSchema(SqlDatabase.SqlSchema.valueOf(sqlDto.getSqlSchema().name()));
    db.setUrl(sqlDto.getUrl());
    db.setUsername(sqlDto.getUsername());
    db.setPassword(sqlDto.getPassword());
    db.setProperties(sqlDto.getProperties());
    switch(db.getSqlSchema()) {
      case JDBC:
        db.setJdbcDatasourceSettings(getJdbcDatasourceSettings(sqlDto.getJdbcDatasourceSettings()));
        break;
      case LIMESURVEY:
        db.setLimesurveyDatasourceSettings(getLimesurveyDatasourceSettings(sqlDto.getLimesurveyDatasourceSettings()));
        break;
    }
  }

  @Nullable
  private static SqlDatabase.LimesurveyDatasourceSettings getLimesurveyDatasourceSettings(
      @Nullable SqlDatabaseDto.LimesurveyDatasourceSettingsDto limesurveySettingsDto) {
    if(limesurveySettingsDto == null) return null;
    SqlDatabase.LimesurveyDatasourceSettings limesurveySettings = new SqlDatabase.LimesurveyDatasourceSettings();
    limesurveySettings.setTablePrefix(limesurveySettingsDto.getTablePrefix());
    return limesurveySettings;
  }

  @Nullable
  private static SqlDatabase.JdbcDatasourceSettings getJdbcDatasourceSettings(
      @Nullable Magma.JdbcDatasourceSettingsDto jdbcSettingsDto) {
    if(jdbcSettingsDto == null) return null;
    SqlDatabase.JdbcDatasourceSettings jdbcSettings = new SqlDatabase.JdbcDatasourceSettings();
    jdbcSettings.setDefaultEntityType(jdbcSettingsDto.getDefaultEntityType());
    jdbcSettings.setDefaultCreatedTimestampColumnName(jdbcSettingsDto.getDefaultCreatedTimestampColumnName());
    jdbcSettings.setDefaultUpdatedTimestampColumnName(jdbcSettingsDto.getDefaultUpdatedTimestampColumnName());
    jdbcSettings.setUseMetadataTables(jdbcSettingsDto.getUseMetadataTables());
    return jdbcSettings;
  }

  private static SqlDatabase fromDto(DatabaseDto dto, SqlDatabaseDto sqlDto) {
    SqlDatabase db = new SqlDatabase();
    fromDto(dto, sqlDto, db);
    return db;
  }

  private static void fromDto(DatabaseDto dto, MongoDbDatabaseDto mongoDto, MongoDbDatabase db) {
    fromDto(db, dto);
    db.setUrl(mongoDto.getUrl());
    db.setUsername(mongoDto.getUsername());
    db.setPassword(mongoDto.getPassword());
    db.setProperties(mongoDto.getProperties());
  }

  private static MongoDbDatabase fromDto(DatabaseDto dto, MongoDbDatabaseDto mongoDto) {
    MongoDbDatabase db = new MongoDbDatabase();
    fromDto(dto, mongoDto, db);
    return db;
  }

  public static DatabaseDto asDto(Database db) {
    DatabaseDto.Builder builder = DatabaseDto.newBuilder();
    builder.setName(db.getName());
    if(!Strings.isNullOrEmpty(db.getDescription())) builder.setDescription(db.getDescription());
    builder.setDefaultStorage(db.isDefaultStorage());
    builder.setEditable(db.isEditable());
    builder.setUsedForIdentifiers(db.isUsedForIdentifiers());
    builder.setUsage(DatabaseDto.Usage.valueOf(db.getUsage().name()));
    if(db instanceof SqlDatabase) {
      return builder.setExtension(SqlDatabaseDto.settings, asDto((SqlDatabase) db)).build();
    }
    if(db instanceof MongoDbDatabase) {
      return builder.setExtension(MongoDbDatabaseDto.settings, asDto((MongoDbDatabase) db)).build();
    }
    throw new IllegalArgumentException("Unsupported database class " + db.getClass());
  }

  private static SqlDatabaseDto asDto(SqlDatabase db) {
    SqlDatabaseDto.Builder builder = SqlDatabaseDto.newBuilder() //
        .setDriverClass(db.getDriverClass()) //
        .setSqlSchema(SqlDatabaseDto.SqlSchema.valueOf(db.getSqlSchema().name())) //
        .setUrl(db.getUrl()) //
        .setUsername(db.getUsername());
    if(!Strings.isNullOrEmpty(db.getPassword())) builder.setPassword(db.getPassword());
    if(!Strings.isNullOrEmpty(db.getProperties())) builder.setProperties(db.getProperties());
    switch(db.getSqlSchema()) {
      case JDBC:
        setJdbcDatasourceSettings(builder, db.getJdbcDatasourceSettings());
        break;
      case LIMESURVEY:
        setLimesurveyDatasourceSettings(builder, db.getLimesurveyDatasourceSettings());
        break;
    }
    return builder.build();
  }

  private static void setJdbcDatasourceSettings(SqlDatabaseDto.Builder builder,
      @Nullable SqlDatabase.JdbcDatasourceSettings jdbcSettings) {
    if(jdbcSettings == null) return;
    Magma.JdbcDatasourceSettingsDto.Builder jdbcSettingsBuilder = Magma.JdbcDatasourceSettingsDto.newBuilder();
    jdbcSettingsBuilder.setDefaultEntityType(jdbcSettings.getDefaultEntityType());
    String defaultCreatedTimestampColumnName = jdbcSettings.getDefaultCreatedTimestampColumnName();
    if(!Strings.isNullOrEmpty(defaultCreatedTimestampColumnName)) {
      jdbcSettingsBuilder.setDefaultCreatedTimestampColumnName(defaultCreatedTimestampColumnName);
    }
    String defaultUpdatedTimestampColumnName = jdbcSettings.getDefaultUpdatedTimestampColumnName();
    if(!Strings.isNullOrEmpty(defaultUpdatedTimestampColumnName)) {
      jdbcSettingsBuilder.setDefaultUpdatedTimestampColumnName(defaultUpdatedTimestampColumnName);
    }
    jdbcSettingsBuilder.setUseMetadataTables(jdbcSettings.isUseMetadataTables());
    builder.setJdbcDatasourceSettings(jdbcSettingsBuilder);
  }

  private static void setLimesurveyDatasourceSettings(SqlDatabaseDto.Builder builder,
      @Nullable SqlDatabase.LimesurveyDatasourceSettings limesurveySettings) {
    if(limesurveySettings == null) return;
    SqlDatabaseDto.LimesurveyDatasourceSettingsDto.Builder limesurveySettingsBuilder = SqlDatabaseDto
        .LimesurveyDatasourceSettingsDto.newBuilder();
    String tablePrefix = limesurveySettings.getTablePrefix();
    if(!Strings.isNullOrEmpty(tablePrefix)) limesurveySettingsBuilder.setTablePrefix(tablePrefix);
    builder.setLimesurveyDatasourceSettings(limesurveySettingsBuilder);
  }

  private static MongoDbDatabaseDto asDto(MongoDbDatabase db) {
    MongoDbDatabaseDto.Builder builder = MongoDbDatabaseDto.newBuilder() //
        .setUrl(db.getUrl());
    if(!Strings.isNullOrEmpty(db.getUsername())) builder.setUsername(db.getUsername());
    if(!Strings.isNullOrEmpty(db.getPassword())) builder.setPassword(db.getPassword());
    if(!Strings.isNullOrEmpty(db.getProperties())) builder.setProperties(db.getProperties());
    return builder.build();
  }

}
