/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.datasource.onyx;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import org.springframework.core.io.Resource;

public class OpalKeyStore implements IKeyProvider {
  //
  // Instance Variables
  //

  private KeyStore keyStore;

  private Resource keyStoreResource;

  private char[] keyStorePassword;

  //
  // IKeyProvider Methods
  //

  public void init(String keyProviderArgs) {
    this.keyStorePassword = keyProviderArgs.toCharArray();
  }

  public KeyPair getKeyPair(String alias) {
    if(keyStore == null) {
      loadKeyStore();
    }

    KeyPair keyPair = null;

    try {
      Key key = keyStore.getKey(alias, keyStorePassword);

      if(key instanceof PrivateKey) {
        // Get certificate of public key
        Certificate cert = keyStore.getCertificate(alias);

        // Get public key
        PublicKey publicKey = cert.getPublicKey();

        // Return a key pair
        keyPair = new KeyPair(publicKey, (PrivateKey) key);
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }

    return keyPair;
  }

  public KeyPair getKeyPair(PublicKey publicKey) {
    if(keyStore == null) {
      loadKeyStore();
    }

    Enumeration<String> aliases = null;
    try {
      aliases = keyStore.aliases();
    } catch(KeyStoreException ex) {
      throw new RuntimeException(ex);
    }

    KeyPair keyPair = null;

    while(aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      KeyPair currentKeyPair = getKeyPair(alias);

      if(Arrays.equals(currentKeyPair.getPublic().getEncoded(), publicKey.getEncoded())) {
        keyPair = currentKeyPair;
        break;
      }
    }

    return keyPair;
  }

  //
  // Methods
  //

  public void setKeyStoreResource(Resource keyStoreResource) {
    this.keyStoreResource = keyStoreResource;
  }

  /**
   * Loads the KeyStore from the specified file using the specified password.
   * 
   * @throws RuntimeException
   */
  public void loadKeyStore() {
    InputStream is = null;

    try {
      keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      if(keyStoreResource.exists()) {
        keyStore.load(is = keyStoreResource.getInputStream(), keyStorePassword);
      } else {
        keyStore.load(null, keyStorePassword);
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      if(is != null) try {
        is.close();
      } catch(IOException ex) {
        ; // nothing to do
      }
    }
  }
}
