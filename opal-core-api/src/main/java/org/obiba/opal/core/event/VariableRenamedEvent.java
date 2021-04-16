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
import org.obiba.magma.Variable;

public class VariableRenamedEvent extends ValueTableEvent {

  private final Variable variable;

  private final String newName;

  public VariableRenamedEvent(ValueTable table, Variable variable, String newName) {
    super(table);
    this.variable = variable;
    this.newName = newName;
  }

  public Variable getVariable() {
    return variable;
  }

  public String getNewName() {
    return newName;
  }
}
