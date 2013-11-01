package org.obiba.opal.core.magma.math;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface VariableSummaryFactory<TVariableSummary extends VariableSummary> {

  @NotNull
  TVariableSummary getSummary();

  @NotNull
  String getCacheKey();

  @NotNull
  Variable getVariable();

  @NotNull
  ValueTable getTable();

  void setValueSource(ValueSource valueSource);

  @NotNull
  ValueSource getValueSource();

  void setTable(ValueTable table);

  void setVariable(Variable variable);
}
