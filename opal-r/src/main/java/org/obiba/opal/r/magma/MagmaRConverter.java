/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import org.rosuda.REngine.REXP;

/**
 * Provides a R vector from a Magma fully qualified path.
 */
public interface MagmaRConverter {

  /**
   * Build a R vector from the Magma fully-qualified path.
   *
   * @param path
   * @param withMissings
   * @param identifiersMapping
   * @return
   */
  REXP asVector(String path, boolean withMissings, String identifiersMapping);

  /**
   * Check if path can be resolved as a datasource, table or variable.
   *
   * @param path
   * @return
   */
  boolean canResolve(String path);

  void doAssign(String symbol, String path, boolean withMissings, String identifiersMapping);

}
