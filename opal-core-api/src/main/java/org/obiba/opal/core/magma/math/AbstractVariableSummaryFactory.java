package org.obiba.opal.core.magma.math;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public abstract class AbstractVariableSummaryFactory<TVariableSummary extends VariableSummary>
    implements VariableSummaryFactory<TVariableSummary> {

  private Variable variable;

  private ValueTable table;

  private ValueSource valueSource;

  @Override
  @NotNull
  public Variable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  @NotNull
  public ValueTable getTable() {
    return table;
  }

  @Override
  public void setTable(ValueTable table) {
    this.table = table;
  }

  @NotNull
  @Override
  public ValueSource getValueSource() {
    return valueSource;
  }

  @Override
  public void setValueSource(ValueSource valueSource) {
    this.valueSource = valueSource;
  }

}
