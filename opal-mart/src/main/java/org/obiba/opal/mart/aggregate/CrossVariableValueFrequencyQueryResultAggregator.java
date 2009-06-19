package org.obiba.opal.mart.aggregate;

import java.util.Set;

import org.obiba.opal.mart.QueryResult;

public class CrossVariableValueFrequencyQueryResultAggregator implements QueryResultAggregator<AggregateQueryResult> {

  private Set<String> crossedVariables;

  private AggregateQueryResult currentAggregation;

  public void setCrossedVariables(Set<String> crossedVariables) {
    this.crossedVariables = crossedVariables;
  }

  public AggregateQueryResult aggregate(QueryResult item) throws Exception {

    if (item == null) {
      AggregateQueryResult returnValue = currentAggregation;
      currentAggregation = null;
      return returnValue;
    }

    String entityId = item.getEntityId();
    if (currentAggregation == null) {
      currentAggregation = new AggregateQueryResult(entityId);
    }

    AggregateQueryResult toReturn = null;

    if (currentAggregation.isFor(entityId) == false) {
      toReturn = currentAggregation;
      currentAggregation = new AggregateQueryResult(entityId);
    }

    if (crossedVariables.contains(item.getDataItemClass().getClassName())) {
      Object value = item.getValue();
      Integer frequency = currentAggregation.getValue(value);
      if (frequency == null) {
        frequency = 0;
      }
      frequency = frequency + 1;
      currentAggregation.addValue(value, frequency);
    }

    return toReturn;
  }

}
