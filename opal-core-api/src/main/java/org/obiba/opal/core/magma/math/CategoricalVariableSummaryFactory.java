package org.obiba.opal.core.magma.math;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class CategoricalVariableSummaryFactory extends AbstractVariableSummaryFactory {

  private boolean distinct;

  private Integer offset;

  private Integer limit;

  @Override
  public String getCacheKey() {
    return getCacheKey(getVariable(), getTable(), distinct, offset, limit);
  }

  public static String getCacheKey(Variable variable, ValueTable table, boolean distinct, Integer offset,
      Integer limit) {
    String key = variable.getVariableReference(table) + "." + distinct;
    if(offset != null) key += "." + offset;
    if(limit != null) key += "." + limit;
    return key;
  }

  public CategoricalVariableSummary getSummary() {
    return new CategoricalVariableSummary.Builder(getVariable()) //
        .distinct(distinct) //
        .filter(offset, limit) //
        .addTable(getTable(), getValueSource()) //
        .build();
  }

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder
      extends AbstractVariableSummaryFactory.Builder<CategoricalVariableSummaryFactory, Builder> {

    @Override
    protected CategoricalVariableSummaryFactory createFactory() {
      return new CategoricalVariableSummaryFactory();
    }

    @Override
    protected Builder createBuilder() {
      return this;
    }

    public Builder distinct(boolean distinct) {
      factory.distinct = distinct;
      return this;
    }

    public Builder offset(Integer offset) {
      factory.offset = offset;
      return this;
    }

    public Builder limit(Integer limit) {
      factory.limit = limit;
      return this;
    }

    public CategoricalVariableSummaryFactory build() {
      return factory;
    }

  }

}
