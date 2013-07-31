package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class AddProjectModalPresenter extends ModalPresenterWidget<AddProjectModalPresenter.Display>
    implements AddProjectModalUiHandlers {

  private JsArray<ProjectDto> projects;

  public interface Display extends PopupView, HasUiHandlers<AddProjectModalUiHandlers> {
    void setNameError(String message);
  }

  private final Translations translations;

  @Inject
  public AddProjectModalPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    this.translations = translations;

    getView().setUiHandlers(this);
  }

  @Override
  public boolean addProject(final ProjectFactoryDto project) {
    if(Strings.isNullOrEmpty(project.getName())) {
      getView().setNameError(translations.userMessageMap().get("ProjectNameRequired"));
      return false;
    }
    for(ProjectDto p : JsArrays.toIterable(projects)) {
      if(p.getName().equals(project.getName())) {
        getView().setNameError(translations.userMessageMap().get("ProjectNameMustBeUnique"));
        return false;
      }
    }
    ResponseCodeCallback callback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_CREATED) {
          getEventBus().fireEvent(new ProjectCreatedEvent());
        } else if(response.getText() != null && response.getText().length() != 0) {
          ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
          getEventBus().fireEvent(
              NotificationEvent.newBuilder().error("ProjectCreationFailed").args(errorDto.getArgumentsArray()).build());
        } else {
          getEventBus()
              .fireEvent(NotificationEvent.newBuilder().error(translations.datasourceCreationFailed()).build());
        }
      }
    };
    ResourceRequestBuilderFactory.<ProjectFactoryDto>newBuilder().forResource("/projects").post()//
        .withResourceBody(ProjectFactoryDto.stringify(project))//
        .withCallback(Response.SC_CREATED, callback).withCallback(Response.SC_BAD_REQUEST, callback)
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback).send();

    return true;
  }

  public void setProjects(JsArray<ProjectDto> projects) {
    this.projects = projects;
  }

}
