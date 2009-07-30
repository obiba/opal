/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.crypt;

import javax.security.auth.callback.PasswordCallback;

public class CacheablePasswordCallback extends PasswordCallback {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  private String passwordKey;

  //
  // Constructors
  //

  public CacheablePasswordCallback(String passwordKey, String prompt, boolean echoOn) {
    super(prompt, echoOn);

    this.passwordKey = passwordKey;
  }

  //
  // Methods
  //

  /**
   * The password's "key" (i.e., the key used to cache it).
   * 
   * @return password key
   */
  public String getPasswordKey() {
    return passwordKey;
  }
}
