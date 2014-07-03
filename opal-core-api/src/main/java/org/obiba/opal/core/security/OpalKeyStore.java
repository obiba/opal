/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.crypt.CacheablePasswordCallback;
import org.obiba.crypt.CachingCallbackHandler;
import org.obiba.crypt.KeyProviderSecurityException;
import org.obiba.magma.Datasource;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.security.ObibaKeyStore;
import org.springframework.util.Assert;


public class OpalKeyStore extends ObibaKeyStore implements KeyProvider {

  public OpalKeyStore(String name, KeyStore store) {
    super(name, store);
  }

  public X509Certificate importCertificate(String alias, FileObject certFile) {
    try {
      return importCertificate(alias, certFile.getContent().getInputStream());
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Import a private key and it's associated certificate into the keystore at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   */
  public void importKey(String alias, FileObject privateKey, FileObject certificate) {
    try {
      InputStream privateIn = privateKey.getContent().getInputStream();
      InputStream certificateIn = certificate.getContent().getInputStream();
      importKey(alias, privateIn, certificateIn);
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Import a private key into the keystore and generate an associated certificate at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  public void importKey(String alias, FileObject privateKey, String certificateInfo) {
    try {
      importKey(alias, privateKey.getContent().getInputStream(), certificateInfo);
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException {
    try {
      Certificate cert = getKeyStore().getCertificate(datasource.getName());
      if(cert == null) {
        throw new NoSuchKeyException(datasource.getName(),
            "No PublicKey for Datasource '" + datasource.getName() + "'");
      }
      return cert.getPublicKey();
    } catch(KeyStoreException e) {
      throw new MagmaCryptRuntimeException(e);
    }
  }

  @SuppressWarnings({ "StaticMethodOnlyUsedInOneClass", "ParameterHidesMemberVariable" })
  public static class Builder {

    private String name;

    private CallbackHandler callbackHandler;

    public static Builder newStore() {
      return new Builder();
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder passwordPrompt(CallbackHandler callbackHandler) {
      this.callbackHandler = callbackHandler;
      return this;
    }

    private char[] getKeyPassword(CacheablePasswordCallback passwordCallback)
        throws UnsupportedCallbackException, IOException {
      callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
      return passwordCallback.getPassword();
    }

    public OpalKeyStore build() {
      Assert.hasText(name, "name must not be null or empty");
      Assert.notNull(callbackHandler, "callbackHandler must not be null");

      loadBouncyCastle();

      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(name)
          .prompt("Enter '" + name + "' keystore password:  ")
          .confirmation("Re-enter '" + name + "' keystore password:  ").build();

      KeyStore keyStore = createEmptyKeyStore(passwordCallback);

      return createKeyStore(keyStore);
    }

    private KeyStore createEmptyKeyStore(CacheablePasswordCallback passwordCallback) {
      KeyStore keyStore = null;
      try {
        keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, getKeyPassword(passwordCallback));
      } catch(KeyStoreException e) {
        clearPasswordCache(callbackHandler, name);
        throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
      } catch(GeneralSecurityException | UnsupportedCallbackException e) {
        throw new RuntimeException(e);
      } catch(IOException ex) {
        clearPasswordCache(callbackHandler, name);
        translateAndRethrowKeyStoreIOException(ex);
      }
      return keyStore;
    }

    private static void clearPasswordCache(CallbackHandler callbackHandler, String alias) {
      if(callbackHandler instanceof CachingCallbackHandler) {
        ((CachingCallbackHandler) callbackHandler).clearPasswordCache(alias);
      }
    }

    private static void translateAndRethrowKeyStoreIOException(IOException ex) {
      if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
        throw new KeyProviderSecurityException("Wrong keystore password");
      }
      throw new RuntimeException(ex);
    }

    private OpalKeyStore createKeyStore(KeyStore keyStore) {
      OpalKeyStore opalKeyStore = new OpalKeyStore(name, keyStore);
      opalKeyStore.setCallbackHandler(callbackHandler);
      return opalKeyStore;
    }
  }

}
