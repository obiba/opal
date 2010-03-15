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

import java.security.KeyStoreException;

import javax.security.auth.callback.CallbackHandler;

import org.apache.commons.vfs.FileObject;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.opal.core.domain.unit.UnitKeyStore;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.springframework.util.Assert;

/**
 *
 */
public class DefaultUnitKeyStoreServiceImpl extends PersistenceManagerAwareService implements UnitKeyStoreService {
  //
  // Instance Variables
  //

  protected CallbackHandler callbackHandler;

  //
  // UnitKeyStore Methods
  //

  public UnitKeyStore getUnitKeyStore(String unitName) {
    Assert.hasText(unitName, "unitName must not be null or empty");

    UnitKeyStore template = new UnitKeyStore();
    template.setUnit(unitName);

    UnitKeyStore unitKeyStore = getPersistenceManager().matchOne(template);
    if(unitKeyStore != null) {
      unitKeyStore.setCallbackHander(callbackHandler);
      UnitKeyStore.loadBouncyCastle();
    }
    return unitKeyStore;
  }

  public UnitKeyStore getOrCreateUnitKeyStore(String unitName) {
    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore == null) {
      unitKeyStore = UnitKeyStore.Builder.newStore().unit(unitName).passwordPrompt(callbackHandler).build();
      saveUnitKeyStore(unitKeyStore);
    }
    return unitKeyStore;
  }

  public void saveUnitKeyStore(UnitKeyStore unitKeyStore) {
    Assert.notNull(unitKeyStore, "unitKeyStore must not be null");
    getPersistenceManager().save(unitKeyStore);
  }

  public void createOrUpdateKey(String unitName, String alias, String algorithm, int size, String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.hasText(algorithm, "algorithm must not be null or empty");
    Assert.notNull(size, "size must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(certificateInfo);
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
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");

    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore == null) {
      throw new RuntimeException("The key store [" + unitName + "] does not exist.");
    }
    return unitKeyStore.aliasExists(alias);
  }

  public void deleteKey(String unitName, String alias) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");

    UnitKeyStore unitKeyStore = getUnitKeyStore(unitName);
    if(unitKeyStore == null) {
      throw new RuntimeException("The key store [" + unitName + "] does not exist. Nothing to delete.");
    }
    unitKeyStore.deleteKey(alias);
  }

  public void importKey(String unitName, String alias, FileObject privateKey, FileObject certificate) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.notNull(certificate, "certificate must not be null");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificate);
  }

  public void importKey(String unitName, String alias, FileObject privateKey, String certificateInfo) {
    Assert.hasText(unitName, "unitName must not be null or empty");
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.notNull(privateKey, "privateKey must not be null");
    Assert.hasText(certificateInfo, "certificateInfo must not be null or empty");

    UnitKeyStore unitKeyStore = getOrCreateUnitKeyStore(unitName);
    unitKeyStore.importKey(alias, privateKey, certificateInfo);
  }

  //
  // Methods
  //

  public void setCallbackHandler(CallbackHandler callbackHandler) {
    this.callbackHandler = callbackHandler;
  }

}
