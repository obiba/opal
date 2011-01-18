/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r.service;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RSession;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * Reference to a R session.
 */
public class OpalRSession {

  private String id;

  private RSession rSession;

  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Build a R session reference from a R connection.
   * @param connection
   */
  public OpalRSession(RConnection connection) {
    super();
    try {
      this.rSession = connection.detach();
    } catch(RserveException e) {
      throw new RRuntimeException(e);
    }
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Get the unique identifier of the R session.
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * Creates a new R connection from the last R session state.
   * @return
   */
  public RConnection newConnection() {
    try {
      return rSession.attach();
    } catch(RserveException e) {
      throw new RRuntimeException(e);
    }
  }

  /**
   * Close the R session.
   */
  public void close() {
    newConnection().close();
    rSession = null;
  }

  /**
   * Detach the R connection and updates the R session.
   * @param connection
   */
  public void close(RConnection connection) {
    if(connection == null) return;
    try {
      rSession = connection.detach();
    } catch(RserveException e) {
      throw new RRuntimeException("Failed detaching connection of R session: " + id, e);
    }
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

}
