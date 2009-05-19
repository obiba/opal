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
 * Signals that the <code>KeyPair</code> requested of <code>IKeyProvider</code> does not exist in, or is not
 * available to, that <code>IKeyProvider</code>.
 */
public class KeyPairNotFoundException extends KeyProviderException {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public KeyPairNotFoundException(String message) {
    super(message);
  }
}
