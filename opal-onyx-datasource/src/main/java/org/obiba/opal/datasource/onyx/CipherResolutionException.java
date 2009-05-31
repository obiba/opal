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
 * General cipher resolution exception.
 * 
 * This is a wrapper around various problems that may be encountered when attempting to obtain a <code>Cipher</code>
 * instance. For instance:
 * <ul>
 * <li>NoSuchAlgorithmException</li>
 * <li>NoSuchPaddingException</li>
 * <li>InvalidKeyException</li>
 * <li>InvalidAlgorithmParameterException</li>
 * </ul>
 */
public class CipherResolutionException extends Exception {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public CipherResolutionException(Throwable cause) {
    super(cause);
  }
}
