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
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class MongoDBDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private final DatabaseRegistry databaseRegistry;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @Autowired
  public MongoDBDatasourceFactoryDtoParser(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    Magma.MongoDBDatasourceFactoryDto mongoDto = dto.getExtension(Magma.MongoDBDatasourceFactoryDto.params);
    MongoDbSettings mongoDbSettings = databaseRegistry.getDatabase(mongoDto.getDatabase()).getMongoDbSettings();
    if(mongoDbSettings == null) {
      throw new IllegalArgumentException("Cannot find mongoDbSettings for database " + dto.getName());
    }
    return mongoDbSettings.createMongoDBDatasourceFactory(dto.getName(), socketFactoryProvider);
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(Magma.MongoDBDatasourceFactoryDto.params);
  }

}
