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
