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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.vfs2.FileObject;
import org.obiba.opal.core.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.crypt.CachingCallbackHandler;
import org.obiba.opal.core.crypt.KeyProviderSecurityException;
import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
@Transactional
public class DefaultUnitKeyStoreServiceImpl implements UnitKeyStoreService {

  @Nonnull
  private final CallbackHandler callbackHandler;

  @Nonnull
  private final OrientDbDocumentService orientDbDocumentService;

  @Autowired
  public DefaultUnitKeyStoreServiceImpl(@Nonnull CallbackHandler callbackHandler,
      @Nonnull OrientDbDocumentService orientDbDocumentService) {
    this.callbackHandler = callbackHandler;
    this.orientDbDocumentService = orientDbDocumentService;
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbDocumentService.createUniqueStringIndex(UnitKeyStoreState.class, "name");
  }

  @Override
  public void stop() {
  }

  @Nullable
  @Override
  public UnitKeyStore getUnitKeyStore(@Nonnull String unitName) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    UnitKeyStoreState state = findByUnit(unitName);
    return state == null ? null : loadUnitKeyStore(unitName, state);
  }

  private UnitKeyStoreState findByUnit(String unitName) {
    return orientDbDocumentService.uniqueResult(UnitKeyStoreState.class,
        "select from " + UnitKeyStoreState.class.getSimpleName() + " where unit = ?", unitName);
  }

  @Override
  public UnitKeyStore getOrCreateUnitKeyStore(@Nonnull String unitName) {
    Assert.hasText(unitName, "unitName must not be null or empty");

    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore == null) {
      unitKeyStore = UnitKeyStore.Builder.newStore().unit(unitName).passwordPrompt(callbackHandler).build();
      saveUnitKeyStore(unitKeyStore);
    }
    return unitKeyStore;
  }

  @Override
  public void saveUnitKeyStore(@Nonnull UnitKeyStore unitKeyStore) {
    Assert.notNull(unitKeyStore, "unitKeyStore must not be null");

    UnitKeyStoreState state;
    String unitName = unitKeyStore.getUnitName();
    UnitKeyStoreState existing = findByUnit(unitName);
    if(existing == null) {
      state = new UnitKeyStoreState();
      state.setUnit(unitName);
    } else {
      state = existing;
    }
    state.setKeyStore(getKeyStoreByteArray(unitKeyStore));

    orientDbDocumentService.save(state);
  }

  @Override
  public void createOrUpdateKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull String algorithm, int size,
      @Nonnull String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.hasText(algorithm, "algorithm must not be null or empty");
    Assert.notNull(size, "size must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    try {
      if(unitKeyStore.getKeyStore().containsAlias(alias)) {
        unitKeyStore.getKeyStore().deleteEntry(alias);
      }
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
    unitKeyStore.createOrUpdateKey(alias, algorithm, size, certificateInfo);

    saveUnitKeyStore(unitKeyStore);
  }

  @Override
  public boolean aliasExists(@Nonnull String unitName, @Nonnull String alias) {
    Assert.hasText(alias, "alias must not be null or empty");
    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    return unitKeyStore != null && unitKeyStore.aliasExists(alias);
  }

  @Override
  public void deleteKey(@Nonnull String unitName, @Nonnull String alias) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");

    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore == null) {
      throw new RuntimeException("The key store [" + unitName + "] does not exist. Nothing to delete.");
    }
    unitKeyStore.deleteKey(alias);

    saveUnitKeyStore(unitKeyStore);
  }

  @Override
  public void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull FileObject privateKey,
      @Nonnull FileObject certificate) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.notNull(certificate, "certificate must not be null");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificate);

    saveUnitKeyStore(unitKeyStore);
  }

  @Override
  public void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull InputStream privateKey,
      @Nonnull InputStream certificate) throws NoSuchFunctionalUnitException {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.notNull(certificate, "certificate must not be null");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificate);

    saveUnitKeyStore(unitKeyStore);
  }

  @Override
  public void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull FileObject privateKey,
      @Nonnull String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificateInfo);

    saveUnitKeyStore(unitKeyStore);
  }

  @Override
  public void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull InputStream privateKey,
      @Nonnull String certificateInfo) throws NoSuchFunctionalUnitException {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificateInfo);

    saveUnitKeyStore(unitKeyStore);
  }

  @Override
  public void importCertificate(@Nonnull String unitName, @Nonnull String alias, @Nonnull InputStream certStream) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(certStream, "certStream must not be null");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importCertificate(alias, certStream);

    saveUnitKeyStore(unitKeyStore);
  }

  private UnitKeyStore loadUnitKeyStore(@Nonnull String unitName, @Nonnull UnitKeyStoreState state) {
    CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
        .prompt(getPasswordFor(unitName)).build();

    UnitKeyStore unitKeyStore = null;
    try {
      unitKeyStore = new UnitKeyStore(unitName, loadKeyStore(state.getKeyStore(), passwordCallback));
      unitKeyStore.setCallbackHandler(callbackHandler);
      UnitKeyStore.loadBouncyCastle();
    } catch(GeneralSecurityException ex) {
      throw new RuntimeException(ex);
    } catch(UnsupportedCallbackException ex) {
      throw new RuntimeException(ex);
    } catch(IOException ex) {
      clearPasswordCache(callbackHandler, unitName);
      translateAndRethrowKeyStoreIOException(ex);
    }

    return unitKeyStore;
  }

  private KeyStore loadKeyStore(byte[] keyStoreBytes, CacheablePasswordCallback passwordCallback)
      throws GeneralSecurityException, UnsupportedCallbackException, IOException {
    KeyStore ks = KeyStore.getInstance("JCEKS");
    ks.load(new ByteArrayInputStream(keyStoreBytes), getKeyPassword(passwordCallback));

    return ks;
  }

  private byte[] getKeyStoreByteArray(UnitKeyStore unitKeyStore) {
    String unitName = unitKeyStore.getUnitName();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
          .prompt(getPasswordFor(unitName)).build();
      unitKeyStore.getKeyStore().store(baos, getKeyPassword(passwordCallback));
    } catch(KeyStoreException e) {
      clearPasswordCache(callbackHandler, unitKeyStore.getUnitName());
      throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch(IOException ex) {
      clearPasswordCache(callbackHandler, unitName);
      translateAndRethrowKeyStoreIOException(ex);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
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
    return UnitKeyStore.PASSWORD_FOR + " '" + name + "':  ";
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
