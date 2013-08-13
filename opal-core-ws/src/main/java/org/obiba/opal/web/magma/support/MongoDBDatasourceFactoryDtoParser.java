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

import javax.annotation.Nonnull;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
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
  public MongoDBDatasourceFactoryDtoParser(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  @Nonnull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    MongoDBDatasourceFactory factory = new MongoDBDatasourceFactory();
    Magma.MongoDBDatasourceFactoryDto mongoDto = dto.getExtension(Magma.MongoDBDatasourceFactoryDto.params);
    MongoDbDatabase database = (MongoDbDatabase) databaseRegistry.getDatabase(mongoDto.getDatabase());
    factory.setConnectionURI(database.getUrl());
    // TODO get registered host port for the database
    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(Magma.MongoDBDatasourceFactoryDto.params);
  }

}
