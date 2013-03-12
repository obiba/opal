/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield.expr;

public class InvalidScriptException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidScriptException() {
    super();
  }

  public InvalidScriptException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidScriptException(String message) {
    super(message);
  }

  public InvalidScriptException(Throwable cause) {
    super(cause);
  }

}
