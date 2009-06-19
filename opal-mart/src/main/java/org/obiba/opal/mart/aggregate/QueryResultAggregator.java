package org.obiba.opal.mart.aggregate;

import org.obiba.opal.mart.QueryResult;

public interface QueryResultAggregator<T> {

  T aggregate(QueryResult item) throws Exception;

}
