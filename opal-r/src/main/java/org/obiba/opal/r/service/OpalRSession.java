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

import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.obiba.magma.VariableEntity;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RSession;
import org.rosuda.REngine.Rserve.RserveException;

import com.google.common.collect.ImmutableSortedSet;

/**
 * Reference to a R session.
 */
public class OpalRSession implements ROperationTemplate, VariableEntitiesHolder {

  private final String id;

  private final ReentrantLock lock = new ReentrantLock();

  private RSession rSession;

  private SortedSet<VariableEntity> entities;

  /**
   * Build a R session reference from a R connection.
   * @param connection
   */
  OpalRSession(RConnection connection) {
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

  @Override
  public boolean hasEntities() {
    return entities != null;
  }

  public SortedSet<VariableEntity> getEntities() {
    if(entities == null) throw new IllegalStateException("call setEntities() first");
    return entities;
  }

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
   * @see #hasSubjectCurrentRSession(), {@link #setSubjectCurrentRSession(String)}
   */
  @Override
  public void execute(ROperation rop) {
    lock.lock();
    RConnection connection = null;

    try {
      connection = newConnection();
      rop.doWithConnection(connection);
    } finally {
      if(connection != null) close(connection);
      lock.unlock();
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
   * @return
   */
  private RConnection newConnection() {
    try {
      return rSession.attach();
    } catch(RserveException e) {
      throw new RRuntimeException(e);
    }
  }

  /**
   * Detach the R connection and updates the R session.
   * @param connection
   */
  private void close(RConnection connection) {
    if(connection == null) return;
    try {
      rSession = connection.detach();
    } catch(RserveException e) {
      throw new RRuntimeException("Failed detaching connection of R session: " + id, e);
    }
  }

}
