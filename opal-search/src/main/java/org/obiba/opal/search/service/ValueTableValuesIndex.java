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

import org.obiba.magma.Variable;

/**
 * An index of a {@code ValueTable} values.
 */
public interface ValueTableValuesIndex extends ValueTableIndex {

  /**
   * Name of the field for the given variable.
   *
   * @param variable
   * @return
   */
  String getFieldName(Variable variable);

  /**
   * Name of the field for the given variable name.
   *
   * @param variableName
   * @return
   */
  String getFieldName(String variableName);

  /**
   * Get the variables being indexed.
   *
   * @return
   */
  Iterable<Variable> getVariables();

}
