/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search;

import java.util.Calendar;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.Timestampeds;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.runtime.security.BackgroundJobServiceAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@code IndexSynchronization} tasks. This class will monitor the state of all indices periodically. When an
 * index is determined to be out of date, a {@code IndexSynchronization} task is created and run.
 */
@Component
@Transactional
public class IndexSynchronizationManager {

  private static final Logger log = LoggerFactory.getLogger(IndexSynchronizationManager.class);

  @Autowired
  private IndexManager indexManager;

  // Grace period before reindexing (in seconds)
  private final int GRACE_PERIOD = 300;

  private final Sync sync = new Sync();

  public IndexSynchronizationManager() {
    // this.indexManager = indexManager;
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

  private void submit(IndexSynchronization sync) {
    sync.run();
  }

  /**
   * Returns a {@code Value} with the date and time at which things are reindexed.
   * @return
   */
  private Value gracePeriod() {
    // Now
    Calendar gracePeriod = Calendar.getInstance();
    // Move back in time by GRACE_PERIOD seconds
    gracePeriod.add(Calendar.SECOND, -GRACE_PERIOD);
    // Things modified before this value can be reindexed
    return DateTimeType.get().valueOf(gracePeriod);
  }

  private class Sync implements Runnable {

    @Override
    public void run() {
      for(Datasource ds : MagmaEngine.get().getDatasources()) {
        for(ValueTable vt : ds.getValueTables()) {

          ValueTableIndex index = indexManager.getIndex(vt);

          // Check that the index is older than the ValueTable
          if(Timestampeds.lastUpdateComparator.compare(index, vt) < 0) {
            // The index needs to be updated
            Value value = vt.getTimestamps().getLastUpdate();
            // Check that the last modification to the ValueTable is older than the gracePeriod
            // If we don't know (null value), reindex
            if(value.isNull() || value.compareTo(gracePeriod()) < 0) {
              IndexSynchronization sync = indexManager.createSyncTask(vt, index);
              submit(sync);
            }
          }

        }
      }
    }
  }
}
