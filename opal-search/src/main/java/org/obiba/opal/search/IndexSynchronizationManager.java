/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.Timestampeds;
import org.obiba.opal.core.runtime.security.BackgroundJobServiceAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IndexSynchronizationManager {

  private static final Logger log = LoggerFactory.getLogger(IndexSynchronizationManager.class);

  private final IndexManager indexManager;

  private final Sync sync = new Sync();

  @Autowired
  public IndexSynchronizationManager(IndexManager indexManager) {
    this.indexManager = indexManager;
  }

  // Every ten seconds
  @Scheduled(fixedDelay = 10 * 1000)
  public void synchronizeIndices() {
    getSubject().execute(sync);
  }

  private Subject getSubject() {
    // Login as background job user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager().authenticate(new BackgroundJobServiceAuthToken()).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private class Sync implements Runnable {

    @Override
    public void run() {
      for(Datasource ds : MagmaEngine.get().getDatasources()) {
        for(ValueTable vt : ds.getValueTables()) {

          ValueTableIndex index = indexManager.getIndex(vt);

          if(Timestampeds.lastUpdateComparator.compare(index, vt) < 0) {
            index.update();
          }
        }
      }
    }
  }
}
