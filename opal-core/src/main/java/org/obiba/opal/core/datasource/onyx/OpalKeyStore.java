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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.springframework.core.io.Resource;

public class OpalKeyStore implements IKeyProvider {
  //
  // Instance Variables
  //

  private KeyStore keyStore;

  private Resource keyStoreResource;

  private String keyStorePassword;

  //
  // IKeyProvider Methods
  //

  public KeyPair getKeyPair(String alias) {
    KeyPair keyPair = null;

    try {
      Key key = keyStore.getKey(alias, "password".toCharArray());

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

  //
  // Methods
  //

  public void setKeyStoreResource(Resource keyStoreResource) {
    this.keyStoreResource = keyStoreResource;
  }

  public void setKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
  }

  /**
   * Opens the KeyStore from the specified file using the specified password.
   * 
   * @throws RuntimeException
   */
  public void open() {
    InputStream is = null;

    try {
      keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      if(keyStoreResource.exists()) {
        keyStore.load(is = keyStoreResource.getInputStream(), keyStorePassword.toCharArray());
      } else {
        keyStore.load(null, keyStorePassword.toCharArray());
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      if(is != null) try {
        is.close();
      } catch(IOException e) {
        ; // nothing to do
      }
    }
  }

  public void close() {
    keyStore = null;
  }
}
