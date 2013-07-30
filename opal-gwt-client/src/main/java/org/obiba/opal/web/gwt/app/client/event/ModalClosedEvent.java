package org.obiba.opal.web.gwt.app.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ModalClosedEvent<S> extends GwtEvent<ModalClosedEvent.Handler> {
  public interface Handler extends EventHandler {
    void onModalClosed(ModalClosedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private Object source;

  public ModalClosedEvent(Object source) {
    this.source = source;
  }

  @Override
  public Object getSource() {
    return source;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onModalClosed(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }

}
