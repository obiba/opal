/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource;

import org.obiba.magma.*;

import java.util.List;

/**
 * Establishes the connection with a resource and performs basic operations on its tabular representation.
 */
public interface TabularResourceConnector extends Initialisable, Disposable {

  /**
   * Get the (R) symbol name that refers to the tabular representation of the resource.
   *
   * @return
   */
  String getSymbol();

  /**
   * Get all columns.
   *
   * @return
   */
  List<Column> getColumns();

  /**
   * Check there is a column with given nale.
   *
   * @param name
   * @return
   */
  boolean hasColumn(String name);

  /**
   * Get column by name.
   *
   * @param name
   * @return
   */
  Column getColumn(String name);

  /**
   * Get whether there are multiple lines per entity.
   *
   * @param idColumn
   * @return
   */
  boolean isMultilines(String idColumn);

  interface Column {

    /**
     * Get column name.
     *
     * @return
     */
    String getName();

    /**
     * Get column position.
     *
     * @return
     */
    int getPosition();

    /**
     * Get the column's vector length.
     *
     * @param distinct
     * @return
     */
    int getLength(boolean distinct);

    /**
     * Get the column's vector of values, sliced.
     *
     * @param valueType
     * @param offset
     * @param limit
     * @return
     */
    List<Value> asVector(ValueType valueType, boolean distinct, int offset, int limit);

    /**
     * Get the column's vector of values for entities.
     *
     * @param valueType
     * @param idColumn
     * @param entities
     * @return
     */
    List<Value> asVector(ValueType valueType, String idColumn, Iterable<VariableEntity> entities);

    /**
     * Get the column as a variable object.
     *
     * @param entityType
     * @param multilines
     * @return
     */
    Variable asVariable(String entityType, boolean multilines);
  }
}
