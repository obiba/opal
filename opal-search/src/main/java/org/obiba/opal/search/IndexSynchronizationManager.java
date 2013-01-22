/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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

  // Grace period before reindexing (in seconds)
  private static final int GRACE_PERIOD = 300;

  @Autowired
  private IndexManager indexManager;

  private final SyncProducer syncProducer = new SyncProducer();

  private boolean consumerStarted;

  private IndexSynchronization currentTask;

  private BlockingQueue<IndexSynchronization> indexSyncQueue;

  public IndexSynchronizationManager() {
    consumerStarted = false;
    indexSyncQueue = new LinkedBlockingQueue<IndexSynchronization>();
  }

  // Every minute
  @Scheduled(fixedDelay = 60 * 1000)
  public void synchronizeIndices() {
    getSubject().execute(syncProducer);
    if(consumerStarted == false) {
      // start one IndexSynchronization consumer thread
      Thread consumer = new Thread(getSubject().associateWith(new SyncConsumer()));
      consumer.setPriority(Thread.MIN_PRIORITY);
      consumer.start();
      consumerStarted = true;
    }
  }

  public void synchronizeIndex(ValueTable vt) {
    syncProducer.index(vt);
  }

  public boolean hasTask() {
    return currentTask != null;
  }

  public IndexSynchronization getCurrentTask() {
    return currentTask;
  }

  public void stopTask(){
    currentTask.stop();
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
          log.debug("Check index for table: {}.{}", ds.getName(), vt.getName());
          if(indexManager.isIndexable(vt) && indexManager.isReadyForIndexing(vt)) {
            maybeUpdateIndex(vt);
          }
        }
      }
    }

    private void maybeUpdateIndex(ValueTable vt) {
      ValueTableIndex index = indexManager.getIndex(vt);

      // Check that the index is older than the ValueTable
      if(index.requiresUpgrade() || !index.isUpToDate()) {
        index(vt);
      }
    }

    private void index(ValueTable vt) {
      // The index needs to be updated
      Value value = vt.getTimestamps().getLastUpdate();
      // Check that the last modification to the ValueTable is older than the gracePeriod
      // If we don't know (null value), reindex
      if(value.isNull() || value.compareTo(gracePeriod()) < 0) {
        submitTask(vt, indexManager.getIndex(vt));
      }
    }

    /**
     * Check if the index is not the current task, or in the queue before adding it to the indexation queue.
     *
     * @param vt
     * @param index
     */
    private void submitTask(ValueTable vt, ValueTableIndex index) {
      if(currentTask != null && currentTask.getValueTableIndex().getName().equals(index.getName())) return;

      boolean alreadyQueued = false;
      for(IndexSynchronization s : indexSyncQueue) {
        if(s.getValueTableIndex().getName().equals(index.getName())) {
          alreadyQueued = true;
          break;
        }
      }
      if(alreadyQueued == false) {
        log.trace("Queueing for indexing {}", index.getName());
        IndexSynchronization sync = indexManager.createSyncTask(vt, index);
        indexSyncQueue.offer(sync);
      }
    }
  }

  private class SyncConsumer implements Runnable {

    @Override
    public void run() {
      log.debug("Starting indexing consumer");
      try {
        //noinspection InfiniteLoopStatement
        while(true) {
          consume(indexSyncQueue.take());
        }
      } catch(InterruptedException ignored) {
      }
    }

    private void consume(IndexSynchronization sync) {
      log.trace("Prepare indexing {}", sync.getValueTableIndex().getName());
      currentTask = sync;
      try {
        // check if still indexable: indexation config could have changed
        if(indexManager.isIndexable(sync.getValueTable())) {
          getSubject().execute(sync);
        }
      } finally {
        currentTask = null;
      }
    }
  }
}
