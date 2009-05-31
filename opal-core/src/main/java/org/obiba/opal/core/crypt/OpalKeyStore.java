/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.crypt;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import org.springframework.core.io.Resource;

public class OpalKeyStore implements IKeyProvider {
  //
  // Constants
  //

  public static final String KEYSTORE_PASSWORD_ARGKEY = "keystorePassword";

  public static final String KEY_PASSWORD_ARGKEY = "keyPassword";

  //
  // Instance Variables
  //

  private KeyStore keyStore;

  private Resource keyStoreResource;

  private char[] keystorePassword;

  private char[] keyPassword;

  //
  // IKeyProvider Methods
  //

  public void init(Map<String, String> keyProviderArgs) {
    if(!keyProviderArgs.containsKey(KEYSTORE_PASSWORD_ARGKEY)) {
      throw new KeyProviderInitializationException("Key provider argument missing (keystorePassword)");
    }

    if(!keyProviderArgs.containsKey(KEY_PASSWORD_ARGKEY)) {
      throw new KeyProviderInitializationException("Key provider argument missing (keyPassword)");
    }

    String keystorePassword = keyProviderArgs.get(KEYSTORE_PASSWORD_ARGKEY);
    this.keystorePassword = (keystorePassword != null) ? keystorePassword.toCharArray() : null;

    String keyPassword = keyProviderArgs.get(KEY_PASSWORD_ARGKEY);
    this.keyPassword = (keyPassword != null) ? keyPassword.toCharArray() : null;

    loadKeyStore();
  }

  public KeyPair getKeyPair(String alias) {
    if(keyStore == null) {
      throw new IllegalStateException("Null keystore (init method must be called prior to calling getKeyPair method)");
    }

    KeyPair keyPair = null;

    try {
      Key key = keyStore.getKey(alias, keyPassword);

      if(key == null) {
        throw new KeyPairNotFoundException("KeyPair not found for specified alias (" + alias + ")");
      }

      if(key instanceof PrivateKey) {
        // Get certificate of public key
        Certificate cert = keyStore.getCertificate(alias);

        // Get public key
        PublicKey publicKey = cert.getPublicKey();

        // Return a key pair
        keyPair = new KeyPair(publicKey, (PrivateKey) key);
      } else {
        throw new KeyPairNotFoundException("KeyPair not found for specified alias (" + alias + ")");
      }
    } catch(KeyPairNotFoundException ex) {
      throw ex;
    } catch(UnrecoverableKeyException ex) {
      throw new KeyProviderSecurityException("Wrong key password");
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }

    return keyPair;
  }

  public KeyPair getKeyPair(PublicKey publicKey) {
    if(keyStore == null) {
      throw new IllegalStateException("Null keystore (init method must be called prior to calling getKeyPair method)");
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

    if(keyPair == null) {
      throw new KeyPairNotFoundException("KeyPair not found for specified public key");
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
   * @throws KeyProviderInitializationException if the keystore resource could not be found
   * @throws KeyProviderSecurityException if the keystore password is incorrect
   */
  private void loadKeyStore() {
    InputStream is = null;

    try {
      keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      if(keyStoreResource.exists()) {
        keyStore.load(is = keyStoreResource.getInputStream(), keystorePassword);
      } else {
        throw new KeyProviderInitializationException("Keystore [" + keyStoreResource.getFile().getName() + "] not found.");
      }
    } catch(KeyProviderInitializationException ex) {
      throw ex;
    } catch(IOException ex) {
      if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
        throw new KeyProviderSecurityException("Wrong keystore password");
      }
      throw new RuntimeException(ex);
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
