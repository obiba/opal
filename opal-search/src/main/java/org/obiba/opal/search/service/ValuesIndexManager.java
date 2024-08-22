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
 * Manager of {@code ValueTable} values indices.
 */
public interface ValuesIndexManager extends IndexManager {

  String FIELD_SEP = "__";

  /**
   * Get {@code ValueTable} values index.
   *
   * @param valueTable
   * @return
   */
  @Override
  ValueTableValuesIndex getIndex(ValueTable valueTable);

}
