package org.obiba.opal.datasource.onyx.variable;

import org.obiba.onyx.engine.variable.VariableData;

public interface VariableDataVisitor {

  public void visit(VariableData data);

}
