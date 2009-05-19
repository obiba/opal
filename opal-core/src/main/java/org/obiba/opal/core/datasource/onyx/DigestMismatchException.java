/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.datasource.onyx;

/**
 * Signals a mismatch of data with its digest.
 */
public class DigestMismatchException extends RuntimeException {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  public DigestMismatchException(Throwable cause) {
    super(cause);
  }

  public DigestMismatchException(String message) {
    super(message);
  }
}
