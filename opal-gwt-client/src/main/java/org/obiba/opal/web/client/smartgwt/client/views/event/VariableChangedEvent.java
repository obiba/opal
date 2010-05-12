package org.obiba.opal.web.client.smartgwt.client.views.event;

import com.google.gwt.event.shared.GwtEvent;
import com.smartgwt.client.data.Record;

public class VariableChangedEvent extends GwtEvent<VariableChangedHandler> {

  private static Type<VariableChangedHandler> TYPE;

  public static Type<VariableChangedHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<VariableChangedHandler>());
  }

  private final Record variable;

  public VariableChangedEvent(Record variable) {
    this.variable = variable;
  }

  public Record getVariable() {
    return variable;
  }

  @Override
  public final Type<VariableChangedHandler> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(VariableChangedHandler handler) {
    handler.onVariableChanged(this);
  }

}
