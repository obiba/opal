/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import com.google.common.eventbus.Subscribe;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.*;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.search.event.SynchronizeIndexEvent;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.search.service.IndexManager;
import org.obiba.opal.search.service.IndexSynchronization;
import org.obiba.opal.search.service.ValueTableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages {@code IndexSynchronization} tasks. This class will monitor the state of all indices periodically. When an
 * index is determined to be out of date, a {@code IndexSynchronization} task is created and run.
 */
@Component
@Transactional(readOnly = true)
public class IndexSynchronizationManager {

  private static final Logger log = LoggerFactory.getLogger(IndexSynchronizationManager.class);

  // Grace period before reindexing (in seconds)
  private static final int GRACE_PERIOD = 30;

  private final OpalSearchService opalSearchService;

  private final TransactionalThreadFactory transactionalThreadFactory;

  private final IndexManagerConfigurationService indexConfig;

  private final SyncProducer syncProducer = new SyncProducer();

  private SyncConsumer syncConsumer;

  private IndexSynchronization currentTask;

  private final BlockingQueue<IndexSynchronization> indexSyncQueue = new LinkedBlockingQueue<>();

  private Thread consumer;

  @Autowired
  public IndexSynchronizationManager(OpalSearchService opalSearchService, TransactionalThreadFactory transactionalThreadFactory, IndexManagerConfigurationService indexConfig) {
    this.opalSearchService = opalSearchService;
    this.transactionalThreadFactory = transactionalThreadFactory;
    this.indexConfig = indexConfig;
  }

  // Every minute
  @Scheduled(fixedDelay = 60 * 1000)
  public void synchronizeIndices() {

    if(!opalSearchService.isRunning()) return;

    if(syncConsumer == null) {
      // start one IndexSynchronization consumer thread per index manager
      syncConsumer = new SyncConsumer();
      startConsumerThread();
    } else if(consumer != null && !consumer.isAlive() && !consumer.isInterrupted()) {
      // restart consumer if it died unexpectedly
      startConsumerThread();
    }
    getSubject().execute(syncProducer);
  }

  public void synchronizeIndex(IndexManager indexManager, ValueTable table) {
    syncProducer.index(indexManager, table, 0);
  }

  public IndexSynchronization getCurrentTask() {
    return currentTask;
  }

  public void stopTask() {
    currentTask.stop();
    syncProducer.deleteCurrentTaskFromQueue();
  }

  private void startConsumerThread() {
    consumer = transactionalThreadFactory.newThread(getSubject().associateWith(syncConsumer));
    consumer.setName("Index Synchronization Consumer " + syncConsumer);
    consumer.setPriority(Thread.MIN_PRIORITY);
    consumer.start();
  }

  public void terminateConsumerThread() {
    if(consumer != null && consumer.isAlive()) consumer.interrupt();
  }

  @Subscribe
  public void onSynchronizeIndex(SynchronizeIndexEvent event) {
    synchronizeIndex(event.getIndexManager(), event.getValueTable());
  }

  //
  // Private methods
  //

  private Subject getSubject() {
    // Login as background task user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager()
          .authenticate(BackgroundJobServiceAuthToken.INSTANCE).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private class SyncProducer implements Runnable {

    @Override
    public void run() {
      try {
        for(Datasource ds : MagmaEngine.get().getDatasources()) {
          for(ValueTable table : ds.getValueTables()) {
            if (ValueTableStatus.READY.equals(table.getStatus())) {
              log.debug("Check index for table: {}.{}", ds.getName(), table.getName());
              // tables
              IndexManager indexManager = opalSearchService.getTablesIndexManager();
              checkIndexable(indexManager, table, indexManager.isReady() && !indexManager.getIndex(table).isUpToDate());
              // variables
              indexManager = opalSearchService.getVariablesIndexManager();
              checkIndexable(indexManager, table, indexManager.isReady() && !indexManager.getIndex(table).isUpToDate());
              // values
              indexManager = opalSearchService.getValuesIndexManager();
              checkIndexable(indexManager, table, indexManager.isReady() && indexConfig.getConfig().isReadyForIndexing(table, indexManager.getIndex(table)));
            }
          }
        }
      } catch(Exception ignored) {
        log.warn("Error while checking indexable", ignored);
      }
    }

    private void checkIndexable(IndexManager indexManager, ValueTable table, boolean indexable) {
      if(!indexable) return;
      index(indexManager, table, GRACE_PERIOD);
    }

    private void index(IndexManager indexManager, ValueTable table, int seconds) {
      // The index needs to be updated
      Value value = table.getTimestamps().getLastUpdate();
      // Check that the last modification to the ValueTable is older than the gracePeriod
      // If we don't know (null value), reindex
      if(value.isNull() || value.compareTo(gracePeriod(seconds)) < 0) {
        submitTask(indexManager, table);
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
     * @param table
     */
    private void submitTask(IndexManager indexManager, ValueTable table) {
      ValueTableIndex index = indexManager.getIndex(table);
      if(!isAlreadyQueued(indexManager, index)) {
        log.trace("Queueing for indexing {} in {}", index.getIndexType(), indexManager.getName());
        indexSyncQueue.offer(indexManager.createSyncTask(table, index));
      }
    }

    private void deleteCurrentTaskFromQueue() {
      log.trace("Deleting current task from queue : {}", currentTask.getValueTable().getName());
      indexSyncQueue.remove(currentTask);
    }
  }

  public boolean isAlreadyQueued(IndexManager indexManager, ValueTableIndex index) {
    for(IndexSynchronization s : indexSyncQueue) {
      if(s.getValueTableIndex().getIndexType().equals(index.getIndexType())
          && s.getIndexManager().getName().equals(indexManager.getName())
          && s.getValueTable().getTableReference().equals(index.getValueTableReference())) {
        log.trace("Indexation of {} is already queued in {}...", index.getValueTableReference(), indexManager.getName());
        return true;
      }
    }
    log.trace("Indexation of {} is not queued in {}...", index.getValueTableReference(), indexManager.getName());
    return false;
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
        log.debug("Stopping indexing consumer");
      }
    }

    private void consume(IndexSynchronization sync) {
      currentTask = sync;
      try {
        log.trace("Prepare indexing {} in {} with type {}",
            sync.getValueTableIndex().getValueTableReference(), sync.getIndexManager().getName(), sync.getValueTableIndex().getIndexType());
        // check if still indexable: indexation config could have changed
        if(sync.getIndexManager().isReady()) {
          getSubject().execute(sync);
        }
      } catch(NoSuchDatasourceException | NoSuchValueTableException e) {
        log.trace("Cannot index: ", e.getMessage());
      } finally {
        currentTask = null;
      }
    }
  }
}
