package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;

public class ValueTableAddedEvent extends ValueTableEvent {

  private String datasourceName;

  private String tableName;

  public ValueTableAddedEvent(ValueTable table) {
    super(table);
  }

  public ValueTableAddedEvent(String datasourceName, String tableName) {
    super(null);
    this.datasourceName = datasourceName;
    this.tableName = tableName;
  }

  public String getDatasourceName() {
    if (hasValueTable()) return getValueTable().getDatasource().getName();
    return datasourceName;
  }

  public String getTableName() {
    if (hasValueTable()) return getValueTable().getName();
    return tableName;
  }
}