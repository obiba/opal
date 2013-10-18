package org.obiba.opal.core.magma.math;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public abstract class AbstractVariableSummaryFactory {

  private Variable variable;

  private ValueTable table;

  private ValueSource valueSource;

  public abstract String getCacheKey();

  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  public ValueTable getTable() {
    return table;
  }

  public void setTable(ValueTable table) {
    this.table = table;
  }

  public ValueSource getValueSource() {
    return valueSource;
  }

  public void setValueSource(ValueSource valueSource) {
    this.valueSource = valueSource;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static abstract class Builder<TFactory extends AbstractVariableSummaryFactory, TBuilder extends Builder<TFactory, TBuilder>> {

    protected final TFactory factory;

    protected final TBuilder builder;

    protected Builder() {
      factory = createFactory();
      builder = createBuilder();
    }

    protected abstract TFactory createFactory();

    protected abstract TBuilder createBuilder();

    public TBuilder variable(Variable variable) {
      factory.setVariable(variable);
      return builder;
    }

    public TBuilder table(ValueTable table) {
      factory.setTable(table);
      return builder;
    }

    public TBuilder valueSource(ValueSource valueSource) {
      factory.setValueSource(valueSource);
      return builder;
    }

  }
}
