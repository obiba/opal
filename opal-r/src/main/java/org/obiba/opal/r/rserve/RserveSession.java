/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.rserve;

import com.google.common.base.Strings;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.AbstractRServerSession;
import org.obiba.opal.r.service.NoSuchRSessionException;
import org.obiba.opal.spi.r.ROperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Reference to a Rserve session.
 */
class RserveSession extends AbstractRServerSession {

  private static final Logger log = LoggerFactory.getLogger(RserveSession.class);

  private final RserveConnection rConnection;

  private boolean closed = false;

  /**
   * Build a R session reference from a R connection.
   *
   * @param connection
   */
  RserveSession(RserveConnection connection, TransactionalThreadFactory transactionalThreadFactory, String user) {
    super(UUID.randomUUID().toString(), user, transactionalThreadFactory);
    this.rConnection = connection;
    initDirectories();
  }

  @Override
  public String toString() {
    return getId();
  }

  //
  // ROperationTemplate methods
  //

  /**
   * Executes the R operation on the current R session of the invoking Opal user. If no current R session is defined, a
   * {@link NoSuchRSessionException} is thrown.
   */
  @Override
  public synchronized void execute(ROperation rop) {
    lock.lock();
    setBusy(true);
    touch();
    try {
      rop.doWithConnection(rConnection);
    } finally {
      setBusy(false);
      touch();
      lock.unlock();
    }
  }

  /**
   * Close the R session.
   */
  @Override
  public void close() {
    if (isClosed()) return;
    cleanDirectories();
    closeConnection();
    closeRCommandsQueue();
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  //
  // private methods
  //

  /**
   * Close the Rserve connection.
   */
  private void closeConnection() {
    if (!Strings.isNullOrEmpty(rConnection.getLastError()) && !rConnection.getLastError().equalsIgnoreCase("ok")) {
      log.warn("Unexpected R server error: " + rConnection.getLastError());
    }
    try {
      rConnection.close();
      closed = true;
    } catch (Exception e) {
      log.warn("Error while closing R connection.", e);
    }
  }

}
