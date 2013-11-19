package org.obiba.opal.web.magma.math;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface SummaryResource {
  void setValueTable(@NotNull ValueTable valueTable);

  void setVariable(@NotNull Variable variable);

  void setVariableValueSource(@NotNull ValueSource variableValueSource);

  @NotNull
  ValueTable getValueTable();

  @NotNull
  Variable getVariable();

  @NotNull
  ValueSource getVariableValueSource();
}
