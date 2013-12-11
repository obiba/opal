/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.magma.datasource.jdbc.JdbcValueTableSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.JdbcDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.JdbcDatasourceSettingsDto;
import org.obiba.opal.web.model.Magma.JdbcValueTableSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

@Component
public class JdbcDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private final DatabaseRegistry databaseRegistry;

  @Autowired
  public JdbcDatasourceFactoryDtoParser(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(JdbcDatasourceFactoryDto.params);
  }

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    JdbcDatasourceFactoryDto jdbcDto = dto.getExtension(JdbcDatasourceFactoryDto.params);
    return new DatabaseJdbcDatasourceFactory(dto.getName(), jdbcDto.getDatabase(), databaseRegistry);
  }

  private JdbcDatasourceSettings parseSettings(JdbcDatasourceSettingsDto dto) {
    JdbcDatasourceSettings settings = new JdbcDatasourceSettings();
    if(dto.hasDefaultEntityType()) {
      settings.setDefaultEntityType(dto.getDefaultEntityType());
    }
    settings.setUseMetadataTables(dto.getUseMetadataTables());
    ImmutableSet<String> mappedTables = new ImmutableSet.Builder<String>().addAll(dto.getMappedTablesList()).build();
    settings.setMappedTables(mappedTables);
    ImmutableSet.Builder<JdbcValueTableSettings> tableSettings = new ImmutableSet.Builder<>();
    for(JdbcValueTableSettingsDto tableDto : dto.getTableSettingsList()) {
      tableSettings.add(parseSettings(tableDto));
    }
    settings.setTableSettings(tableSettings.build());
    if(dto.hasDefaultCreatedTimestampColumnName()) {
      settings.setDefaultCreatedTimestampColumnName(dto.getDefaultCreatedTimestampColumnName());
    }
    if(dto.hasDefaultUpdatedTimestampColumnName()) {
      settings.setDefaultUpdatedTimestampColumnName(dto.getDefaultUpdatedTimestampColumnName());
    }
    return settings;
  }

  private JdbcValueTableSettings parseSettings(JdbcValueTableSettingsDto dto) {
    JdbcValueTableSettings settings = new JdbcValueTableSettings();
    settings.setSqlTableName(dto.getSqlTableName());
    if(dto.hasMagmaTableName()) {
      settings.setMagmaTableName(dto.getMagmaTableName());
    }
    settings.setEntityType(dto.getEntityType());
    settings.setEntityIdentifierColumns(dto.getEntityIdentifierColumnsList());
    if(dto.hasCreatedTimestampColumnName()) {
      settings.setCreatedTimestampColumnName(dto.getCreatedTimestampColumnName());
    }
    if(dto.hasUpdatedTimestampColumnName()) {
      settings.setUpdatedTimestampColumnName(dto.getUpdatedTimestampColumnName());
    }
    return settings;
  }

}
