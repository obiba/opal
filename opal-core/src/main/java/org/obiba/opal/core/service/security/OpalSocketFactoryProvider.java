/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import org.obiba.magma.SocketFactoryProvider;
import org.obiba.ssl.AnyTrustManager;
import org.obiba.ssl.X509ExtendedKeyManagerImpl;
import org.obiba.ssl.X509TrustManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OpalSocketFactoryProvider implements SocketFactoryProvider {

  private static final Logger log = LoggerFactory.getLogger(OpalSocketFactoryProvider.class);

  @Value("${org.obiba.opal.security.ssl.allowInvalidCertificates}")
  private boolean allowInvalidCertificates;

  @Autowired
  @Qualifier("systemKeyStoreService")
  private SystemKeyStoreService systemKeyStoreService;

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  private SocketFactory socketFactory;

  @Override
  public SocketFactory getSocketFactory() {
    if (socketFactory != null) return socketFactory;
    try {
      socketFactory = getSSLContext(allowInvalidCertificates).getSocketFactory();
    } catch (Exception e) {
      log.error("Failed building a socket factory based on internal keystore", e);
      socketFactory = SSLSocketFactory.getDefault();
    }
    return socketFactory;
  }

  private SSLContext getSSLContext(boolean allowInvalidCertificates) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
    KeyManager[] keyManagers = getKeyManagers();
    TrustManager[] trustManagers = getTrustManagers(allowInvalidCertificates);
    SSLContext context = SSLContext.getInstance("TLS"); // note: could be restricted to a specific TLS version
    context.init(keyManagers, trustManagers, null);
    return context;
  }

  /**
   * Get a trust manager based on the credentials keystore (known certificates).
   *
   * @param allowInvalidCertificates
   * @return
   * @throws NoSuchAlgorithmException
   * @throws KeyStoreException
   */
  private TrustManager[] getTrustManagers(boolean allowInvalidCertificates) throws NoSuchAlgorithmException, KeyStoreException {
    if (allowInvalidCertificates) {
      return new TrustManager[]{new AnyTrustManager()};
    } else {
      return new TrustManager[] {new X509TrustManagerImpl(credentialsKeyStoreService.getKeyStore(), false, true)};
    }
  }

  /**
   * Get a key manager based on the system keystore.
   *
   * @return
   */
  private KeyManager[] getKeyManagers() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
    KeyManagerFactory kmFact = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmFact.init(null, null);
    List<KeyManager> kms = Arrays.stream(kmFact.getKeyManagers()).collect(Collectors.toList());
    kms.add(new X509ExtendedKeyManagerImpl(systemKeyStoreService.getKeyStore()));
    return kms.toArray(new KeyManager[kms.size()]);
  }


}
