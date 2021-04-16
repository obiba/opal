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

public class VariablesUpdatedEvent extends ValueTableEvent {

  private final Iterable<Variable> variables;

  public VariablesUpdatedEvent(ValueTable valueTable, Iterable<Variable> variables) {
    super(valueTable);
    this.variables = variables;
  }

  public Iterable<Variable> getVariables() {
    return variables;
  }
}
