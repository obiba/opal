/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.RASyncOperationTemplate;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RSession;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Reference to a R session.
 */
public class OpalRSession implements RASyncOperationTemplate {

  private static final Logger log = LoggerFactory.getLogger(OpalRSession.class);

  private final TransactionalThreadFactory transactionalThreadFactory;

  private final String id;

  private final Lock lock = new ReentrantLock();

  private RSession rSession;

  /**
   * R commands to be processed.
   */
  private final BlockingQueue<RCommand> rCommandQueue = new LinkedBlockingQueue<>();

  /**
   * All R commands.
   */
  private final List<RCommand> rCommandList = Collections.synchronizedList(new LinkedList<RCommand>());

  private RCommandsConsumer rCommandsConsumer;

  private Thread consumer;

  /**
   * R command identifier increment.
   */
  private int commandId = 1;

  /**
   * Build a R session reference from a R connection.
   *
   * @param connection
   */
  OpalRSession(RConnection connection, TransactionalThreadFactory transactionalThreadFactory) {
    this.transactionalThreadFactory = transactionalThreadFactory;
    try {
      rSession = connection.detach();
    } catch(RserveException e) {
      log.error("Error while detaching R session.", e);
      throw new RRuntimeException(e);
    }
    id = UUID.randomUUID().toString();
  }

  /**
   * Get the unique identifier of the R session.
   *
   * @return
   */
  public String getId() {
    return id;
  }

  //
  // ROperationTemplate methods
  //

  /**
   * Executes the R operation on the current R session of the invoking Opal user. If no current R session is defined, a
   * {@link NoSuchRSessionException} is thrown.
   *
   * @see #hasSubjectCurrentRSession(), {@link #setSubjectCurrentRSession(String)}
   */
  @Override
  public void execute(ROperation rop) {
    RConnection connection = null;
    lock.lock();
    try {
      connection = newConnection();
      rop.doWithConnection(connection);
    } finally {
      lock.unlock();
      if(connection != null) close(connection);
    }
  }

  @Override
  public synchronized String executeAsync(ROperation rop) {
    ensureRCommandsConsumer();
    String rCommandId = id + "-" + commandId++;
    RCommand cmd = new RCommand(rCommandId, rop);
    rCommandList.add(cmd);
    rCommandQueue.offer(cmd);
    return rCommandId;
  }

  @Override
  public Iterable<RCommand> getRCommands() {
    return rCommandList;
  }

  @Override
  public boolean hasRCommand(String cmdId) {
    for(RCommand rCommand : rCommandList) {
      if(rCommand.getId().equals(cmdId)) return true;
    }
    return false;
  }

  @Override
  public RCommand getRCommand(String cmdId) {
    for(RCommand rCommand : rCommandList) {
      if(rCommand.getId().equals(cmdId)) return rCommand;
    }
    throw new NoSuchRCommandException(cmdId);
  }

  @Override
  public RCommand removeRCommand(String cmdId) {
    RCommand rCommand = getRCommand(cmdId);
    synchronized(rCommand) {
      rCommand.notifyAll();
    }
    rCommandList.remove(rCommand);
    return rCommand;
  }

  /**
   * Close the R session.
   */
  public void close() {
    if(rSession == null) return;

    try {
      newConnection().close();
    } catch(Exception e) {
      // ignore
    } finally {
      rSession = null;
    }

    if(consumer == null) return;
    try {
      consumer.interrupt();
    } catch(Exception e) {
      // ignore
    } finally {
      consumer = null;
      rCommandList.clear();
      rCommandQueue.clear();
    }
  }

  //
  // private methods
  //

  /**
   * Creates a new R connection from the last R session state.
   *
   * @return
   */
  private RConnection newConnection() {
    if(rSession == null) throw new NoSuchRSessionException();
    try {
      return rSession.attach();
    } catch(RserveException e) {
      log.error("Error while attaching R session.", e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Detach the R connection and updates the R session.
   *
   * @param connection
   */
  private void close(RConnection connection) {
    if(connection == null) return;
    if (!Strings.isNullOrEmpty(connection.getLastError()) && !connection.getLastError().toLowerCase().equals("ok")) {
      throw new RRuntimeException("Unexpected R server error: " + connection.getLastError());
    }
    try {
      rSession = connection.detach();
    } catch(RserveException e) {
      log.warn("Error while detaching R session.", e);
    }
  }

  private void ensureRCommandsConsumer() {
    if(rCommandsConsumer == null) {
      rCommandsConsumer = new RCommandsConsumer();
      startRCommandsConsumer();
    } else if(consumer == null || !consumer.isAlive()) {
      startRCommandsConsumer();
    }
  }

  private void startRCommandsConsumer() {
    consumer = transactionalThreadFactory.newThread(rCommandsConsumer);
    consumer.setName("R Operations Consumer " + rCommandsConsumer);
    consumer.start();
  }

  private class RCommandsConsumer implements Runnable {

    @Override
    public void run() {
      log.debug("Starting R operations consumer");
      try {
        //noinspection InfiniteLoopStatement
        while(true) {
          consume(rCommandQueue.take());
        }
      } catch(InterruptedException ignored) {
        log.debug("Stopping R operations consumer");
      } catch(Exception e) {
        log.error("Error in R command consumer", e);
      }
    }

    private void consume(RCommand rCommand) {
      try {
        rCommand.inProgress();
        execute(rCommand.getROperation());
        rCommand.completed();
      } catch(Exception e) {
        log.error("Error when consuming R command: {}", e.getMessage(), e);
        rCommand.failed(e.getMessage());
      }
      synchronized(rCommand) {
        rCommand.notifyAll();
      }
    }
  }

}
