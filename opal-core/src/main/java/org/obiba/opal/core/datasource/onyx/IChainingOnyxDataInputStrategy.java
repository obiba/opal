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
 * <code>IChainingOnyxDataInputStrategy</code> is a "chaining" interface. An <code>IOnyxDataInputStrategy</code>
 * that delegates to (or depends on) another strategy should implement this interface.
 */
public interface IChainingOnyxDataInputStrategy extends IOnyxDataInputStrategy {

  /**
   * Sets the strategy's "delegate."
   * 
   * @param delegate the delegate
   */
  public void setDelegate(IOnyxDataInputStrategy delegate);
}
