/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.service;

import org.obiba.magma.ValueTable;

/**
 * Manager of {@code ValueTable} indices.
 */
public interface IndexManager {

  /**
   * The name of the index manager that groups some table indices.
   *
   * @return
   */
  String getName();

  /**
   * Get the table index.
   *
   * @param valueTable
   * @return
   */
  ValueTableIndex getIndex(ValueTable valueTable);

  /**
   * Create a index synchronization task.
   *
   * @param valueTable
   * @param index
   * @return
   */
  IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index);

  /**
   * Create a query executor for this index.
   *
   * @return
   */
  SearchQueryExecutor createQueryExecutor();

  /**
   * Check if this index manager is enabled.
   *
   * @return
   */
  boolean isEnabled();

  /**
   * Check if any indexation tasks can be started.
   *
   * @return
   */
  boolean isReady();

  /**
   * Drop the index for all the tables.
   *
   */
  void drop();

  /**
   * Drop the index for a table.
   *
   */
  void drop(ValueTable valueTable);

  /**
   * Returns true of value table is indexed and index is up to date, ie ready to be queried.
   *
   * @param valueTable
   * @return
   */
  boolean isIndexUpToDate(ValueTable valueTable);

  /**
   * Returns true if the given table has an index. In the ES paradigm, this refers to a index type.
   *
   * @return
   */
  boolean hasIndex(ValueTable valueTable);
}
