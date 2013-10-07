package org.obiba.opal.web.database;

import javax.annotation.Nonnull;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.web.model.Opal;

import com.google.common.base.Strings;

public class Dtos {

  private Dtos() {}

  public static void fromDto(@Nonnull Opal.DatabaseDto dto, @Nonnull Database database) {
    Opal.SqlDatabaseDto sqlDto = dto.getExtension(Opal.SqlDatabaseDto.settings);
    Opal.MongoDbDatabaseDto mongoDto = dto.getExtension(Opal.MongoDbDatabaseDto.settings);
    if(sqlDto != null) {
      fromDto(dto, sqlDto, (SqlDatabase) database);
    } else if(mongoDto != null) {
      fromDto(dto, mongoDto, (MongoDbDatabase) database);
    } else {
      throw new IllegalArgumentException("Unsupported DatabaseDto extension");
    }
  }

  public static Database fromDto(Opal.DatabaseDto dto) {
    Opal.SqlDatabaseDto sqlDto = dto.getExtension(Opal.SqlDatabaseDto.settings);
    if(sqlDto != null) return fromDto(dto, sqlDto);
    Opal.MongoDbDatabaseDto mongoDto = dto.getExtension(Opal.MongoDbDatabaseDto.settings);
    if(mongoDto != null) return fromDto(dto, mongoDto);
    throw new IllegalArgumentException("Unsupported DatabaseDto extension");
  }

  private static void fromDto(Database db, Opal.DatabaseDto dto) {
    db.setEditable(dto.getEditable());
    db.setDefaultStorage(dto.getDefaultStorage());
    db.setDescription(dto.getDescription());
    db.setName(dto.getName());
    db.setUsage(Database.Usage.valueOf(dto.getUsage().name()));
    db.setUsedForIdentifiers(dto.getUsedForIdentifiers());
  }

  private static void fromDto(Opal.DatabaseDto dto, Opal.SqlDatabaseDto sqlDto, SqlDatabase db) {
    fromDto(db, dto);
    db.setDriverClass(sqlDto.getDriverClass());
    db.setSqlSchema(SqlDatabase.SqlSchema.valueOf(sqlDto.getSqlSchema().name()));
    db.setUrl(sqlDto.getUrl());
    db.setUsername(sqlDto.getUsername());
    db.setPassword(sqlDto.getPassword());
    db.setProperties(sqlDto.getProperties());
  }

  private static SqlDatabase fromDto(Opal.DatabaseDto dto, Opal.SqlDatabaseDto sqlDto) {
    SqlDatabase db = new SqlDatabase();
    fromDto(dto, sqlDto, db);
    return db;
  }

  private static void fromDto(Opal.DatabaseDto dto, Opal.MongoDbDatabaseDto mongoDto, MongoDbDatabase db) {
    fromDto(db, dto);
    db.setUrl(mongoDto.getUrl());
    db.setUsername(mongoDto.getUsername());
    db.setPassword(mongoDto.getPassword());
    db.setProperties(mongoDto.getProperties());
  }

  private static MongoDbDatabase fromDto(Opal.DatabaseDto dto, Opal.MongoDbDatabaseDto mongoDto) {
    MongoDbDatabase db = new MongoDbDatabase();
    fromDto(dto, mongoDto, db);
    return db;
  }

  public static Opal.DatabaseDto asDto(Database db) {
    Opal.DatabaseDto.Builder builder = Opal.DatabaseDto.newBuilder();
    builder.setName(db.getName());
    if(!Strings.isNullOrEmpty(db.getDescription())) builder.setDescription(db.getDescription());
    builder.setDefaultStorage(db.isDefaultStorage());
    builder.setEditable(db.isEditable());
    builder.setUsedForIdentifiers(db.isUsedForIdentifiers());
    builder.setUsage(Opal.DatabaseDto.Usage.valueOf(db.getUsage().name()));
    if(db instanceof SqlDatabase) {
      return builder.setExtension(Opal.SqlDatabaseDto.settings, asDto((SqlDatabase) db)).build();
    }
    if(db instanceof MongoDbDatabase) {
      return builder.setExtension(Opal.MongoDbDatabaseDto.settings, asDto((MongoDbDatabase) db)).build();
    }
    throw new IllegalArgumentException("Unsupported database class " + db.getClass());
  }

  private static Opal.SqlDatabaseDto asDto(SqlDatabase db) {
    Opal.SqlDatabaseDto.Builder builder = Opal.SqlDatabaseDto.newBuilder() //
        .setDriverClass(db.getDriverClass()) //
        .setSqlSchema(Opal.SqlDatabaseDto.SqlSchema.valueOf(db.getSqlSchema().name())) //
        .setUrl(db.getUrl()) //
        .setUsername(db.getUsername());
    if(!Strings.isNullOrEmpty(db.getPassword())) builder.setPassword(db.getPassword());
    if(!Strings.isNullOrEmpty(db.getProperties())) builder.setProperties(db.getProperties());
    return builder.build();
  }

  private static Opal.MongoDbDatabaseDto asDto(MongoDbDatabase db) {
    Opal.MongoDbDatabaseDto.Builder builder = Opal.MongoDbDatabaseDto.newBuilder() //
        .setUrl(db.getUrl());
    if(!Strings.isNullOrEmpty(db.getUsername())) builder.setUsername(db.getUsername());
    if(!Strings.isNullOrEmpty(db.getPassword())) builder.setPassword(db.getPassword());
    if(!Strings.isNullOrEmpty(db.getProperties())) builder.setProperties(db.getProperties());
    return builder.build();
  }

}
