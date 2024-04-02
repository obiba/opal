/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.database;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.magma.datasource.jdbc.JdbcValueTableSettings;
import org.obiba.magma.datasource.jdbc.JdbcValueTableSettingsFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.web.model.Magma;

import com.google.common.base.Strings;

import java.util.stream.Collectors;

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
    return settings;
  }

  @Nullable
  private static JdbcDatasourceSettings fromDto(@NotNull Magma.JdbcDatasourceSettingsDto dto) {
    JdbcDatasourceSettings.Builder builder = JdbcDatasourceSettings.newSettings(dto.getDefaultEntityType())
        .entityIdentifierColumn(dto.getDefaultEntityIdColumnName())
        .createdTimestampColumn(dto.getDefaultCreatedTimestampColumnName())
        .updatedTimestampColumn(dto.getDefaultUpdatedTimestampColumnName())
        .useMetadataTables(dto.getUseMetadataTables())
        .multipleDatasources(dto.getMultipleDatasources())
        .multilines(dto.getMultilines());

    if (dto.getMappedTablesCount() > 0) builder.mappedTables(Sets.newLinkedHashSet(dto.getMappedTablesList()));
    if (dto.hasBatchSize()) builder.batchSize(dto.getBatchSize());
    if (dto.getTableSettingsCount() > 0) {
      builder.tableSettings(dto.getTableSettingsList().stream().map(settingsDto ->
        fromDto(settingsDto, dto.getDefaultEntityType(), dto.getDefaultEntityIdColumnName())).collect(Collectors.toSet()));
    }
    if (dto.getTableSettingsFactoriesCount() > 0) {
      builder.tableSettingsFactories(dto.getTableSettingsFactoriesList().stream().map(settingsDto ->
          fromDto(settingsDto, dto.getDefaultEntityType(), dto.getDefaultEntityIdColumnName())).collect(Collectors.toSet()));
    }

    return builder.build();
  }

  private static JdbcValueTableSettings fromDto(@NotNull Magma.JdbcValueTableSettingsDto dto, String defaultEntityType, String defaultEntityIdColumn ) {
    JdbcValueTableSettings.Builder builder = JdbcValueTableSettings.newSettings(dto.getSqlTable()) //
        .tableName(dto.hasOpalTable() ? dto.getOpalTable() : dto.getSqlTable()) //
        .entityType(dto.hasEntityType() ? dto.getEntityType() : defaultEntityType) //
        .entityIdentifierColumn(dto.hasEntityIdentifierColumn() ? dto.getEntityIdentifierColumn() : defaultEntityIdColumn)
        .multilines(dto.getMultilines());
    if (dto.hasCreatedTimestampColumn()) builder.createdTimestampColumn(dto.getCreatedTimestampColumn());
    if (dto.hasUpdatedTimestampColumn()) builder.updatedTimestampColumn(dto.getUpdatedTimestampColumn());
    if (dto.hasEntityIdentifiersWhere()) builder.entityIdentifiersWhere(dto.getEntityIdentifiersWhere());
    if (dto.hasExcludedColumns()) builder.excludedColumns(dto.getExcludedColumns());
    if (dto.hasIncludedColumns()) builder.includedColumns(dto.getIncludedColumns());
    return builder.build();
  }

  private static JdbcValueTableSettingsFactory fromDto(@NotNull Magma.JdbcValueTableSettingsFactoryDto dto, String defaultEntityType, String defaultEntityIdColumn ) {
    JdbcValueTableSettingsFactory.Builder builder = JdbcValueTableSettingsFactory.newSettings(dto.getSqlTable(), dto.getTablePartitionColumn()) //
        .tableName(dto.getOpalTable()) //
        .entityType(dto.hasEntityType() ? dto.getEntityType() : defaultEntityType) //
        .entityIdentifierColumn(dto.hasEntityIdentifierColumn() ? dto.getEntityIdentifierColumn() : defaultEntityIdColumn)
        .multilines(dto.getMultilines());
    if (dto.hasCreatedTimestampColumn()) builder.createdTimestampColumn(dto.getCreatedTimestampColumn());
    if (dto.hasUpdatedTimestampColumn()) builder.updatedTimestampColumn(dto.getUpdatedTimestampColumn());
    if (dto.hasEntityIdentifiersWhere()) builder.entityIdentifiersWhere(dto.getEntityIdentifiersWhere());
    if (dto.hasExcludedColumns()) builder.excludedColumns(dto.getExcludedColumns());
    if (dto.hasIncludedColumns()) builder.includedColumns(dto.getIncludedColumns());
    return builder.build();
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

    if (db.getSqlSchema() == SqlSettings.SqlSchema.JDBC) {
      JdbcDatasourceSettings jdbcSettings = db.getJdbcDatasourceSettings();
      if(jdbcSettings != null) {
        builder.setJdbcDatasourceSettings(asDto(jdbcSettings));
      }
    }
    return builder;
  }

  private static Magma.JdbcDatasourceSettingsDto.Builder asDto(JdbcDatasourceSettings jdbcSettings) {
    Magma.JdbcDatasourceSettingsDto.Builder builder = Magma.JdbcDatasourceSettingsDto.newBuilder();
    builder.setDefaultEntityType(jdbcSettings.getDefaultEntityType());
    if(jdbcSettings.hasEntityIdColumnName()) {
      builder.setDefaultEntityIdColumnName(jdbcSettings.getDefaultEntityIdColumnName());
    }
    if(jdbcSettings.hasCreatedTimestampColumnName()) {
      builder.setDefaultCreatedTimestampColumnName(jdbcSettings.getDefaultCreatedTimestampColumnName());
    }
    if(jdbcSettings.hasUpdatedTimestampColumnName()) {
      builder.setDefaultUpdatedTimestampColumnName(jdbcSettings.getDefaultUpdatedTimestampColumnName());
    }
    builder.setUseMetadataTables(jdbcSettings.isUseMetadataTables());
    if(jdbcSettings.isMultipleDatasources()) {
      builder.setMultipleDatasources(jdbcSettings.isMultipleDatasources());
    }

    builder.setBatchSize(jdbcSettings.getBatchSize());

    if(jdbcSettings.hasMappedTables()) jdbcSettings.getMappedTables().forEach(t -> builder.addMappedTables(t));

    jdbcSettings.getTableSettings().forEach(settings -> builder.addTableSettings(asDto(settings)));
    jdbcSettings.getTableSettingsFactories().forEach(settings -> builder.addTableSettingsFactories(asDto(settings)));

    return builder;
  }

  private static Magma.JdbcValueTableSettingsDto.Builder asDto(JdbcValueTableSettings jdbcSettings) {
    Magma.JdbcValueTableSettingsDto.Builder builder = Magma.JdbcValueTableSettingsDto.newBuilder();
    builder.setSqlTable(jdbcSettings.getSqlTableName());
    builder.setEntityType(jdbcSettings.getEntityType());
    builder.setOpalTable(jdbcSettings.getMagmaTableName());
    builder.setEntityIdentifierColumn(jdbcSettings.getEntityIdentifierColumn());
    if(jdbcSettings.hasCreatedTimestampColumnName()) builder.setCreatedTimestampColumn(jdbcSettings.getCreatedTimestampColumnName());
    if(jdbcSettings.hasUpdatedTimestampColumnName()) builder.setUpdatedTimestampColumn(jdbcSettings.getUpdatedTimestampColumnName());
    if(jdbcSettings.hasEntityIdentifiersWhere()) builder.setEntityIdentifiersWhere(jdbcSettings.getEntityIdentifiersWhere());
    if(jdbcSettings.hasExcludedColumns()) builder.setExcludedColumns(jdbcSettings.getExcludedColumns());
    if(jdbcSettings.hasIncludedColumns()) builder.setIncludedColumns(jdbcSettings.getIncludedColumns());
    builder.setMultilines(jdbcSettings.isMultilines());
    return builder;
  }

  private static Magma.JdbcValueTableSettingsFactoryDto.Builder asDto(JdbcValueTableSettingsFactory factory) {
    Magma.JdbcValueTableSettingsFactoryDto.Builder builder = Magma.JdbcValueTableSettingsFactoryDto.newBuilder();
    builder.setSqlTable(factory.getSqlTableName()) //
        .setTablePartitionColumn(factory.getTablePartitionColumn()) //
        .setEntityType(factory.getEntityType()) //
        .setEntityIdentifierColumn(factory.getEntityIdentifierColumn());
    if(factory.hasMagmaTableName()) builder.setOpalTable(factory.getMagmaTableName());
    if(factory.hasCreatedTimestampColumnName()) builder.setCreatedTimestampColumn(factory.getCreatedTimestampColumnName());
    if(factory.hasUpdatedTimestampColumnName()) builder.setUpdatedTimestampColumn(factory.getUpdatedTimestampColumnName());
    if(factory.hasExcludedColumns()) builder.setExcludedColumns(factory.getExcludedColumns());
    if(factory.hasIncludedColumns()) builder.setIncludedColumns(factory.getIncludedColumns());
    builder.setMultilines(factory.isMultilines());
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
