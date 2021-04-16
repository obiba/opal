/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

/**
 * Provides a R vector from a Magma fully qualified path.
 */
public interface MagmaRConverter {

  /**
   * Perform the assignment.
   *
   * @param symbol
   * @param path
   */
  void doAssign(String symbol, String path);

}
