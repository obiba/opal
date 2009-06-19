package org.obiba.opal.mart.aggregate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AggregateQueryResult {

  private String aggregateKey;

  private Map<Object, Object> values = new HashMap<Object, Object>();

  public AggregateQueryResult(String key) {
    this.aggregateKey = key;
  }

  public String getAggregateKey() {
    return aggregateKey;
  }

  public boolean isFor(String key) {
    return this.aggregateKey.equals(key);
  }

  public void addValue(Object key, Object value) {
    this.values.put(key, value);
  }

  public <T> T getValue(Object key) {
    return (T) this.values.get(key);
  }

  public Collection<Object> values() {
    return this.values.values();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(aggregateKey).append(":").append(values);
    return sb.toString();
  }
}
