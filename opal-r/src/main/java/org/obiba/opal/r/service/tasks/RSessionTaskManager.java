package org.obiba.opal.r.service.tasks;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.security.SessionDetachedSubject;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.*;
import org.obiba.opal.spi.r.RServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RSessionTaskManager implements DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(RSessionTaskManager.class);

  protected final TransactionalThreadFactory transactionalThreadFactory;

  private final RServerManagerService rServerManagerService;

  private final BlockingQueue<RSessionTask> tasksQueue = new LinkedBlockingQueue<>();

  private RSessionTaskConsumer taskConsumer;
  private final ExecutorService threadPool;
  private final AtomicBoolean isRunning;

  /**
   * All R session tasks.
   */
  private final List<RSessionTask> tasksList = Collections.synchronizedList(new LinkedList<>());

  @Autowired
  public RSessionTaskManager(TransactionalThreadFactory transactionalThreadFactory, RServerManagerService rServerManagerService) {
    this.transactionalThreadFactory = transactionalThreadFactory;
    this.rServerManagerService = rServerManagerService;
    this.isRunning = new AtomicBoolean(false);
    this.threadPool = Executors.newFixedThreadPool(3, transactionalThreadFactory);
  }

  public RSessionTask makeCreationTask(String principal, RServerProfile profile, SubjectRSessions rSessions) {
    ensureRSessionTasksConsumer();
    try {
      RSessionTask task = buildCreationTask(principal, profile, rSessions);
      tasksQueue.put(task);
      tasksList.add(task);
      return task;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Failed to enqueue R task", e);
    }
  }

  public RServerSession makeRSession(String principal, RServerProfile profile, SubjectRSessions rSessions) throws RServerException {
    RSessionCreationTask task = buildCreationTask(principal, profile, rSessions);
    return makeRSession(task);
  }

  public RSessionTask getTask(String id) {
    return tasksList.stream().filter((tsk) -> tsk.getId().equals(id)).findFirst().orElseThrow(() -> new NoSuchElementException(id));
  }

  @Override
  public void destroy() throws Exception {
    closeTasksQueue();
  }

  //
  // Private methods
  //

  private void closeTasksQueue() {
    isRunning.set(false);
    threadPool.shutdown();
    try {
      if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
        threadPool.shutdownNow();
      }
    } catch (InterruptedException e) {
      threadPool.shutdownNow();
      Thread.currentThread().interrupt();
    } finally {
      tasksList.clear();
      tasksQueue.clear();
    }
  }

  private void ensureRSessionTasksConsumer() {
    if (isRunning.compareAndSet(false, true)) {
      for (int i = 0; i < 3; i++) {
        threadPool.submit(new RSessionTaskConsumer());
      }
      log.info("R session task processor started with 3 worker threads");
    }
  }

  private RSessionCreationTask buildCreationTask(String principal, RServerProfile profile, SubjectRSessions rSessions) {
    return new RSessionCreationTask(UUID.randomUUID().toString(), principal, profile, rSessions);
  }

  private RServerSession makeRSession(RSessionCreationTask task) throws RServerException {
    RServerService service = rServerManagerService.getRServer(task.getProfile().getCluster());
    RServerSession rSession = service.newRServerSession(task.getPrincipal());
    rSession.setProfile(task.getProfile());
    task.getRSessions().addRSession(rSession);
    return rSession;
  }

  private class RSessionTaskConsumer implements Runnable {

    @Override
    public void run() {
      log.debug("Starting R operations consumer");
      while (isRunning.get()) {
        try {
          RSessionTask task = tasksQueue.poll(5, TimeUnit.SECONDS);
          if (task != null) {
            consume(task);
          }
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
          break;
        } catch (Exception e) {
          log.error("Error in R command consumer", e);
        }
      }
    }

    private void consume(RSessionTask task) {
      try {
        task.inProgress();
        if (task instanceof RSessionCreationTask) {
          makeRSession((RSessionCreationTask) task);
        }
      } catch (Exception e) {
        log.error("Error when consuming R session task: {}", e.getMessage(), e);
        task.failed(e.getMessage());
      }
      synchronized (task) {
        task.notifyAll();
      }
    }
  }

}
