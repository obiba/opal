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

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

/**
 * The timer the storage services run on: the disk space sampler and the H2 checkpointer.
 * <p>
 * They get their own threads rather than the ones behind {@code @Scheduled}, which are a single thread shared by every
 * annotated method in Opal. A {@code CHECKPOINT SYNC} on a large store is an fsync of everything written since the last
 * one, and while it runs nothing else on that thread moves — including the R session cleanup that expects to run every
 * minute.
 * <p>
 * This is deliberately not a {@code TaskScheduler} bean. Declaring one would make it the scheduler for every
 * {@code @Scheduled} method in the application, which is the opposite of the isolation wanted here.
 */
@Component
public class StorageTaskScheduler implements InitializingBean, DisposableBean {

  private static final int POOL_SIZE = 2;

  private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

  @Override
  public void afterPropertiesSet() {
    scheduler.setPoolSize(POOL_SIZE);
    scheduler.setThreadNamePrefix("opal-storage-");
    // A checkpoint in flight is not worth waiting for at shutdown: the close that follows performs the same fsync.
    scheduler.setWaitForTasksToCompleteOnShutdown(false);
    scheduler.setAwaitTerminationSeconds(5);
    scheduler.initialize();
  }

  @Override
  public void destroy() {
    scheduler.shutdown();
  }

  /**
   * Run a task repeatedly, waiting the given delay between the end of one run and the start of the next. A run that
   * takes longer than the delay therefore pushes the next one back instead of piling up behind it.
   */
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
    return scheduler.scheduleWithFixedDelay(task, delay);
  }
}
