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
}
