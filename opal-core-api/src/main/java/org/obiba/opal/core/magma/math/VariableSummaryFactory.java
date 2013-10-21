package org.obiba.opal.core.magma.math;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface VariableSummaryFactory<TVariableSummary extends VariableSummary> {

  @Nonnull
  TVariableSummary getSummary();

  @Nonnull
  String getCacheKey();

  @Nonnull
  Variable getVariable();

  @Nonnull
  ValueTable getTable();

  void setValueSource(ValueSource valueSource);

  @Nonnull
  ValueSource getValueSource();

  void setTable(ValueTable table);

  void setVariable(Variable variable);
}
