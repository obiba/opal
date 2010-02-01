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

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.opal.core.crypt.StudyKeyStore;
import org.obiba.opal.core.service.StudyKeyStoreService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Transactional
public abstract class DefaultStudyKeyStoreServiceImpl extends PersistenceManagerAwareService implements StudyKeyStoreService {

  protected CallbackHandler callbackHandler;

  public StudyKeyStore getStudyKeyStore(String studyId) {
    Assert.hasText(studyId, "studyId must not be null or empty");
    StudyKeyStore template = new StudyKeyStore();
    template.setStudyId(studyId);
    StudyKeyStore studyKeyStore = getPersistenceManager().matchOne(template);
    if(studyKeyStore != null) {
      studyKeyStore.setCallbackHander(callbackHandler);
    }
    return studyKeyStore;
  }

  public void saveStudyKeyStore(StudyKeyStore studyKeyStore) {
    Assert.notNull(studyKeyStore, "studyKeyStore must not be null");
    getPersistenceManager().save(studyKeyStore);
  }

  public void createOrUpdateKey(String alias, String algorithm, int size, String certificateInfo) {
    Assert.hasText(alias, "alias must not be null or empty");
    Assert.hasText(algorithm, "algorithm must not be null or empty");
    Assert.notNull(size, "size must not be null");
    Assert.hasText(certificateInfo, "studyId must not be null or empty");
    StudyKeyStore defaultStore = getStudyKeyStore(DEFAULT_STUDY_ID);
    if(defaultStore == null) {
      defaultStore = StudyKeyStore.Builder.newStore().studyId(DEFAULT_STUDY_ID).passwordPrompt(callbackHandler).build();
    }
    try {
      if(defaultStore.getKeyStore().containsAlias(alias)) {
        defaultStore.getKeyStore().deleteEntry(alias);
      }
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
    defaultStore.createOfUpdateKey(alias, algorithm, size, certificateInfo);
    saveStudyKeyStore(defaultStore);
  }

  public void setCallbackHandler(CallbackHandler callbackHandler) {
    this.callbackHandler = callbackHandler;
  }

  public void deleteKey(String alias) {
    Assert.hasText(alias, "alias must not be null or empty");
    StudyKeyStore defaultStore = getStudyKeyStore(DEFAULT_STUDY_ID);
    if(defaultStore == null) {
      throw new RuntimeException("The key store [" + DEFAULT_STUDY_ID + "] does not exist. Nothing to delete.");
    }
    defaultStore.deleteKey(alias);
  }

  public boolean aliasExists(String alias) {
    Assert.hasText(alias, "alias must not be null or empty");
    StudyKeyStore defaultStore = getStudyKeyStore(DEFAULT_STUDY_ID);
    if(defaultStore == null) {
      throw new RuntimeException("The key store [" + DEFAULT_STUDY_ID + "] does not exist.");
    }
    return defaultStore.aliasExists(alias);
  }

}
