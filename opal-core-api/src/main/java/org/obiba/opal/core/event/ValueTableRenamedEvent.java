package org.obiba.opal.core.event;

import org.obiba.magma.ValueTable;

public class ValueTableRenamedEvent extends ValueTableEvent {

  private final String newName;

  public ValueTableRenamedEvent(ValueTable table, String newName) {
    super(table);
    this.newName = newName;
  }

  public String getNewName() {
    return newName;
  }
}
