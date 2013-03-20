/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import org.obiba.magma.ValueTable;

/**
 * Manager of {@code ValueTable} indices.
 */
public interface IndexManager {

  /**
   * The name of the index manager that groups some table indices.
   * @return
   */
  String getName();


  /**
   * Get the table index.
   * @param valueTable
   * @return
   */
  ValueTableIndex getIndex(ValueTable valueTable);

  /**
   * Create a index synchronization task.
   * @param valueTable
   * @param index
   * @return
   */
  IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index);

  /**
   * Check if any indexation tasks can be started.
   *
   * @return
   */
  boolean isReady();

  /**
   * Check if a value table is to be indexed.
   * @param valueTable
   * @return
   */
  boolean isIndexable(ValueTable valueTable);
}
