/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.ssl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.security.SystemKeyStoreService;
import org.obiba.ssl.SslContextFactory;
import org.obiba.ssl.X509ExtendedKeyManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpalSslContextFactory implements SslContextFactory {

//  private static final Logger log = LoggerFactory.getLogger(OpalSslContextFactory.class);

  @Value("${org.obiba.opal.public.url}")
  private String publicUrl;

  @Autowired
  private SystemKeyStoreService systemKeyStoreService;

  @Autowired
  private CredentialsTrustManager credentialsTrustManager;

  @Override
  public SSLContext createSslContext() {
    OpalKeyStore opalKeystore = prepareServerKeystore();
    try {
      SSLContext ctx = SSLContext.getInstance("TLSv1");
      ctx.init(new KeyManager[] { new X509ExtendedKeyManagerImpl(opalKeystore) },
          new TrustManager[] { credentialsTrustManager }, null);
      return ctx;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Prepares the Opal keystore for serving HTTPs requests. This method will create the keystore if it does not exist
   * and generate a self-signed certificate. If the keystore already exists, it is not modified in any way.
   *
   * @return a prepared keystore
   */
  private OpalKeyStore prepareServerKeystore() {
    OpalKeyStore keystore = systemKeyStoreService.getKeyStore();
    if(!systemKeyStoreService.aliasExists(X509ExtendedKeyManagerImpl.HTTPS_ALIAS)) {
      keystore.createOrUpdateKey(X509ExtendedKeyManagerImpl.HTTPS_ALIAS, "RSA", 2048, generateCertificateInfo());
      systemKeyStoreService.saveKeyStore(keystore);
    }
    return keystore;
  }

  private String generateCertificateInfo() {
    try {
      String hostname = new URL(publicUrl).getHost();
      return "CN=" + hostname + ", OU=Opal, O=" + hostname + ", L=, ST=, C=";
    } catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
