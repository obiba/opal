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
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class OpalSslContextFactory implements SslContextFactory {

  private static final Logger log = LoggerFactory.getLogger(OpalSslContextFactory.class);

  private final FunctionalUnitService functionalUnitService;

  private final UnitKeyStoreService unitKeystoreService;

  private final String publicUrl;

  @Autowired
  public OpalSslContextFactory(FunctionalUnitService functionalUnitService, UnitKeyStoreService unitKeystoreService, @Value("${org.obiba.opal.public.url}") String publicUrl) {
    this.functionalUnitService = functionalUnitService;
    this.unitKeystoreService = unitKeystoreService;
    this.publicUrl = publicUrl;
  }

  @Override
  public SSLContext createSslContext() {
    UnitKeyStore opalKeystore = prepareServerKeystore();

    List<UnitKeyStore> trustedKeyStores = Lists.newArrayList();
    for(FunctionalUnit unit : functionalUnitService.getFunctionalUnits()) {
      UnitKeyStore unitKeyStore = unit.getKeyStore(false);
      if(unitKeyStore != null) {
        trustedKeyStores.add(unitKeyStore);
      }
    }

    try {
      SSLContext ctx = SSLContext.getInstance("SSLv3");
      ctx.init(new KeyManager[] { new UnitKeyManager(opalKeystore) }, new TrustManager[] { new UnitTrustManager(trustedKeyStores) }, null);
      return ctx;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Prepares the Opal keystore for serving HTTPs requests. This method will create the keystore if it does not exist
   * and generate a self-signed certificate. If the keystore already exists, it is not modified in any way.
   * @return a prepared keystore
   */
  private UnitKeyStore prepareServerKeystore() {
    UnitKeyStore keystore = unitKeystoreService.getUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);
    if(keystore == null) {
      keystore = unitKeystoreService.getOrCreateUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);
      keystore.createOrUpdateKey(UnitKeyManager.HTTPS_ALIAS, "RSA", 2048, generateCertificateInfo());
      unitKeystoreService.saveUnitKeyStore(keystore);
    }
    return keystore;
  }

  private String generateCertificateInfo() {
    URL url;
    try {
      url = new URL(publicUrl);
    } catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
    String hostname = url.getHost();

    return "CN=" + hostname + ", OU=Opal, O=" + hostname + ", L=, ST=, C=";
  }
}
