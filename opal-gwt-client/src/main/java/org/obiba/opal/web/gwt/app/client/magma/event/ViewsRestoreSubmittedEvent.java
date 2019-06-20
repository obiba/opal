package org.obiba.opal.web.gwt.app.client.magma.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ViewsRestoreSubmittedEvent extends GwtEvent<ViewsRestoreSubmittedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onViewsRestoreSubmitted(ViewsRestoreSubmittedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public ViewsRestoreSubmittedEvent() { }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onViewsRestoreSubmitted(this);
  }


}
