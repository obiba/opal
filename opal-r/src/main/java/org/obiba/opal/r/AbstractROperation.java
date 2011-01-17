/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * Handles a R connection and provides some utility methods to handle operations on it.
 */
public abstract class AbstractROperation implements ROperation {

  RConnection connection;

  /**
   * Check if connection is still operational.
   * @return
   */
  protected boolean isConnected() {
    return connection.isConnected();
  }

  /**
   * Assign a string value to a symbol in R.
   * @param sym symbol
   * @param ct content
   * @see RConnection#assign(String, String)
   */
  protected void assign(String sym, String ct) {
    try {
      connection.assign(sym, ct);
    } catch(RserveException e) {
      throw new RRuntimeException("Failed assigning '" + sym + "' with: " + ct);
    }
  }

  /**
   * Safe evaluation of a R script.
   * @param script
   * @return result
   */
  protected REXP eval(String script) {
    if(script == null) throw new IllegalArgumentException("R script cannot be null");

    REXP evaled;
    try {
      evaled = connection.eval("try(serialize({" + script + "}, NULL))");
    } catch(RserveException e) {
      throw new RRuntimeException("Failed evaluating: " + script, e);
    }
    if(evaled.inherits("try-error")) {
      // Deal with an error
      throw new REvaluationRuntimeException("Error while evaluating '" + script + "'", evaled);
    }

    return evaled;
  }

  /**
   * Get the current R connection.
   * @return
   */
  protected RConnection getConnection() {
    return connection;
  }

  /**
   * Set the R connection to make it available for operations.
   */
  @Override
  public void doWithConnection(RConnection connection) {
    if(connection == null) throw new IllegalArgumentException("R connection cannot be null");
    this.connection = connection;
    doWithConnection();
  }

  /**
   * Does anything with the current R connection.
   */
  public abstract void doWithConnection();

}
