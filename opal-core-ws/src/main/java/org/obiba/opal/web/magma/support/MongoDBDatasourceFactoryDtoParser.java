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
import org.obiba.magma.datasource.excel.support.ExcelDatasourceFactory;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.ExcelDatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

/**
 *
 */
@Component
public class MongoDBDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private final String mongoURI;

  @Autowired
  public MongoDBDatasourceFactoryDtoParser(@Value("${org.obiba.opal.mongo.data.uri}") String mongoURI) {
    this.mongoURI = mongoURI;
  }

  @Nonnull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    MongoDBDatasourceFactory factory = new MongoDBDatasourceFactory();
    Magma.MongoDBDatasourceFactoryDto mongoDto = dto.getExtension(Magma.MongoDBDatasourceFactoryDto.params);
    if (mongoDto.hasUri()) {
      factory.setConnectionURI(mongoDto.getUri());
    } else {
      factory.setConnectionURI(mongoURI);
    }
    // TODO get registered host port for ths database
    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(Magma.MongoDBDatasourceFactoryDto.params);
  }

}
