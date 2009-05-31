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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides an execution context for an <code>IOnyxDataInputStrategy</code> (specifically, for its
 * <code>prepare</code> and <code>terminate</code> methods). It contains information required by those methods.
 */
public class OnyxDataInputContext {
  //
  // Instance Variables
  //

  private String source;

  private Map<String, String> keyProviderArgs;

  //
  // Constructors
  //

  public OnyxDataInputContext() {
    keyProviderArgs = new HashMap<String, String>();
  }

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
   * Sets the value of an <code>IKeyProvider</code> argument.
   * 
   * @param argKey argument key
   * @param argValue argument value
   */
  public void setKeyProviderArg(String argKey, String argValue) {
    keyProviderArgs.put(argKey, argValue);
  }

  /**
   * Returns the value of the specified <code>IKeyProvider</code> argument.
   * 
   * @param argKey argument key
   * @return the argument value
   */
  public String getKeyProviderArg(String argKey) {
    return keyProviderArgs.get(argKey);
  }

  public Map<String, String> getKeyProviderArgs() {
    return new HashMap<String, String>(keyProviderArgs);
  }
}