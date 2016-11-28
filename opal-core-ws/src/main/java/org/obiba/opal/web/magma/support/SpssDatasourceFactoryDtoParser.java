/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.SpssDatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class SpssDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    SpssDatasourceFactory factory = new SpssDatasourceFactory();
    SpssDatasourceFactoryDto spssDto = dto.getExtension(SpssDatasourceFactoryDto.params);
    factory.setName(dto.getName());
    factory.setFile(resolveLocalFile(spssDto.getFile()));

    if(spssDto.hasCharacterSet()) {
      factory.setCharacterSet(spssDto.getCharacterSet());
    }

    if(spssDto.hasEntityType()) {
      factory.setEntityType(spssDto.getEntityType());
    }

    if(spssDto.hasLocale()) {
      factory.setLocale(spssDto.getLocale());
    }

    if(spssDto.hasIdVariable()) {
      factory.setIdVariable(spssDto.getIdVariable());
    }

    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(SpssDatasourceFactoryDto.params);
  }

}
