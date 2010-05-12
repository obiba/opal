package org.obiba.opal.web.client.smartgwt.client.views.event;

import com.google.gwt.event.shared.GwtEvent;
import com.smartgwt.client.data.Record;

public class ValueTableChangedEvent extends GwtEvent<ValueTableChangedHandler> {

  private static Type<ValueTableChangedHandler> TYPE;

  public static Type<ValueTableChangedHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<ValueTableChangedHandler>());
  }

  private final Record valueTable;

  public ValueTableChangedEvent(Record valueTable) {
    this.valueTable = valueTable;
  }

  public Record getValueTable() {
    return valueTable;
  }

  @Override
  public final Type<ValueTableChangedHandler> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(ValueTableChangedHandler handler) {
    handler.onValueTableChanged(this);
  }

}
