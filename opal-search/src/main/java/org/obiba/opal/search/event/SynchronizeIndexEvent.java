/*
 * Copyright (c) 2023 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.event;

import org.obiba.magma.ValueTable;
import org.obiba.opal.search.service.IndexManager;

public class SynchronizeIndexEvent {

  private final IndexManager indexManager;

  private final ValueTable valueTable;

  public SynchronizeIndexEvent(IndexManager indexManager, ValueTable valueTable) {
    this.indexManager = indexManager;
    this.valueTable = valueTable;
  }

  public IndexManager getIndexManager() {
    return indexManager;
  }

  public ValueTable getValueTable() {
    return valueTable;
  }
}
