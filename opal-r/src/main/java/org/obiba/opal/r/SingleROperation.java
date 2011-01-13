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

/**
 * Does the evaluation of a R script and stores the result.
 */
public class SingleROperation extends AbstractROperation {

  private String script;

  private REXP result;

  public SingleROperation(String script) {
    super();
    if(script == null) throw new IllegalArgumentException("R script cannot be null");

    this.script = script;
  }

  /**
   * Set the R script to be evaluated.
   * @param script
   */
  public void setScript(String script) {
    this.script = script;
  }

  /**
   * Evaluates the provided R script.
   */
  @Override
  public void doWithConnection() {
    result = null;
    if(getConnection() == null) throw new IllegalStateException("R connection cannot be null");
    result = eval(script);
  }

  /**
   * Get the result of the last R script evaluation.
   * @return
   */
  public REXP getResult() {
    return result;
  }

}
