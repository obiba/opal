/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.storage;

import org.apache.commons.dbcp2.BasicDataSource;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * Forces the open H2 databases to physical disk, periodically.
 * <p>
 * Nothing else does. While Opal runs, {@code FileStore.writeInBackground()} only calls {@code MVStore.tryCommit()}:
 * pages are written to the file, and the file is never synced. {@code WRITE_DELAY} sets how often that commit happens
 * and buys no durability at all. A machine that loses power has lost everything written since the last sync, and
 * without this task the last sync was the last time the database was opened.
 * <p>
 * The shutdown case is not this task's business and is deliberately not implemented here. H2 already ends a clean
 * close in {@code FileChannel.force(true)} — {@code closeOpenFilesAndUnlock()} → {@code FileStore.stop()} →
 * {@code writeCleanShutdown()} → {@code sync()} — and that close is reached both when the last connection is released
 * and from the JVM shutdown hook. Adding a checkpoint there would add code that does nothing;
 * {@code H2DatabaseUrls.validateProperties} is what keeps the settings that would disable it out of reach.
 */
@Component
public class H2Checkpointer implements SystemService {

  private static final Logger log = LoggerFactory.getLogger(H2Checkpointer.class);

  /**
   * Flush the store and fsync the file. {@code CHECKPOINT} alone does not sync, which is the whole point here.
   */
  private static final String CHECKPOINT_SYNC = "CHECKPOINT SYNC";

  @Value("${org.obiba.opal.storage.checkpoint.interval}")
  private long interval;

  @Autowired
  @Qualifier("configDataSource")
  private DataSource configDataSource;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private StorageTaskScheduler scheduler;

  private ScheduledFuture<?> task;

  /**
   * Data sources whose checkpoint failed, so that a database Opal is not administrator of is reported once rather than
   * on every tick.
   */
  private final Set<String> failed = Collections.synchronizedSet(new HashSet<>());

  @Override
  public void start() {
    if(interval <= 0) {
      log.info("H2 checkpointing is disabled (org.obiba.opal.storage.checkpoint.interval={})", interval);
      return;
    }
    task = scheduler.scheduleWithFixedDelay(this::checkpoint, Duration.ofMillis(interval));
    log.info("H2 checkpointing every {} ms", interval);
  }

  @Override
  public void stop() {
    if(task != null) {
      task.cancel(false);
      task = null;
    }
  }

  /**
   * The configuration database, plus the storage databases that are open. Databases that have never been used are not
   * opened to be checkpointed.
   */
  void checkpoint() {
    checkpoint(configDataSource);
    for(DataSource dataSource : databaseRegistry.getLoadedDataSources()) {
      checkpoint(dataSource);
    }
  }

  private void checkpoint(DataSource dataSource) {
    if(dataSource == null || isBusy(dataSource)) return;
    try(Connection connection = dataSource.getConnection()) {
      if(!isH2(connection)) return;
      long start = System.currentTimeMillis();
      try(Statement statement = connection.createStatement()) {
        statement.execute(CHECKPOINT_SYNC);
      }
      failed.remove(key(dataSource));
      log.debug("Checkpointed {} in {} ms", connection.getMetaData().getURL(), System.currentTimeMillis() - start);
    } catch(SQLException | RuntimeException e) {
      // CHECKPOINT SYNC needs the admin right. Opal creates the databases it registers, so it holds that right; an
      // externally created H2 database opened with a restricted account is the case this catches.
      if(failed.add(key(dataSource))) {
        log.warn("Cannot checkpoint an H2 database, its writes stay unsynced until it is closed: {}", e.getMessage());
      }
    }
  }

  /**
   * The pools are built with no maximum wait, so borrowing a connection from a saturated one would block this thread
   * for as long as the load lasts. A database that busy is being written to, which is the case a checkpoint matters
   * least for: skip it and take it on the next tick.
   */
  private boolean isBusy(DataSource dataSource) {
    if(!(dataSource instanceof BasicDataSource)) return false;
    BasicDataSource pool = (BasicDataSource) dataSource;
    boolean busy = pool.getNumIdle() == 0 && pool.getNumActive() >= pool.getMaxTotal();
    if(busy) log.debug("Skipping the checkpoint of {}, its connection pool is saturated", pool.getUrl());
    return busy;
  }

  /**
   * Ask the connection rather than the registered driver class: the configuration database can be an external
   * PostgreSQL one, which manages its own durability and has no CHECKPOINT statement to run.
   */
  private boolean isH2(Connection connection) throws SQLException {
    return "H2".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
  }

  private String key(DataSource dataSource) {
    return dataSource.getClass().getName() + '@' + System.identityHashCode(dataSource);
  }
}
