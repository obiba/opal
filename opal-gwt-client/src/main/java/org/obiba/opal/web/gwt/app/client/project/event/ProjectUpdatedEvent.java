package org.obiba.opal.web.gwt.app.client.project.event;

import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ProjectUpdatedEvent extends GwtEvent<ProjectUpdatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onProjectUpdated(ProjectUpdatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final ProjectDto projectDto;

  public ProjectUpdatedEvent(ProjectDto dto) {
    projectDto = dto;
  }

  public ProjectDto getProjectDto() {
    return projectDto;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProjectUpdated(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
