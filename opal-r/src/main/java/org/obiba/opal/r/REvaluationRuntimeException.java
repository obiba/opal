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

import java.util.Arrays;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when a R try-error statement fails.
 */
public class REvaluationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(REvaluationRuntimeException.class);

  private REXP result;

  public REvaluationRuntimeException(String msg, REXP result) {
    super(msg);
    this.result = result;
  }

  public REXP getResult() {
    return result;
  }

  public List<String> getRMessages() {
    String[] strs = null;
    try {
      if(result != null) strs = result.asStrings();
    } catch(REXPMismatchException e) {
      log.error("Not a REXP with strings", e);
    }
    if(strs == null) strs = new String[] { };

    return Arrays.asList(strs);
  }

}
