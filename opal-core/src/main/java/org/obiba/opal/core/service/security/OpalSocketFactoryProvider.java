package org.obiba.opal.core.service.security;

import org.obiba.magma.SocketFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

@Component
public class OpalSocketFactoryProvider implements SocketFactoryProvider {

  private static final Logger log = LoggerFactory.getLogger(OpalSocketFactoryProvider.class);

  @Value("${org.obiba.opal.security.ssl.allowInvalidCertificates}")
  private boolean allowInvalidCertificates;

  @Autowired
  @Qualifier("systemKeyStoreService")
  private SystemKeyStoreService systemKeyStoreService;

  private SocketFactory socketFactory;

  @Override
  public SocketFactory getSocketFactory() {
    if (socketFactory != null) return socketFactory;
    try {
      socketFactory = systemKeyStoreService.getKeyStore().getSSLContext(allowInvalidCertificates).getSocketFactory();
    } catch (Exception e) {
      log.error("Failed building a socket factory based on internal keystore", e);
      socketFactory = SSLSocketFactory.getDefault();
    }
    return socketFactory;
  }

}
