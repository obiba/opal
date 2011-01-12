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
 *
 */
public class OpalROperationTemplate {

  RConnection connection;

  private OpalROperationTemplate() {
    super();
  }

  public boolean isConnected() {
    return connection.isConnected();
  }

  /**
   * Assign a string value to a symbol in R.
   * @param sym symbol
   * @param ct content
   * @see RConnection#assign(String, String)
   */
  public void assign(String sym, String ct) {
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
  public REXP execute(String script) {
    if(script == null) throw new IllegalArgumentException("R script cannot be null");

    REXP evaled;
    try {
      evaled = connection.eval("try({" + script + "})");
    } catch(RserveException e) {
      throw new RRuntimeException("Failed evaluating: " + script, e);
    }
    if(evaled.inherits("try-error")) {
      // Deal with an error
      throw new RRuntimeException("Error while evaluating: " + script);
    }

    return evaled;
  }

  //
  // Builder
  //

  public static OpalROperationTemplate create(RConnection connection) {
    if(connection == null) throw new IllegalArgumentException("R connection cannot be null");
    if(!connection.isConnected()) throw new IllegalArgumentException("R connection must not be closed");
    OpalROperationTemplate template = new OpalROperationTemplate();
    template.connection = connection;
    return template;
  }

}
