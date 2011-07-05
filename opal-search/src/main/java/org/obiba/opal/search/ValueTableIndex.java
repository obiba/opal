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

import org.obiba.magma.Timestamped;

/**
 * An index of a {@code ValueTable}
 */
public interface ValueTableIndex extends Timestamped {

  /**
   * The full path of this index to make requests. TODO: this should be hidden in the implementation. Probably we should
   * expose some sort of Request/Response api?
   * @return
   */
  public String getRequestPath();

  public String getName();

  public void delete();

}
