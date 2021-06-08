/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.security.SessionDetachedSubject;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.spi.r.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Defines the common attributes of what is a R server session.
 */
public abstract class AbstractRServerSession implements RServerSession {

  private static final Logger log = LoggerFactory.getLogger(AbstractRServerSession.class);

  protected final TransactionalThreadFactory transactionalThreadFactory;

  protected final Lock lock = new ReentrantLock();

  private final String id;

  private final String user;

  private final Date created;

  private Date timestamp;

  private boolean busy = false;

  private String executionContext = DEFAULT_CONTEXT;

  private final String clusterName;

  private final String serverName;

  private String profile;

  /**
   * R commands to be processed.
   */
  private final BlockingQueue<RCommand> rCommandQueue = new LinkedBlockingQueue<>();

  /**
   * All R commands.
   */
  private final List<RCommand> rCommandList = Collections.synchronizedList(new LinkedList<>());

  private RCommandsConsumer rCommandsConsumer;

  private Thread consumer;

  /**
   * R command identifier increment.
   */
  private int commandId = 1;

  protected AbstractRServerSession(String clusterName, String serverName, String id, String user, TransactionalThreadFactory transactionalThreadFactory) {
    this.id = id;
    this.user = user;
    this.transactionalThreadFactory = transactionalThreadFactory;
    this.created = new Date();
    this.timestamp = created;
    this.clusterName = clusterName;
    this.serverName = serverName;
  }

  //
  // RServerSession methods
  //

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void touch() {
    timestamp = new Date();
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public Date getCreated() {
    return created;
  }

  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean isBusy() {
    return busy;
  }

  @Override
  public void setExecutionContext(String executionContext) {
    this.executionContext = executionContext;
  }

  @Override
  public String getExecutionContext() {
    return Strings.isNullOrEmpty(executionContext) ? DEFAULT_CONTEXT : executionContext;
  }

  @Override
  public void setProfile(String profile) {
    this.profile = profile;
  }

  @Override
  public String getProfile() {
    return profile;
  }

  @Override
  public String getRServerClusterName() {
    return clusterName;
  }

  @Override
  public String getRServerServiceName() {
    return serverName;
  }

  @Override
  public boolean hasExpired(long timeout) {
    Date now = new Date();
    return !busy && now.getTime() - timestamp.getTime() > timeout * 60 * 1000;
  }

  @Override
  public File getWorkspace(String saveId) {
    File ws = new File(getWorkspaces(), getUser() + File.separatorChar + (Strings.isNullOrEmpty(saveId) ? getId() : saveId));
    if (!ws.exists()) ws.mkdirs();
    return ws;
  }

  @Override
  public void saveRSessionFiles(String saveId) {
    // save the files (if any)
    String rscript = "list.files(path = '.', recursive = TRUE)";
    RScriptROperation rop = new RScriptROperation(rscript, false);
    execute(rop);
    if (!rop.hasResult()) return;
    String[] files = rop.getResult().asStrings();
    Lists.newArrayList(files).forEach(file -> {
      FileReadROperation readop = new FileReadROperation(file, new File(getWorkspace(saveId), file));
      execute(readop);
    });
  }


  //
  // RASyncOperationTemplate
  //

  @Override
  public synchronized String executeAsync(ROperation rop) {
    touch();
    ensureRCommandsConsumer();
    String rCommandId = getId() + "-" + commandId++;
    RCommand cmd = new RCommand(rCommandId, rop);
    rCommandList.add(cmd);
    rCommandQueue.offer(cmd);
    return rCommandId;
  }

  @Override
  public Iterable<RCommand> getRCommands() {
    touch();
    return rCommandList;
  }

  @Override
  public boolean hasRCommand(String cmdId) {
    touch();
    for (RCommand rCommand : rCommandList) {
      if (rCommand.getId().equals(cmdId)) return true;
    }
    return false;
  }

  @Override
  public RCommand getRCommand(String cmdId) {
    touch();
    for (RCommand rCommand : rCommandList) {
      if (rCommand.getId().equals(cmdId)) return rCommand;
    }
    throw new NoSuchRCommandException(cmdId);
  }

  @Override
  public RCommand removeRCommand(String cmdId) {
    touch();
    RCommand rCommand = getRCommand(cmdId);
    synchronized (rCommand) {
      rCommand.notifyAll();
    }
    rCommandList.remove(rCommand);
    return rCommand;
  }

  //
  // Protected methods
  //

  protected synchronized void setBusy(boolean busy) {
    this.busy = busy;
  }

  /**
   * Get the workspaces directory for the current execution context.
   *
   * @return
   */
  protected File getWorkspaces() {
    return new File(String.format(String.format(OpalRSessionManager.WORKSPACES_FORMAT, getExecutionContext())));
  }


  //
  // Commands
  //

  protected void closeRCommandsQueue() {
    if (consumer == null) return;
    try {
      consumer.interrupt();
    } catch (Exception e) {
      // ignore
    } finally {
      consumer = null;
      rCommandList.clear();
      rCommandQueue.clear();
    }
  }

  private void ensureRCommandsConsumer() {
    if (rCommandsConsumer == null) {
      rCommandsConsumer = new RCommandsConsumer();
      startRCommandsConsumer();
    } else if (consumer == null || !consumer.isAlive()) {
      startRCommandsConsumer();
    }
  }

  private void startRCommandsConsumer() {
    Subject owner = SessionDetachedSubject.asSessionDetachedSubject(SecurityUtils.getSubject());
    consumer = transactionalThreadFactory.newThread(owner.associateWith(rCommandsConsumer));
    consumer.setName("R Operations Consumer " + rCommandsConsumer);
    consumer.setPriority(Thread.NORM_PRIORITY);
    consumer.start();
  }

  private class RCommandsConsumer implements Runnable {

    @Override
    public void run() {
      log.debug("Starting R operations consumer");
      try {
        //noinspection InfiniteLoopStatement
        while (true) {
          consume(rCommandQueue.take());
        }
      } catch (InterruptedException ignored) {
        log.debug("Stopping R operations consumer");
      } catch (Exception e) {
        log.error("Error in R command consumer", e);
      }
    }

    private void consume(RCommand rCommand) {
      try {
        rCommand.inProgress();
        execute(rCommand.getROperation());
        rCommand.completed();
      } catch (Exception e) {
        log.error("Error when consuming R command: {}", e.getMessage(), e);
        rCommand.failed(e.getMessage());
      }
      synchronized (rCommand) {
        rCommand.notifyAll();
      }
    }
  }

}
