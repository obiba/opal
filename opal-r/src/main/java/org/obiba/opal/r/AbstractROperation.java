/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a R connection and provides some utility methods to handle operations on it.
 */
public abstract class AbstractROperation implements ROperation {

  private static final Logger log = LoggerFactory.getLogger(AbstractROperation.class);

  RConnection connection;

  /**
   * Check if connection is still operational.
   *
   * @return
   */
  protected boolean isConnected() {
    return connection.isConnected();
  }

  /**
   * Assign a string value to a symbol in R.
   *
   * @param sym symbol
   * @param ct content
   * @see RConnection#assign(String, String)
   */
  protected void assign(String sym, String ct) {
    try {
      connection.assign(sym, ct);
    } catch(RserveException e) {
      log.warn("Failed assigning '{}' with: {}", sym, ct, e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Assign a REXP object to a symbol in R.
   *
   * @param sym
   * @param ct
   */
  protected void assign(String sym, REXP ct) {
    try {
      connection.assign(sym, ct);
    } catch(RserveException e) {
      log.warn("Failed assigning '{}' with REXP", sym, e);
      throw new RRuntimeException(e);
    }
  }

  /**
   * Safe evaluation of a R script.
   *
   * @param script
   * @return result serialized
   */
  protected REXP eval(String script) {
    return eval(script, true);
  }

  /**
   * Safe evaluation of a R script with result optionally serialized.
   *
   * @param script
   * @param serialize
   * @return
   */
  protected REXP eval(String script, boolean serialize) {
    if(script == null) throw new IllegalArgumentException("R script cannot be null");

    REXP evaled;
    try {
      log.debug("evaluating {}", script);
      String cmd = script;
      if(serialize) {
        cmd = "serialize({" + script + "}, NULL)";
      }
      evaled = connection.eval("try(" + cmd + ")");
    } catch(RserveException e) {
      log.warn("Failed evaluating: {}", script, e);
      throw new RRuntimeException(e);
    }
    if(evaled.inherits("try-error")) {
      // Deal with an error
      throw new REvaluationRuntimeException("Error while evaluating '" + script + "'", evaled);
    }

    return evaled;
  }

  /**
   * Get the current R connection.
   *
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
  protected abstract void doWithConnection();

}
