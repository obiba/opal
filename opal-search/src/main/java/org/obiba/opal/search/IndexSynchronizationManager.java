/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
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

  private final SyncProducer syncProducer = new SyncProducer();

  private boolean consumerStarted = false;

  private IndexSynchronization currentTask;

  private BlockingQueue<IndexSynchronization> indexSyncQueue = new LinkedBlockingQueue<IndexSynchronization>();

  public IndexSynchronizationManager() {
  }

  // Every minute
  @Scheduled(fixedDelay = 60 * 1000)
  public void synchronizeIndices() {
    getSubject().execute(syncProducer);
    if(consumerStarted == false) {
      // start one IndexSynchronization consumer thread
      new Thread(new SyncConsumer()).start();
      consumerStarted = true;
    }
  }

  public boolean hasTask() {
    return currentTask != null;
  }

  public IndexSynchronization getCurrentTask() {
    return currentTask;
  }

  private Subject getSubject() {
    // Login as background job user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager()
          .authenticate(new BackgroundJobServiceAuthToken()).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a {@code Value} with the date and time at which things are reindexed.
   *
   * @return value
   */
  private Value gracePeriod() {
    // Now
    Calendar gracePeriod = Calendar.getInstance();
    // Move back in time by GRACE_PERIOD seconds
    gracePeriod.add(Calendar.SECOND, -GRACE_PERIOD);
    // Things modified before this value can be reindexed
    return DateTimeType.get().valueOf(gracePeriod);
  }

  private class SyncProducer implements Runnable {

    @Override
    public void run() {
      for(Datasource ds : MagmaEngine.get().getDatasources()) {
        for(ValueTable vt : ds.getValueTables()) {
          log.info("Check index for table: {}.{}", ds.getName(), vt.getName());
          if(indexManager.isIndexable(vt)) {
            maybeUpdateIndex(vt);
          }
        }
      }
    }

    private void maybeUpdateIndex(ValueTable vt) {
      ValueTableIndex index = indexManager.getIndex(vt);

      if(index.requiresUpgrade()) {
        submitTask(vt, index);
      }
      // Check that the index is older than the ValueTable
      else if(index.isUpToDate() == false) {
        // The index needs to be updated
        Value value = vt.getTimestamps().getLastUpdate();
        // Check that the last modification to the ValueTable is older than the gracePeriod
        // If we don't know (null value), reindex
        if(value.isNull() || value.compareTo(gracePeriod()) < 0) {
          submitTask(vt, index);
        }
      }
    }

    private void submitTask(ValueTable vt, ValueTableIndex index) {
      boolean alreadyQueued = false;
      for(IndexSynchronization s : indexSyncQueue) {
        if(s.getValueTableIndex().getName().equals(index.getName())) {
          alreadyQueued = true;
          break;
        }
      }
      if(alreadyQueued == false) {
        log.info("Queueing for indexing {}", index.getName());
        IndexSynchronization sync = indexManager.createSyncTask(vt, index);
        indexSyncQueue.offer(sync);
      }
    }
  }

  private class SyncConsumer implements Runnable {

    @Override
    public void run() {
      log.info("Starting indexing consumer");
      try {
        while(true) {
          consume(indexSyncQueue.take());
        }
      } catch(InterruptedException ex) {
      }
    }

    private void consume(IndexSynchronization sync) {
      log.info("*************** Indexing {}", sync.getValueTableIndex().getName());
      currentTask = sync;
      try {
        getSubject().execute(sync);
      } finally {
        currentTask = null;
      }
    }
  }
}
