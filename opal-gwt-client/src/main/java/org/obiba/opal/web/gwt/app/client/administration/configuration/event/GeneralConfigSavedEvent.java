package org.obiba.opal.web.gwt.app.client.administration.configuration.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class GeneralConfigSavedEvent extends GwtEvent<GeneralConfigSavedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onGeneralConfigSaved(GeneralConfigSavedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public GeneralConfigSavedEvent() {
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onGeneralConfigSaved(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
