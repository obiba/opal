package org.obiba.opal.core.magma.math;

import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.springframework.util.Assert;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public abstract class AbstractVariableSummary implements VariableSummary {

  private static final long serialVersionUID = 3105572632716973506L;

  @NotNull
  protected transient final Variable variable;

  @NotNull
  protected final String variableName;

  protected Integer offset;

  protected Integer limit;

  protected AbstractVariableSummary(@NotNull Variable variable) {
    Assert.notNull(variable, "Variable cannot be null");
    this.variable = variable;
    variableName = variable.getName();
  }

  protected SortedSet<VariableEntity> getVariableEntities(@NotNull ValueTable table) {
    if(offset == null && limit == null) return Sets.newTreeSet(table.getVariableEntities());

    Iterable<VariableEntity> entities;
    entities = Sets.newTreeSet(table.getVariableEntities());
    // Apply offset then limit (in that order)
    if(offset != null) {
      entities = Iterables.skip(entities, offset);
    }
    if(limit != null && limit >= 0) {
      entities = Iterables.limit(entities, limit);
    }
    return Sets.newTreeSet(entities);
  }

  @NotNull
  @Override
  public String getVariableName() {
    return variableName;
  }

  @NotNull
  @Override
  public Variable getVariable() {
    return variable;
  }

  void setOffset(Integer offset) {
    this.offset = offset;
  }

  void setLimit(Integer limit) {
    this.limit = limit;
  }

  public boolean isFiltered() {
    return offset != null || limit != null;
  }

  public Integer getOffset() {
    return offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public interface VariableSummaryBuilder<TVariableSummary extends VariableSummary> {

    TVariableSummary build();

    @NotNull
    Variable getVariable();
  }

}
