package org.obiba.opal.core.magma.math;

import java.util.SortedSet;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public abstract class AbstractVariableSummary {

  private Integer offset;

  private Integer limit;

  protected SortedSet<VariableEntity> getVariableEntities(@Nonnull ValueTable table) {
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

}
