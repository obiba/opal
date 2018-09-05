package org.obiba.opal.core.service.security;

import com.google.common.base.Joiner;
import org.obiba.magma.SocketFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

@Component
public class OpalSocketFactoryProvider implements SocketFactoryProvider {

  private static final Logger log = LoggerFactory.getLogger(OpalSocketFactoryProvider.class);

  @Autowired
  @Qualifier("systemKeyStoreService")
  private SystemKeyStoreService systemKeyStoreService;

  private SSLSocketFactory sslSocketFactory;

  @Override
  public SocketFactory getSocketFactory() {
    if (sslSocketFactory != null) return sslSocketFactory;
    try {
      sslSocketFactory = systemKeyStoreService.getKeyStore().getSSLContext().getSocketFactory();
      return sslSocketFactory;
    } catch (Exception e) {
      log.error("Failed building a socket factory based on internal keystore", e);
    }
    return SSLSocketFactory.getDefault();
  }

}
