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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.vfs.FileObject;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.opal.core.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.crypt.CachingCallbackHandler;
import org.obiba.opal.core.crypt.KeyProviderSecurityException;
import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Transactional
public class DefaultUnitKeyStoreServiceImpl extends PersistenceManagerAwareService implements UnitKeyStoreService {
  //
  // Constants
  //

  private static final String PASSWORD_FOR = "Password for";

  //
  // Instance Variables
  //

  protected CallbackHandler callbackHandler;

  protected OpalRuntime opalRuntime;

  //
  // UnitKeyStore Methods
  //

  public UnitKeyStore getUnitKeyStore(String unitName) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    validateUnitExists(unitName);

    UnitKeyStoreState template = new UnitKeyStoreState();
    template.setUnit(unitName);

    UnitKeyStoreState unitKeyStoreState = getPersistenceManager().matchOne(template);
    if(unitKeyStoreState != null) {
      return loadUnitKeyStore(unitName, unitKeyStoreState);
    }

    return null;
  }

  public UnitKeyStore getOrCreateUnitKeyStore(String unitName) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    validateUnitExists(unitName);

    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore == null) {
      unitKeyStore = UnitKeyStore.Builder.newStore().unit(unitName).passwordPrompt(callbackHandler).build();
      saveUnitKeyStore(unitKeyStore);
    }
    return unitKeyStore;
  }

  public void saveUnitKeyStore(UnitKeyStore unitKeyStore) {
    Assert.notNull(unitKeyStore, "unitKeyStore must not be null");

    UnitKeyStoreState template = new UnitKeyStoreState();
    template.setUnit(unitKeyStore.getUnitName());

    UnitKeyStoreState unitKeyStoreState = getPersistenceManager().matchOne(template);
    if(unitKeyStoreState == null) {
      unitKeyStoreState = template;
    }
    unitKeyStoreState.setKeyStore(getKeyStoreByteArray(unitKeyStore));

    getPersistenceManager().save(unitKeyStoreState);
  }

  public void createOrUpdateKey(String unitName, String alias, String algorithm, int size, String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.hasText(algorithm, "algorithm must not be null or empty");
    Assert.notNull(size, "size must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");
    validateUnitExists(unitName);

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

  public boolean aliasExists(String unitName, String alias) {
    Assert.hasText(alias, "alias must not be null or empty");

    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore != null) {
      return unitKeyStore.aliasExists(alias);
    }
    return false;
  }

  public void deleteKey(String unitName, String alias) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    validateUnitExists(unitName);

    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore == null) {
      throw new RuntimeException("The key store [" + unitName + "] does not exist. Nothing to delete.");
    }
    unitKeyStore.deleteKey(alias);

    saveUnitKeyStore(unitKeyStore);
  }

  public void importKey(String unitName, String alias, FileObject privateKey, FileObject certificate) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.notNull(certificate, "certificate must not be null");
    validateUnitExists(unitName);

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificate);

    saveUnitKeyStore(unitKeyStore);
  }

  public void importKey(String unitName, String alias, FileObject privateKey, String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");
    validateUnitExists(unitName);

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificateInfo);

    saveUnitKeyStore(unitKeyStore);
  }

  //
  // Methods
  //

  public void setCallbackHandler(CallbackHandler callbackHandler) {
    this.callbackHandler = callbackHandler;
  }

  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  private void validateUnitExists(String unitName) throws NoSuchFunctionalUnitException {
    if(opalRuntime.getOpalConfiguration().getFunctionalUnit(unitName) == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
  }

  private UnitKeyStore loadUnitKeyStore(String unitName, UnitKeyStoreState unitKeyStoreState) {
    CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName).prompt(getPasswordFor(unitName)).build();

    UnitKeyStore unitKeyStore = null;
    try {
      unitKeyStore = new UnitKeyStore(unitName, loadKeyStore(unitKeyStoreState.getKeyStore(), passwordCallback));
      unitKeyStore.setCallbackHander(callbackHandler);
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

  private KeyStore loadKeyStore(byte[] keyStoreBytes, CacheablePasswordCallback passwordCallback) throws GeneralSecurityException, UnsupportedCallbackException, IOException {
    KeyStore ks = KeyStore.getInstance("JCEKS");
    ks.load(new ByteArrayInputStream(keyStoreBytes), getKeyPassword(passwordCallback));

    return ks;
  }

  private byte[] getKeyStoreByteArray(UnitKeyStore unitKeyStore) {
    String unitName = unitKeyStore.getUnitName();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName).prompt(getPasswordFor(unitName)).build();
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

  private char[] getKeyPassword(CacheablePasswordCallback passwordCallback) throws UnsupportedCallbackException, IOException {
    callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
    return passwordCallback.getPassword();
  }

  /**
   * Returns "Password for 'name':  ".
   */
  private String getPasswordFor(String name) {
    return new StringBuilder().append(PASSWORD_FOR).append(" '").append(name).append("':  ").toString();
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
