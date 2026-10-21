/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.support;

import jakarta.validation.constraints.NotNull;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.core.magma.amber.AmberDatasourceFactory;
import org.obiba.opal.web.model.Magma.AmberDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

@Component
public class AmberDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Value("${org.obiba.opal.security.ssl.allowInvalidCertificates}")
  private boolean allowInvalidCertificates;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    AmberDatasourceFactoryDto aDto = dto.getExtension(AmberDatasourceFactoryDto.params);
    AmberDatasourceFactory factory = new AmberDatasourceFactory(dto.getName(), aDto.getUrl(), aDto.getToken(),
        aDto.getSource(), aDto.getStudy(), aDto.getForm());
    if (aDto.hasState()) factory.setState(aDto.getState());
    if (aDto.hasFrom()) factory.setFrom(aDto.getFrom());
    if (aDto.hasTo()) factory.setTo(aDto.getTo());
    if (aDto.hasEntityType()) factory.setEntityType(aDto.getEntityType());
    if (aDto.hasCampaign()) factory.setCampaign(aDto.getCampaign());
    // Amber is trusted like any other server Opal talks to (see RestDatasourceFactoryDtoParser)
    factory.setAllowInvalidCertificates(allowInvalidCertificates);
    SocketFactory socketFactory = socketFactoryProvider.getSocketFactory();
    if (socketFactory instanceof SSLSocketFactory) {
      factory.setSslSocketFactory((SSLSocketFactory) socketFactory);
    }
    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(AmberDatasourceFactoryDto.params);
  }

}
