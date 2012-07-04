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

public interface IndexManager {

  public boolean isIndexable(ValueTable valueTable);

  public ValueTableIndex getIndex(ValueTable valueTable);

  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index);

}
