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

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.opal.core.crypt.StudyKeyStore;
import org.obiba.opal.core.service.StudyKeyStoreService;
import org.springframework.util.Assert;

public abstract class DefaultStudyKeyStoreServiceImpl extends PersistenceManagerAwareService implements StudyKeyStoreService {

  public StudyKeyStore getStudyKeyStore(String studyId) {
    Assert.hasText(studyId, "studyId must not be null or empty");
    StudyKeyStore template = new StudyKeyStore();
    template.setStudyId(studyId);
    return getPersistenceManager().matchOne(template);
  }

  public void saveStudyKeyStore(StudyKeyStore studyKeyStore) {
    Assert.notNull(studyKeyStore, "studyKeyStore must not be null");
    getPersistenceManager().save(studyKeyStore);
  }

}
