package org.obiba.opal.mart.aggregate;

import org.obiba.opal.mart.QueryResult;

public class VariableValueFrequencyQueryResultAggregator implements QueryResultAggregator<AggregateQueryResult> {

  private String aggregatedVariableName;

  private AggregateQueryResult currentAggregation;

  public void setAggregatedVariableName(String aggregatedVariableName) {
    this.aggregatedVariableName = aggregatedVariableName;
  }

  public AggregateQueryResult aggregate(QueryResult item) throws Exception {

    if (item == null) {
      AggregateQueryResult returnValue = currentAggregation;
      currentAggregation = null;
      return returnValue;
    }

    if (currentAggregation == null) {
      currentAggregation = new AggregateQueryResult(aggregatedVariableName);
    }

    if (item.getDataItemClass().getName().equals(aggregatedVariableName)) {
      Object value = item.getValue();
      Integer frequency = currentAggregation.getValue(value);
      if (frequency == null) {
        frequency = 0;
      }
      frequency = frequency + 1;
      currentAggregation.addValue(value, frequency);
    }

    return null;
  }
}
