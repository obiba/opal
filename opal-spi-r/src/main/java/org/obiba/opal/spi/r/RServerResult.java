/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r;

import java.util.List;

public interface RServerResult {

  /**
   * Get the length, if it makes sense for the type of data, returns -1 otherwise.
   *
   * @return
   */
  int length();

  /**
   * Check if the evaluation result is raw data.
   *
   * @return
   */
  boolean isRaw();

  /**
   * Get the evaluation result as an array of bytes.
   *
   * @return
   */
  byte[] asBytes();

  /**
   * Check if the evaluation result is an array of doubles.
   *
   * @return
   */
  boolean isNumeric();

  /**
   * Get the evaluation result as an array of doubles.
   *
   * @return
   */
  double[] asDoubles();

  /**
   * Check if the evaluation result is an array of integers.
   *
   * @return
   */
  boolean isInteger();

  /**
   * Get the evaluation result as an array of integers.
   *
   * @return
   */
  int[] asIntegers();

  /**
   * Get the evaluation result as a single logical.
   *
   * @return
   */
  boolean asLogical();

  /**
   * Get the JSON representation of the data.
   * @return
   */
  String asJSON();

  /**
   * Check wether the data is null or represents a null value.
   *
   * @return
   */
  boolean isNull();

  /**
   * Check if the evaluation result is an array of strings.
   *
   * @return
   */
  boolean isString();

  /**
   * Get the evaluation result as an array of strings.
   *
   * @return
   */
  String[] asStrings();

  /**
   * Get the evaluation result as a matrix of strings.
   *
   * @return
   */
  RMatrix<String> asStringMatrix();

  /**
   * Check whether it is a list (named or not).
   *
   * @return
   */
  boolean isList();

  /**
   * Get the evaluation result list of values.
   *
   * @return
   */
  List<RServerResult> asList();

  /**
   * Check if there are names associated to a list.
   *
   * @return
   */
  boolean isNamedList();

  /**
   * Get the evaluation result as a named list of results.
   *
   * @return
   */
  RNamedList<RServerResult> asNamedList();

  /**
   * Get whether there are NAs in a vector of data.
   *
   * @return
   */
  boolean[] isNA();

  /**
   * Get the data as a Java object.
   *
   * @return
   */
  Object asNativeJavaObject();

  /**
   * Check if there are names associated to the evaluation result.
   *
   * @return
   */
  boolean hasNames();

  /**
   * Get the names associated to the evaluation result.
   *
   * @return
   */
  String[] getNames();
}
