package org.obiba.opal.datasource.onyx.variable;

import java.util.Date;

import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.opal.elmo.concepts.Entity;

public interface VariableDataVisitor {

  public void forEntity(Class<? extends Entity> entityType, String id, Date sourceDate);

  public void visit(VariableData data);

  public void end();

}
