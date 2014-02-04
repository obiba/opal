package org.obiba.opal.core.magma.math;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class DefaultVariableSummaryFactory extends AbstractVariableSummaryFactory<DefaultVariableSummary> {

  private boolean distinct;

  private Integer offset;

  private Integer limit;

  @NotNull
  @Override
  public String getCacheKey() {
    return getCacheKey(getVariable(), getTable(), distinct, offset, limit);
  }

  @SuppressWarnings("PMD.ExcessiveParameterList")
  public static String getCacheKey(Variable variable, ValueTable table, boolean distinct, Integer offset,
      Integer limit) {
    String key = variable.getVariableReference(table) + "." + distinct;
    if(offset != null) key += "." + offset;
    if(limit != null) key += "." + limit;
    return key;
  }

  @NotNull
  @Override
  public DefaultVariableSummary getSummary() {
    return new DefaultVariableSummary.Builder(getVariable()) //
        .distinct(distinct) //
        .filter(offset, limit) //
        .addTable(getTable(), getValueSource()) //
        .build();
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
  public static class Builder {

    private final DefaultVariableSummaryFactory factory = new DefaultVariableSummaryFactory();

    public Builder variable(Variable variable) {
      factory.setVariable(variable);
      return this;
    }

    public Builder table(ValueTable table) {
      factory.setTable(table);
      return this;
    }

    public Builder valueSource(ValueSource valueSource) {
      factory.setValueSource(valueSource);
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

    public Builder distinct(boolean distinct) {
      factory.distinct = distinct;
      return this;
    }

    public DefaultVariableSummaryFactory build() {
      return factory;
    }
  }

}
