package org.obiba.opal.mart.aggregate;

import org.obiba.opal.mart.QueryResult;

public class EntityIdAggregatingQueryResultAggregator implements QueryResultAggregator<AggregateQueryResult> {

  private AggregateQueryResult currentAggregation;

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

    if (currentAggregation.isFor(entityId)) {
      currentAggregation.addValue(item.getDataItemClass().getName(), item.getValue());
    } else {
      AggregateQueryResult oldAggregate = currentAggregation;
      currentAggregation = new AggregateQueryResult(entityId);
      currentAggregation.addValue(item.getDataItemClass().getName(), item.getValue());
      return oldAggregate;
    }

    return null;
  }

}
