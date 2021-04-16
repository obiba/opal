/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;

public class ValueTableRenamedEvent extends ValueTableEvent {

  private final String newName;

  public ValueTableRenamedEvent(ValueTable table, String newName) {
    super(table);
    this.newName = newName;
  }

  public String getNewName() {
    return newName;
  }
}
