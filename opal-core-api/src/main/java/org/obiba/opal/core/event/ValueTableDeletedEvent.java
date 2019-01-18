package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;

public class ValueTableDeletedEvent extends ValueTableEvent {

  public ValueTableDeletedEvent(ValueTable table) {
    super(table);
  }
}
