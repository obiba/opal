package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class VariableDeletedEvent extends ValueTableEvent {

  private final Variable variable;

  public VariableDeletedEvent(ValueTable table, Variable variable) {
    super(table);
    this.variable = variable;
  }

  public Variable getVariable() {
    return variable;
  }
}
