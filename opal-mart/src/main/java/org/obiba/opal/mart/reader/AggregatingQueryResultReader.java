package org.obiba.opal.mart.reader;

import org.obiba.opal.mart.QueryResult;
import org.obiba.opal.mart.aggregate.QueryResultAggregator;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class AggregatingQueryResultReader<T> implements ItemReader<T>, ItemStream, InitializingBean {

  private ItemReader<QueryResult> itemReader;

  private ItemStream itemStream;

  private QueryResultAggregator<T> aggregator;

  public void setItemReader(ItemReader<QueryResult> itemReader) {
    this.itemReader = itemReader;
  }

  public void setAggregator(QueryResultAggregator<T> aggregator) {
    this.aggregator = aggregator;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(itemReader, "itemReader is mandatory");
    Assert.notNull(aggregator, "aggregator is mandatory");
    itemStream = getItemStream();
  }

  public T read() throws Exception, UnexpectedInputException, ParseException {
    T aggregate = null;
    while (aggregate == null) {
      QueryResult result = itemReader.read();
      aggregate = aggregator.aggregate(result);
      if(result == null && aggregate == null) {
        return null;
      }
    }
    return aggregate;
  }

  public void close() throws ItemStreamException {
    if (itemStream != null) {
      itemStream.close();
    }
  }

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (itemStream != null) {
      itemStream.open(executionContext);
    }
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {
    if (itemStream != null) {
      itemStream.update(executionContext);
    }
  }

  boolean isDelegateItemStream() {
    return itemReader instanceof ItemStream;
  }

  ItemStream getItemStream() {
    return isDelegateItemStream() ? (ItemStream) itemReader : null;
  }
}
