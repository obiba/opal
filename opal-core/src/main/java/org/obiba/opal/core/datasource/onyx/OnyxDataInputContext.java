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
 * Provides an execution context for an <code>IOnyxDataInputStrategy</code> (specifically, for its
 * <code>prepare</code> and <code>terminate</code> methods). It contains information required by those methods.
 */
public class OnyxDataInputContext {
  //
  // Instance Variables
  //

  private String source;

  private String keyProviderArgs;

  //
  // Methods
  //

  /**
   * Indicates the source of the data. This could be, for example, a file path.
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * Returns the source of the data.
   * 
   * @return data source
   */
  public String getSource() {
    return source;
  }

  /**
   * Indicates any arguments required by the <code>KeyProvider</code>.
   * 
   * Arguments are provided simply as a string. The format of this <code>String</code> is provider-dependent.
   * 
   * @param keyProviderArgs key provider arguments (in the case of a keystore, this could be the keystore's password)
   */
  public void setKeyProviderArgs(String keyProviderArgs) {
    this.keyProviderArgs = keyProviderArgs;
  }

  /**
   * Returns the <code>KeyProvider</code>'s argument string.
   * 
   * @return key provider arguments
   */
  public String getKeyProviderArgs() {
    return keyProviderArgs;
  }
}