package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;

public class ValueTableEvent {

  private final ValueTable valueTable;

  public ValueTableEvent(ValueTable table) {
    this.valueTable = table;
  }

  public ValueTable getValueTable() {
    return valueTable;
  }

  public boolean hasValueTable() {
    return valueTable != null;
  }
}