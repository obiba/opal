package org.obiba.opal.core.magma.math;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface VariableSummary extends Serializable {

  String getCacheKey(ValueTable table);

  @NotNull
  Variable getVariable();

  @NotNull
  String getVariableName();

}
