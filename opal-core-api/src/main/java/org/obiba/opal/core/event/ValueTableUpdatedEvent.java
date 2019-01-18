package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;

public class ValueTableUpdatedEvent extends ValueTableEvent {

  public ValueTableUpdatedEvent(ValueTable table) {
    super(table);
  }

}