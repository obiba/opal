package org.obiba.opal.datasource.onyx.variable;

import org.obiba.onyx.engine.variable.Variable;

public interface VariableVisitor {

  /**
   * Indicates the source of the data. This could be, for example, a file path.
   */
  public void setSource(String source);

  public void forDataEntryForm(Variable variable);

  public void visit(Variable variable);

  public void end();

}
