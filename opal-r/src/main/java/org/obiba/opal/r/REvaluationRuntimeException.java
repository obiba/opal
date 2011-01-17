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
 * Exception thrown when a R try-error statement fails.
 */
public class REvaluationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private REXP result;

  public REvaluationRuntimeException(String msg, REXP result) {
    super(msg);
    this.result = result;
  }

  public REXP getResult() {
    return result;
  }
}
