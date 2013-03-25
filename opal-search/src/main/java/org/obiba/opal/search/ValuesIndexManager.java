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

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;

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
  @Nonnull
  @Override
  ValueTableValuesIndex getIndex(@Nonnull ValueTable valueTable);

}
