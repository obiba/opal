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
