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

import jakarta.validation.constraints.NotNull;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.rest.client.magma.RestDatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.RestDatasourceFactoryDto;
import org.springframework.stereotype.Component;
import org.obiba.magma.SocketFactoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 */
@Component
public class RestDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Value("${org.obiba.opal.security.ssl.allowInvalidCertificates}")
  private boolean allowInvalidCertificates;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    RestDatasourceFactoryDto rDto = dto.getExtension(RestDatasourceFactoryDto.params);
    RestDatasourceFactory factory = rDto.hasToken()
        ? new RestDatasourceFactory(dto.getName(), rDto.getUrl(), rDto.getToken(), rDto.getRemoteDatasource())
        : new RestDatasourceFactory(dto.getName(), rDto.getUrl(), rDto.getUsername(), rDto.getPassword(), rDto.getRemoteDatasource());
    // the remote Opal is trusted like any other server Opal talks to: JVM trust store, then the credentials key store,
    // unless invalid certificates were explicitly allowed
    factory.setAllowInvalidCertificates(allowInvalidCertificates);
    SocketFactory socketFactory = socketFactoryProvider.getSocketFactory();
    if (socketFactory instanceof SSLSocketFactory) {
      factory.setSslSocketFactory((SSLSocketFactory) socketFactory);
    }
    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(RestDatasourceFactoryDto.params);
  }

}
