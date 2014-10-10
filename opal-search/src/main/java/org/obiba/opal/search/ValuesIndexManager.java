/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;

import java.util.List;

/**
 * Manager of {@code ValueTable} values indices.
 */
public interface ValuesIndexManager extends IndexManager {

  /**
   * Get {@code ValueTable} values index.
   *
   * @param valueTable
   * @return
   */
  @NotNull
  @Override
  ValueTableValuesIndex getIndex(@NotNull ValueTable valueTable);

    /**
     * Returns the messages of the last attempt (successful or not) to index the given ValueTable
     * @param valueTable
     * @return
     */
  List<Message> getMessages(@NotNull ValueTable valueTable);

}
