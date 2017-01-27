/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPRaw;

import java.util.NoSuchElementException;

/**
 * Implements ROperationWithResult with local result storage. Overrides {@code #eval(String)} and saves the returned
 * value as the result of the {@code ROperationWithResult}
 */
public abstract class AbstractROperationWithResult extends AbstractROperation implements ROperationWithResult {

  private REXP result;

  private boolean ignoreResult;

  public void setIgnoreResult(boolean ignoreResult) {
    this.ignoreResult = ignoreResult;
  }

  public boolean isIgnoreResult() {
    return ignoreResult;
  }

  @Override
  public REXP getResult() {
    if(!hasResult()) throw new NoSuchElementException();
    return result;
  }

  @Override
  public boolean hasResult() {
    return !isIgnoreResult() && result != null;
  }

  @Override
  public boolean hasRawResult() {
    return !isIgnoreResult() && result != null && result.isRaw();
  }

  @Override
  public REXPRaw getRawResult() {
    if(!hasResult()) throw new NoSuchElementException();
    if(!hasRawResult()) throw new NoSuchElementException();
    return (REXPRaw) result;
  }

  @Override
  protected REXP eval(String script) {
    return result = super.eval(script);
  }

  @Override
  protected REXP eval(String script, boolean serialize) {
    return result = super.eval(script, serialize);
  }

  protected void setResult(REXP result) {
    this.result = result;
  }

}
