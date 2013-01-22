/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.support;

import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.IncrementalDatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class IncrementalDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    IncrementalDatasourceFactoryDto factoryDto = dto.getExtension(IncrementalDatasourceFactoryDto.params);
    DatasourceFactoryDto wrappedDatasourceFactoryDto = factoryDto.getDatasourceFactoryDto();
    // TODO
    return null;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(IncrementalDatasourceFactoryDto.params);
  }

}
