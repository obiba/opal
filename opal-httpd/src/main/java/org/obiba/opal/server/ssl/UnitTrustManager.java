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

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class UnitTrustManager implements X509TrustManager {

  private static final Logger log = LoggerFactory.getLogger(UnitTrustManager.class);

  @Autowired
  private FunctionalUnitService functionalUnitService;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Autowired
  private ProjectService projectService;

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    if(log.isDebugEnabled()) {
      log.debug("checkClientTrusted(..., {})", authType);
      for(int i = 0; i < chain.length; i++) {
        log.debug("chain[{}]={}", i, chain[i]);
      }
    }
    for(OpalKeyStore keyStore : getUnitKeyStores()) {
      for(Certificate cert : keyStore.getCertificates().values()) {
        for(X509Certificate x509Cert : chain) {
          try {
            x509Cert.verify(cert.getPublicKey());
            // If verify succeeds, it doesn't throw an Exception
            return;
          } catch(GeneralSecurityException e) {
            // Ignore
          }
        }
      }
    }
    throw new CertificateException("no trusted certificates");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    log.debug("checkServerTrusted(..., {})", authType);
    for(int i = 0; i < chain.length; i++) {
      log.debug("chain[{}]={}", i, chain[i]);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    log.debug("getAcceptedIssuers()");
    return new X509Certificate[0];
  }

  private Iterable<OpalKeyStore> getUnitKeyStores() {
    List<OpalKeyStore> trustedKeyStores = Lists.newArrayList();
    for(FunctionalUnit unit : functionalUnitService.getFunctionalUnits()) {
      trustedKeyStores.add(projectsKeyStoreService.getKeyStore(projectService.getProject(unit.getName())));
    }
    return trustedKeyStores;
  }
}
