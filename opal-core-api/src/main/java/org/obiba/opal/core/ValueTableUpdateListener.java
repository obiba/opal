/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

import javax.validation.constraints.NotNull;

public interface ValueTableUpdateListener extends org.obiba.magma.ValueTableUpdateListener {

  /**
   * Called when variables are updated.
   *
   * @param vt
   * @param vs
   */
  void onUpdate(@NotNull ValueTable vt, Iterable<Variable> vs);

}
