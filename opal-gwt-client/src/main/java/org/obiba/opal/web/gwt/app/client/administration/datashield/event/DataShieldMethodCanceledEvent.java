package org.obiba.opal.web.gwt.app.client.administration.datashield.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class DataShieldMethodCanceledEvent extends GwtEvent<DataShieldMethodCanceledEvent.Handler>  {

  public interface Handler extends EventHandler {
    void onDataShieldMethodCanceled(DataShieldMethodCanceledEvent event);
  }

  private static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<Handler>();

  public DataShieldMethodCanceledEvent() {
  }

  public static GwtEvent.Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataShieldMethodCanceled(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }

}
