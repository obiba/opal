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

import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class OpalSslContextFactory implements SslContextFactory {

  private static final Logger log = LoggerFactory.getLogger(OpalSslContextFactory.class);

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private UnitKeyStoreService keystoreService;

  @Override
  public SSLContext createSslContext() {
    UnitKeyStore serverKeystore = keystoreService.getUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);

    List<UnitKeyStore> trustedKeyStores = Lists.newArrayList();
    for(FunctionalUnit unit : opalRuntime.getFunctionalUnits()) {
      UnitKeyStore unitKeyStore = unit.getKeyStore(false);
      if(unitKeyStore != null) {
        trustedKeyStores.add(unitKeyStore);
      }
    }

    try {
      SSLContext ctx = SSLContext.getInstance("SSLv3");
      ctx.init(new KeyManager[] { new UnitKeyManager(serverKeystore) }, new TrustManager[] { new UnitTrustManager(trustedKeyStores) }, null);
      return ctx;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

}
