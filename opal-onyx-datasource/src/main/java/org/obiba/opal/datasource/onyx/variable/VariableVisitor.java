package org.obiba.opal.datasource.onyx.variable;

import org.obiba.onyx.engine.variable.Variable;

public interface VariableVisitor {

  public void forDataEntryForm(Variable variable);

  public void visit(Variable variable);

  public void end();

}
