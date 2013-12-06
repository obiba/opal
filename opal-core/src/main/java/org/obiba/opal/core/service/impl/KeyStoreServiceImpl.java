/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

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
import org.obiba.opal.core.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.crypt.CachingCallbackHandler;
import org.obiba.opal.core.crypt.KeyProviderSecurityException;
import org.obiba.opal.core.domain.unit.KeyStoreState;
import org.obiba.opal.core.service.KeyStoreService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.unit.OpalKeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
@Transactional
public class KeyStoreServiceImpl implements KeyStoreService {

  @NotNull
  private CallbackHandler callbackHandler;

  @NotNull
  private OrientDbService orientDbService;

  @Autowired
  public void setCallbackHandler(@NotNull CallbackHandler callbackHandler) {
    this.callbackHandler = callbackHandler;
  }

  @Autowired
  public void setOrientDbService(@NotNull OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(KeyStoreState.class);
  }

  @Override
  public void stop() {
  }

  @Nullable
  @Override
  public OpalKeyStore getUnitKeyStore(@NotNull String name) {
    Assert.hasText(name, "unitName must not be null or empty");
    KeyStoreState state = findByUnit(name);
    return state == null ? null : loadUnitKeyStore(name, state);
  }

  @Nullable
  @Override
  public OpalKeyStore getKeyStore(@NotNull String unitName, boolean create) {
    return create ? getOrCreateUnitKeyStore(unitName) : getUnitKeyStore(unitName);
  }

  @Nullable
  @Override
  public OpalKeyStore getKeyStore(@NotNull String unitName) {
    return getKeyStore(unitName, true);
  }

  private KeyStoreState findByUnit(String unitName) {
    return orientDbService.findUnique(new KeyStoreState(unitName));
  }

  @Override
  public OpalKeyStore getOrCreateUnitKeyStore(@NotNull String unitName) {
    Assert.hasText(unitName, "unitName must not be null or empty");

    OpalKeyStore opalKeyStore = getUnitKeyStore(unitName);
    if(opalKeyStore == null) {
      opalKeyStore = OpalKeyStore.Builder.newStore().unit(unitName).passwordPrompt(callbackHandler).build();
      saveUnitKeyStore(opalKeyStore);
    }
    return opalKeyStore;
  }

  @Override
  public void saveUnitKeyStore(@NotNull OpalKeyStore opalKeyStore) {
    Assert.notNull(opalKeyStore, "opalKeyStore must not be null");

    KeyStoreState state;
    String unitName = opalKeyStore.getUnitName();
    KeyStoreState existing = findByUnit(unitName);
    if(existing == null) {
      state = new KeyStoreState();
      state.setName(unitName);
    } else {
      state = existing;
    }
    state.setKeyStore(getKeyStoreByteArray(opalKeyStore));

    orientDbService.save(state, state);
  }

  @Override
  public void createOrUpdateKey(@NotNull String unitName, @NotNull String alias, @NotNull String algorithm, int size,
      @NotNull String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.hasText(algorithm, "algorithm must not be null or empty");
    Assert.notNull(size, "size must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    OpalKeyStore opalKeyStore = getOrCreateUnitKeyStore(unitName);
    try {
      if(opalKeyStore.getKeyStore().containsAlias(alias)) {
        opalKeyStore.getKeyStore().deleteEntry(alias);
      }
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
    opalKeyStore.createOrUpdateKey(alias, algorithm, size, certificateInfo);

    saveUnitKeyStore(opalKeyStore);
  }

  @Override
  public boolean aliasExists(@NotNull String unitName, @NotNull String alias) {
    Assert.hasText(alias, "alias must not be null or empty");
    OpalKeyStore opalKeyStore = getUnitKeyStore(unitName);
    return opalKeyStore != null && opalKeyStore.aliasExists(alias);
  }

  @Override
  public void deleteKey(@NotNull String unitName, @NotNull String alias) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");

    OpalKeyStore opalKeyStore = getUnitKeyStore(unitName);
    if(opalKeyStore == null) {
      throw new RuntimeException("The key store [" + unitName + "] does not exist. Nothing to delete.");
    }
    opalKeyStore.deleteKey(alias);

    saveUnitKeyStore(opalKeyStore);
  }

  @Override
  public void importKey(@NotNull String unitName, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull FileObject certificate) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.notNull(certificate, "certificate must not be null");

    OpalKeyStore opalKeyStore = getOrCreateUnitKeyStore(unitName);
    opalKeyStore.importKey(alias, privateKey, certificate);

    saveUnitKeyStore(opalKeyStore);
  }

  @Override
  public void importKey(@NotNull String unitName, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull InputStream certificate) throws NoSuchFunctionalUnitException {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.notNull(certificate, "certificate must not be null");

    OpalKeyStore opalKeyStore = getOrCreateUnitKeyStore(unitName);
    opalKeyStore.importKey(alias, privateKey, certificate);

    saveUnitKeyStore(opalKeyStore);
  }

  @Override
  public void importKey(@NotNull String unitName, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    OpalKeyStore opalKeyStore = getOrCreateUnitKeyStore(unitName);
    opalKeyStore.importKey(alias, privateKey, certificateInfo);

    saveUnitKeyStore(opalKeyStore);
  }

  @Override
  public void importKey(@NotNull String unitName, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull String certificateInfo) throws NoSuchFunctionalUnitException {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    OpalKeyStore opalKeyStore = getOrCreateUnitKeyStore(unitName);
    opalKeyStore.importKey(alias, privateKey, certificateInfo);

    saveUnitKeyStore(opalKeyStore);
  }

  @Override
  public void importCertificate(@NotNull String unitName, @NotNull String alias, @NotNull InputStream certStream) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(certStream, "certStream must not be null");

    OpalKeyStore opalKeyStore = getOrCreateUnitKeyStore(unitName);
    opalKeyStore.importCertificate(alias, certStream);

    saveUnitKeyStore(opalKeyStore);
  }

  private OpalKeyStore loadUnitKeyStore(@NotNull String unitName, @NotNull KeyStoreState state) {
    CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
        .prompt(getPasswordFor(unitName)).build();

    OpalKeyStore opalKeyStore = null;
    try {
      opalKeyStore = new OpalKeyStore(unitName, loadKeyStore(state.getKeyStore(), passwordCallback));
      opalKeyStore.setCallbackHandler(callbackHandler);
      OpalKeyStore.loadBouncyCastle();
    } catch(GeneralSecurityException | UnsupportedCallbackException ex) {
      throw new RuntimeException(ex);
    } catch(IOException ex) {
      clearPasswordCache(callbackHandler, unitName);
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

  private byte[] getKeyStoreByteArray(OpalKeyStore opalKeyStore) {
    String unitName = opalKeyStore.getUnitName();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
          .prompt(getPasswordFor(unitName)).build();
      opalKeyStore.getKeyStore().store(baos, getKeyPassword(passwordCallback));
    } catch(KeyStoreException e) {
      clearPasswordCache(callbackHandler, opalKeyStore.getUnitName());
      throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
    } catch(GeneralSecurityException | UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException ex) {
      clearPasswordCache(callbackHandler, unitName);
      translateAndRethrowKeyStoreIOException(ex);
    }
    return baos.toByteArray();
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
