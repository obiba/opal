package org.obiba.opal.web.gwt.app.client.project.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ProjectCreatedEvent extends GwtEvent<ProjectCreatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onProjectCreated(ProjectCreatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public ProjectCreatedEvent() {
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProjectCreated(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }  
}
