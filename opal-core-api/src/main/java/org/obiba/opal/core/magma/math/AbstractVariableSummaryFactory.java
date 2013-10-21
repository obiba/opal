package org.obiba.opal.core.magma.math;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public abstract class AbstractVariableSummaryFactory<TVariableSummary extends VariableSummary>
    implements VariableSummaryFactory<TVariableSummary> {

  private Variable variable;

  private ValueTable table;

  private ValueSource valueSource;

  @Override
  @Nonnull
  public Variable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  @Nonnull
  public ValueTable getTable() {
    return table;
  }

  @Override
  public void setTable(ValueTable table) {
    this.table = table;
  }

  @Nonnull
  @Override
  public ValueSource getValueSource() {
    return valueSource;
  }

  @Override
  public void setValueSource(ValueSource valueSource) {
    this.valueSource = valueSource;
  }

}
