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

import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.obiba.magma.VariableEntity;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RSession;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;

/**
 * Reference to a R session.
 */
public class OpalRSession implements ROperationTemplate, VariableEntitiesHolder {

  private static final Logger log = LoggerFactory.getLogger(OpalRSession.class);

  private final String id;

  private final Lock lock = new ReentrantLock();

  private RSession rSession;

  private SortedSet<VariableEntity> entities;

  /**
   * Build a R session reference from a R connection.
   *
   * @param connection
   */
  OpalRSession(RConnection connection) {
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

  @Override
  public boolean hasEntities() {
    return entities != null;
  }

  @Override
  public SortedSet<VariableEntity> getEntities() {
    if(entities == null) throw new IllegalStateException("call setEntities() first");
    return entities;
  }

  @Override
  public void setEntities(SortedSet<VariableEntity> entities) {
    if(this.entities != null) throw new IllegalStateException("cannot invoke setEntities() more than once.");
    this.entities = ImmutableSortedSet.copyOf(entities);
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

  /**
   * Executes a batch of {@code ROperation} within a single connection to the R environment.
   *
   * @param rops
   */
  @Override
  public void execute(Iterable<ROperation> rops) {
    RConnection connection = null;
    lock.lock();
    try {
      connection = newConnection();
      for(ROperation rop : rops) {
        rop.doWithConnection(connection);
      }
    } finally {
      lock.unlock();
      if(connection != null) close(connection);
    }
  }

  /**
   * Close the R session.
   */
  public void close() {
    newConnection().close();
    rSession = null;
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
    try {
      rSession = connection.detach();
    } catch(RserveException e) {
      log.error("Error while detaching R session.", e);
      throw new RRuntimeException(e);
    }
  }

}
