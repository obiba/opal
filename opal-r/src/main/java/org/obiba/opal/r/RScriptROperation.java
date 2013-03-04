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

/**
 * Does the evaluation of a R script and stores the result.
 */
public class RScriptROperation extends AbstractROperationWithResult {

  private final String script;

  private final boolean serialize;

  public RScriptROperation(String script) {
    this(script, true);
  }

  public RScriptROperation(String script, boolean serialize) {
    if(script == null) throw new IllegalArgumentException("R script cannot be null");
    this.script = script;
    this.serialize = serialize;
  }

  /**
   * Evaluates the provided R script.
   */
  @Override
  public void doWithConnection() {
    setResult(null);
    setResult(eval(script, serialize));
  }
}
