package org.obiba.opal.mart.writer.support;

import org.obiba.opal.mart.aggregate.AggregateQueryResult;
import org.springframework.batch.item.file.transform.LineAggregator;

public class AggregateQueryResultLineAggregator implements LineAggregator<AggregateQueryResult> {

  private char separator = ',';
  private char quote = '"';

  public String aggregate(AggregateQueryResult item) {
    StringBuilder sb = new StringBuilder();
    quote(sb, item.getAggregateKey());
    for (Object value : item.values()) {
      sb.append(separator);
      quote(sb, value);
    }
    return sb.toString();
  }

  protected void quote(StringBuilder sb, Object value) {
    sb.append(quote).append(value).append(quote);
  }

}
