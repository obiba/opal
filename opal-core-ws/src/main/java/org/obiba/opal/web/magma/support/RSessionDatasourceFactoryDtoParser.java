/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.support;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.r.magma.RDatasourceFactory;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 *
 */
@Component
public class RSessionDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private final OpalRSessionManager opalRSessionManager;

  @Autowired
  public RSessionDatasourceFactoryDtoParser(OpalRSessionManager opalRSessionManager) {
    this.opalRSessionManager = opalRSessionManager;
  }

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    RDatasourceFactory factory = new RDatasourceFactory();
    Magma.RSessionDatasourceFactoryDto rDto = dto.getExtension(Magma.RSessionDatasourceFactoryDto.params);
    factory.setName(dto.getName());
    factory.setSymbol(rDto.getSymbol());
    factory.setRSessionId(rDto.getSession());
    factory.setOpalRSessionManager(opalRSessionManager);

    if(rDto.hasCharacterSet()) factory.setCharacterSet(rDto.getCharacterSet());
    if(rDto.hasEntityType()) factory.setEntityType(rDto.getEntityType());
    if(rDto.hasLocale()) factory.setLocale(rDto.getLocale());
    if(rDto.hasIdColumn()) factory.setIdColumn(rDto.getIdColumn());
    if(rDto.hasMultilines()) factory.setMultilines(rDto.getMultilines());

    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(Magma.RSessionDatasourceFactoryDto.params);
  }

}
