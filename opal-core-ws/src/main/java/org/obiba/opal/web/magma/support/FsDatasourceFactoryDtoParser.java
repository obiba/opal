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
import org.obiba.magma.datasource.fs.support.FsDatasourceFactory;
import org.obiba.opal.core.support.OnyxDatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.FsDatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class FsDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    FsDatasourceFactory fsFactory = new FsDatasourceFactory();
    FsDatasourceFactoryDto fsDto = dto.getExtension(FsDatasourceFactoryDto.params);
    fsFactory.setFile(resolveLocalFile(fsDto.getFile()));
    fsFactory.setEncryptionStrategy(encryptionStrategy);

    DatasourceFactory factory = fsDto.getOnyxWrapper() ? new OnyxDatasourceFactory(fsFactory) : fsFactory;

    factory.setName(dto.getName());

    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(FsDatasourceFactoryDto.params);
  }
}
