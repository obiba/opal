/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.SpssDatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class SpssDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Nonnull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    SpssDatasourceFactory factory = new SpssDatasourceFactory();
    SpssDatasourceFactoryDto spssDto = dto.getExtension(SpssDatasourceFactoryDto.params);
    factory.setName(dto.getName());
    factory.setFile(resolveLocalFile(spssDto.getFile()));
    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(SpssDatasourceFactoryDto.params);
  }

}
