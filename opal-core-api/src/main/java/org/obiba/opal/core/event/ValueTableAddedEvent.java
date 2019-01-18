package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;

public class ValueTableAddedEvent extends ValueTableEvent {

  public ValueTableAddedEvent(ValueTable table) {
    super(table);
  }

}