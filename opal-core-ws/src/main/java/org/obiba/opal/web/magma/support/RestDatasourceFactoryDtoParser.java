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
import org.obiba.opal.rest.client.magma.RestDatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.RestDatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class RestDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Nonnull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    RestDatasourceFactoryDto rDto = dto.getExtension(RestDatasourceFactoryDto.params);
    return new RestDatasourceFactory(dto.getName(), rDto.getUrl(), rDto.getUsername(), rDto.getPassword(),
        rDto.getRemoteDatasource());
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(RestDatasourceFactoryDto.params);
  }

}
