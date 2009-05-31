/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

/**
 * Signals missing encryption metadata. These metadata are required in order to decrypt data.
 */
public class EncryptionDataMissingException extends RuntimeException {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public EncryptionDataMissingException(Throwable cause) {
    super(cause);
  }

  public EncryptionDataMissingException(String message) {
    super(message);
  }
}
