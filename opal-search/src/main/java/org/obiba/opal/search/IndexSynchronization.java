/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search;

import org.obiba.magma.ValueTable;

/**
 * A {@code Runnable} that will synchronize an index when its {@code run} method is invoked.
 * <p>
 * Provides syncrhonization state through other methods.
 */
public interface IndexSynchronization extends Runnable {

  public ValueTableIndex getValueTableIndex();

  public ValueTable getValueTable();

  public boolean hasStarted();

  public boolean isComplete();

  /**
   * A value between 0 and 1.
   */
  public float getProgress();

}
