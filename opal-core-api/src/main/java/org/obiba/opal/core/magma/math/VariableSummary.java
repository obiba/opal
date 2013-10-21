package org.obiba.opal.core.magma.math;

import java.io.Serializable;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface VariableSummary extends Serializable {

  String getCacheKey(ValueTable table);

  @Nonnull
  Variable getVariable();

  @Nonnull
  String getVariableName();

}
