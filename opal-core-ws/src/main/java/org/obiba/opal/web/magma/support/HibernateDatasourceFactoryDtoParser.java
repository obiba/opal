/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
import org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory;
import org.obiba.opal.core.runtime.jdbc.DatabaseSessionFactoryProvider;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.HibernateDatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class HibernateDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private final DatabaseRegistry databaseRegistry;

  private final IdentifiersTableService identifiersTableService;

  @Autowired
  public HibernateDatasourceFactoryDtoParser(IdentifiersTableService identifiersTableService,
      DatabaseRegistry databaseRegistry) {
    this.identifiersTableService = identifiersTableService;
    this.databaseRegistry = databaseRegistry;
  }

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    DatabaseSessionFactoryProvider sessionFactoryProvider;
    HibernateDatasourceFactoryDto factoryDto = dto.getExtension(HibernateDatasourceFactoryDto.params);
    if(factoryDto.getKey()) {
      sessionFactoryProvider = new DatabaseSessionFactoryProvider(identifiersTableService.getDatasourceName(),
          databaseRegistry, databaseRegistry.getIdentifiersDatabase().getName());
    } else {
      // fallback to default settings
      String database = factoryDto.hasDatabase() ? factoryDto.getDatabase() : "opal-data";
      sessionFactoryProvider = new DatabaseSessionFactoryProvider(dto.getName(), databaseRegistry, database);
    }
    HibernateDatasourceFactory factory = new HibernateDatasourceFactory();
    factory.setSessionFactoryProvider(sessionFactoryProvider);
    factory.setName(dto.getName());
    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(HibernateDatasourceFactoryDto.params);
  }

}
