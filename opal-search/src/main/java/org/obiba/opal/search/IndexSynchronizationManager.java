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
import java.util.Set;
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

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Autowired
  private Set<IndexManager> indexManagers;

  private final SyncProducer syncProducer = new SyncProducer();

  private SyncConsumer syncConsumer;

  private IndexSynchronization currentTask;

  private final BlockingQueue<IndexSynchronization> indexSyncQueue = new LinkedBlockingQueue<IndexSynchronization>();

  public IndexSynchronizationManager() {
  }

  // Every minute
  @Scheduled(fixedDelay = 60 * 1000)
  public void synchronizeIndices() {
    if(syncConsumer == null) {
      // start one IndexSynchronization consumer thread per index manager
      syncConsumer = new SyncConsumer();
      Thread consumer = new Thread(getSubject().associateWith(syncConsumer));
      consumer.setPriority(Thread.MIN_PRIORITY);
      consumer.start();
    }
    getSubject().execute(syncProducer);
  }

  public void synchronizeIndex(IndexManager indexManager, ValueTable vt, int gracePeriod) {
    syncProducer.index(indexManager, vt, gracePeriod);
  }

  public IndexSynchronization getCurrentTask() {
    return currentTask;
  }

  public void stopTask() {
    currentTask.stop();
    syncProducer.deleteCurrentTaskFromQueue();
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

  private class SyncProducer implements Runnable {

    @Override
    public void run() {
      for(Datasource ds : MagmaEngine.get().getDatasources()) {
        for(ValueTable vt : ds.getValueTables()) {
          try {
            log.debug("Check index for table: {}.{}", ds.getName(), vt.getName());
            for(IndexManager indexManager : indexManagers) {
              checkIndexable(indexManager, vt);
            }
          } catch(Exception ignored) {
            log.debug("Error while checking indexable", ignored);
          }
        }
      }
    }

    private void checkIndexable(IndexManager indexManager, ValueTable vt) {
      if(indexManager.isReady() && indexManager.isIndexable(vt)) {
        ValueTableIndex index = indexManager.getIndex(vt);
        // Check that the index is older than the ValueTable
        if(index.requiresUpgrade() || !index.isUpToDate()) {
          index(indexManager, vt, GRACE_PERIOD);
        }
      }
    }

    private void index(IndexManager indexManager, ValueTable vt, int seconds) {
      // The index needs to be updated
      Value value = vt.getTimestamps().getLastUpdate();
      // Check that the last modification to the ValueTable is older than the gracePeriod
      // If we don't know (null value), reindex
      if(value.isNull() || value.compareTo(gracePeriod(seconds)) < 0) {
        submitTask(indexManager, vt);
      }
    }

    /**
     * Returns a {@code Value} with the date and time at which things are reindexed.
     *
     * @return value
     */
    private Value gracePeriod(int seconds) {
      // Now
      Calendar gracePeriod = Calendar.getInstance();
      // Move back in time by GRACE_PERIOD seconds
      gracePeriod.add(Calendar.SECOND, -seconds);
      // Things modified before this value can be reindexed
      return DateTimeType.get().valueOf(gracePeriod);
    }

    /**
     * Check if the index is not the current task, or in the queue before adding it to the indexation queue.
     *
     * @param indexManager
     * @param vt
     */
    private void submitTask(IndexManager indexManager, ValueTable vt) {
      ValueTableIndex index = indexManager.getIndex(vt);
      if(currentTask != null && currentTask.getValueTableIndex().getIndexName().equals(index.getIndexName())) return;

      boolean alreadyQueued = false;
      for(IndexSynchronization s : indexSyncQueue) {
        if(s.getValueTableIndex().getIndexName().equals(index.getIndexName()) &&
            s.getIndexManager().getName().equals(indexManager.getName())) {
          alreadyQueued = true;
          break;
        }
      }
      if(!alreadyQueued) {
        log.trace("Queueing for indexing {} in {}", index.getIndexName(), indexManager.getName());
        indexSyncQueue.offer(indexManager.createSyncTask(vt, index));
      }
    }

    private void deleteCurrentTaskFromQueue() {
      log.trace("Deleting current task from queue : {}", currentTask.getValueTable().getName());
      indexSyncQueue.remove(currentTask);
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
      currentTask = sync;
      try {
        log.trace("Prepare indexing {} in {}", sync.getValueTableIndex().getIndexName(),
            sync.getIndexManager().getName());
        // check if still indexable: indexation config could have changed
        if(sync.getIndexManager().isReady()) {
          getSubject().execute(sync);
        }
      } finally {
        currentTask = null;
      }
    }
  }
}
