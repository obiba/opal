/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd.ssl;

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.X509KeyManager;

import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@code X509KeyManager} on {@code UnitKeyStore}. This implementation will list all available key
 * pairs in the unit keystore and select the first one that matches the requested algorithm.
 */
public class UnitKeyManager implements X509KeyManager {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitSslContextFactory.class);

  private final UnitKeyStore unitKeyStore;

  public UnitKeyManager(final UnitKeyStore unitKeyStore) {
    this.unitKeyStore = unitKeyStore;
  }

  @Override
  public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
    return null;
  }

  @Override
  public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
    log.debug("Requested keyType: '{}'", keyType);
    for(String alias : unitKeyStore.listKeyPairs()) {
      KeyPair pair = unitKeyStore.getKeyPair(alias);
      if(pair.getPrivate().getAlgorithm().equals(keyType)) {
        log.debug("Selecting key '{}'", alias);
        return alias;
      }
    }
    log.debug("No appropriate key pair found.");
    return null;
  }

  @Override
  public X509Certificate[] getCertificateChain(String alias) {
    try {
      Certificate[] certs = this.unitKeyStore.getKeyStore().getCertificateChain(alias);
      // Convert Certificate[] to X509Certificate[]
      return Arrays.copyOf(certs, certs.length, X509Certificate[].class);
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String[] getClientAliases(String keyType, Principal[] issuers) {
    return null;
  }

  @Override
  public PrivateKey getPrivateKey(String alias) {
    try {
      return unitKeyStore.getKeyPair(alias).getPrivate();
    } catch(RuntimeException e) {
      throw e;
    }
  }

  @Override
  public String[] getServerAliases(String keyType, Principal[] issuers) {
    return unitKeyStore.listKeyPairs().toArray(new String[] {});
  }
}
