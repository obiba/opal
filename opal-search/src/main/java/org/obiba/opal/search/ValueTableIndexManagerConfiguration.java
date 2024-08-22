/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import org.obiba.magma.ValueTable;
import org.obiba.opal.search.service.ValueTableIndex;

public interface ValueTableIndexManagerConfiguration {

  /**
   * Get from the Index manager configuration whether a given value table is ready for indexing by comparing
   * the last update of the table with the last update of the index (and a grace period).
   */
  boolean isReadyForIndexing(ValueTable vt, ValueTableIndex index);

  boolean isEnabled();

  void setEnabled(boolean enabled);

  void updateSchedule(ValueTable vt, Schedule schedule);

  void removeSchedule(ValueTable vt);

  Schedule getSchedule(ValueTable vt);

}
