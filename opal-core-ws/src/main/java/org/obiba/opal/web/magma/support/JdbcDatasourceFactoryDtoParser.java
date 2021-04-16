/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.JdbcDatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

}
