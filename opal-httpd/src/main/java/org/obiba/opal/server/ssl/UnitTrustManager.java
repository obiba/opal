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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitTrustManager implements X509TrustManager {

  private static final Logger log = LoggerFactory.getLogger(UnitTrustManager.class);

  private final List<UnitKeyStore> trustedKeystores;

  public UnitTrustManager(List<UnitKeyStore> trustedKeystores) {
    this.trustedKeystores = trustedKeystores;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    log.info("checkClientTrusted(..., {})", authType);
    for(int i = 0; i < chain.length; i++) {
      log.info("chain[{}]={}", i, chain[i]);
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    log.info("checkServerTrusted(..., {})", authType);
    for(int i = 0; i < chain.length; i++) {
      log.info("chain[{}]={}", i, chain[i]);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    log.info("getAcceptedIssuers()");
    return new X509Certificate[0];
  }

}
