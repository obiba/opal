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

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

import org.obiba.opal.core.unit.UnitKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@code X509KeyManager} on {@code UnitKeyStore}. This implementation will list all available key
 * pairs in the unit keystore and select the first one that matches the requested algorithm.
 */
public class UnitKeyManager extends X509ExtendedKeyManager {

  private static final Logger log = LoggerFactory.getLogger(UnitKeyManager.class);

  private final UnitKeyStore unitKeyStore;

  public UnitKeyManager(final UnitKeyStore unitKeyStore) {
    if(unitKeyStore == null) throw new IllegalArgumentException("unitKeyStore cannot be null");
    this.unitKeyStore = unitKeyStore;
  }

  @Override
  public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
    log.info("chooseClientAlias({}, {}, socket)", keyTypes, issuers);
    for(String keyType : keyTypes) {
      String alias = chooseServerAlias(keyType, issuers, socket);
      if(alias != null) {
        return alias;
      }
    }
    return null;
  }

  @Override
  public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
    log.info("Requested keyType: '{}'", keyType);
    for(String alias : unitKeyStore.listKeyPairs()) {
      KeyPair pair = unitKeyStore.getKeyPair(alias);
      if(pair.getPrivate().getAlgorithm().equals(keyType)) {
        log.info("Selecting key '{}'", alias);
        return alias;
      }
    }
    log.info("No appropriate key pair found.");
    return null;
  }

  @Override
  public X509Certificate[] getCertificateChain(String alias) {
    log.info("getCertificateChain({})", alias);
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
    log.info("getClientAliases({}, {})", keyType, issuers);
    return null;
  }

  @Override
  public PrivateKey getPrivateKey(String alias) {
    log.info("getPrivateKey({})", alias);
    try {
      return unitKeyStore.getKeyPair(alias).getPrivate();
    } catch(RuntimeException e) {
      throw e;
    }
  }

  @Override
  public String[] getServerAliases(String keyType, Principal[] issuers) {
    log.info("getServerAliases({}, {})", keyType, issuers);
    return unitKeyStore.listKeyPairs().toArray(new String[] {});
  }

  @Override
  public String chooseEngineClientAlias(String[] keyTypes, Principal[] issuers, SSLEngine engine) {
    log.info("chooseEngineClientAlias({}, {})", keyTypes, issuers);
    return chooseClientAlias(keyTypes, issuers, null);
  }

  @Override
  public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
    log.info("chooseEngineServerAlias({}, {})", keyType, Arrays.toString(issuers));
    return chooseServerAlias(keyType, issuers, null);
  }
}
