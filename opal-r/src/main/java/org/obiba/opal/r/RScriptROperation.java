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

import java.util.NoSuchElementException;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPRaw;

/**
 * Does the evaluation of a R script and stores the result.
 */
public class RScriptROperation extends AbstractROperation implements ROperationWithResult {

  private final String script;

  private REXP result;

  public RScriptROperation(String script) {
    super();
    if(script == null) throw new IllegalArgumentException("R script cannot be null");
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
    if(!hasResult()) throw new NoSuchElementException();
    return result;
  }

  @Override
  public boolean hasResult() {
    return result != null;
  }

  @Override
  public boolean hasRawResult() {
    return result != null && result.isRaw();
  }

  @Override
  public REXPRaw getRawResult() {
    if(!hasResult()) throw new NoSuchElementException();
    if(!hasRawResult()) throw new NoSuchElementException();
    return (REXPRaw) result;
  }

}
