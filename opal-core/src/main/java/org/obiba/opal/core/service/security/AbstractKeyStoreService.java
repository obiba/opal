/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.obiba.crypt.CacheablePasswordCallback;
import org.obiba.crypt.CachingCallbackHandler;
import org.obiba.crypt.KeyProviderSecurityException;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.NoSuchIdentifiersMappingException;
import org.obiba.opal.core.service.OrientDbService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

public abstract class AbstractKeyStoreService {

  @NotNull
  protected CallbackHandler callbackHandler;

  @NotNull
  protected OrientDbService orientDbService;

  @Autowired
  public void setCallbackHandler(@NotNull CallbackHandler callbackHandler) {
    this.callbackHandler = callbackHandler;
  }

  @Autowired
  public void setOrientDbService(@NotNull OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(KeyStoreState.class);
  }

  OpalKeyStore getOrCreateKeyStore(@NotNull String name) {
    OpalKeyStore keyStore = getKeyStore(name);
    if(keyStore == null) {
      keyStore = (OpalKeyStore) OpalKeyStore.Builder.newStore().name(name).passwordPrompt(callbackHandler).build();
      saveKeyStore(keyStore);
    }
    return keyStore;
  }

  void createOrUpdateKey(@NotNull String name, @NotNull String alias, @NotNull String algorithm, int size,
      @NotNull String certificateInfo) {
    hasText(name, "name must not be null or empty");
    hasText(alias, "alias must not be null or empty");
    hasText(algorithm, "algorithm must not be null or empty");
    notNull(size, "size must not be null");
    hasText(certificateInfo, "certificateInfo must not be null or empty");

    OpalKeyStore opalKeyStore = getOrCreateKeyStore(name);
    try {
      if(opalKeyStore.getKeyStore().containsAlias(alias)) {
        opalKeyStore.getKeyStore().deleteEntry(alias);
      }
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
    opalKeyStore.createOrUpdateKey(alias, algorithm, size, certificateInfo);

    saveKeyStore(opalKeyStore);
  }

  boolean aliasExists(@NotNull String name, @NotNull String alias) {
    hasText(alias, "alias must not be null or empty");
    OpalKeyStore opalKeyStore = getKeyStore(name);
    return opalKeyStore != null && opalKeyStore.aliasExists(alias);
  }

  void saveKeyStore(@NotNull OpalKeyStore keyStore) {
    notNull(keyStore, "keyStore must not be null");

    KeyStoreState state;
    String name = keyStore.getName();
    KeyStoreState existing = findByName(name);
    if(existing == null) {
      state = new KeyStoreState();
      state.setName(name);
    } else {
      state = existing;
    }
    state.setKeyStore(getKeyStoreByteArray(keyStore));

    orientDbService.save(state, state);
  }

  void deleteKey(@NotNull String name, @NotNull String alias) {
    hasText(name, "name must not be null or empty");
    hasText(alias, "alias must not be null or empty");

    OpalKeyStore opalKeyStore = getKeyStore(name);
    if(opalKeyStore == null) {
      throw new RuntimeException("The key store [" + name + "] does not exist. Nothing to delete.");
    }
    opalKeyStore.deleteKey(alias);
    saveKeyStore(opalKeyStore);
  }

  void importKey(@NotNull String name, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull FileObject certificate) {
    hasText(name, "name must not be null or empty");
    hasText(alias, "alias must not be null or empty");
    notNull(privateKey, "privateKey must not be null");
    notNull(certificate, "certificate must not be null");

    OpalKeyStore opalKeyStore = getOrCreateKeyStore(name);
    opalKeyStore.importKey(alias, privateKey, certificate);
    saveKeyStore(opalKeyStore);
  }

  void importKey(@NotNull String name, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull InputStream certificate) throws NoSuchIdentifiersMappingException {
    hasText(name, "name must not be null or empty");
    hasText(alias, "alias must not be null or empty");
    notNull(privateKey, "privateKey must not be null");
    notNull(certificate, "certificate must not be null");

    OpalKeyStore opalKeyStore = getOrCreateKeyStore(name);
    opalKeyStore.importKey(alias, privateKey, certificate);

    saveKeyStore(opalKeyStore);
  }

  void importKey(@NotNull String name, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull String certificateInfo) {
    hasText(name, "name must not be null or empty");
    hasText(alias, "alias must not be null or empty");
    notNull(privateKey, "privateKey must not be null");
    hasText(certificateInfo, "certificateInfo must not be null or empty");

    OpalKeyStore opalKeyStore = getOrCreateKeyStore(name);
    opalKeyStore.importKey(alias, privateKey, certificateInfo);

    saveKeyStore(opalKeyStore);
  }

  void importKey(@NotNull String name, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull String certificateInfo) throws NoSuchIdentifiersMappingException {
    hasText(name, "name must not be null or empty");
    hasText(alias, "alias must not be null or empty");
    notNull(privateKey, "privateKey must not be null");
    hasText(certificateInfo, "certificateInfo must not be null or empty");

    OpalKeyStore opalKeyStore = getOrCreateKeyStore(name);
    opalKeyStore.importKey(alias, privateKey, certificateInfo);

    saveKeyStore(opalKeyStore);
  }

  void importCertificate(@NotNull String name, @NotNull String alias, @NotNull InputStream certStream) {
    hasText(name, "name must not be null or empty");
    hasText(alias, "alias must not be null or empty");
    notNull(certStream, "certStream must not be null");

    OpalKeyStore opalKeyStore = getOrCreateKeyStore(name);
    opalKeyStore.importCertificate(alias, certStream);

    saveKeyStore(opalKeyStore);
  }

  @Nullable
  private OpalKeyStore getKeyStore(@NotNull String name) {
    KeyStoreState state = findByName(name);
    return state == null ? null : loadKeyStore(name, state);
  }

  private OpalKeyStore loadKeyStore(@NotNull String name, @NotNull KeyStoreState state) {
    CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(name)
        .prompt(getPasswordFor(name)).build();

    OpalKeyStore opalKeyStore = null;
    try {
      opalKeyStore = new OpalKeyStore(name, loadKeyStore(state.getKeyStore(), passwordCallback));
      opalKeyStore.setCallbackHandler(callbackHandler);
      OpalKeyStore.loadBouncyCastle();
    } catch(GeneralSecurityException | UnsupportedCallbackException ex) {
      throw new RuntimeException(ex);
    } catch(IOException ex) {
      clearPasswordCache(callbackHandler, name);
      translateAndRethrowKeyStoreIOException(ex);
    }
    return opalKeyStore;
  }

  private KeyStore loadKeyStore(byte[] keyStoreBytes, CacheablePasswordCallback passwordCallback)
      throws GeneralSecurityException, UnsupportedCallbackException, IOException {
    KeyStore ks = KeyStore.getInstance("JCEKS");
    ks.load(new ByteArrayInputStream(keyStoreBytes), getKeyPassword(passwordCallback));
    return ks;
  }

  private KeyStoreState findByName(String name) {
    return orientDbService.findUnique(new KeyStoreState(name));
  }

  private byte[] getKeyStoreByteArray(OpalKeyStore opalKeyStore) {
    String name = opalKeyStore.getName();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(name)
          .prompt(getPasswordFor(name)).build();
      opalKeyStore.getKeyStore().store(outputStream, getKeyPassword(passwordCallback));
    } catch(KeyStoreException e) {
      clearPasswordCache(callbackHandler, opalKeyStore.getName());
      throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
    } catch(GeneralSecurityException | UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException ex) {
      clearPasswordCache(callbackHandler, name);
      translateAndRethrowKeyStoreIOException(ex);
    }
    return outputStream.toByteArray();
  }

  private char[] getKeyPassword(CacheablePasswordCallback passwordCallback)
      throws UnsupportedCallbackException, IOException {
    callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
    return passwordCallback.getPassword();
  }

  /**
   * Returns "Password for 'name':  ".
   */
  private String getPasswordFor(String name) {
    return OpalKeyStore.PASSWORD_FOR + " '" + name + "':  ";
  }

  private static void clearPasswordCache(CallbackHandler callbackHandler, String passwordKey) {
    if(callbackHandler instanceof CachingCallbackHandler) {
      ((CachingCallbackHandler) callbackHandler).clearPasswordCache(passwordKey);
    }
  }

  private static void translateAndRethrowKeyStoreIOException(IOException ex) {
    if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
      throw new KeyProviderSecurityException("Wrong keystore password");
    }
    throw new RuntimeException(ex);
  }
}
