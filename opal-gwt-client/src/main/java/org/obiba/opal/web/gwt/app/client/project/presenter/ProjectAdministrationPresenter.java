/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseResources;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class ProjectAdministrationPresenter extends PresenterWidget<ProjectAdministrationPresenter.Display>
    implements ProjectEditionUiHandlers {

  private final PlaceManager placeManager;

  private ProjectDto project;

  private Runnable removeConfirmation;

  @Inject
  public ProjectAdministrationPresenter(EventBus eventBus, Display view, PlaceManager placeManager) {
    super(eventBus, view);
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {

    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());
  }

  public void setProject(ProjectDto project) {
    this.project = project;
    getView().setProject(project);
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder().forResource(DatabaseResources.databases())
        .withCallback(new ResourceCallback<JsArray<DatabaseDto>>() {

          @Override
          public void onResource(Response response, JsArray<DatabaseDto> resource) {
            getView().setAvailableDatabases(resource);
          }
        }).get().send();
  }

  @Override
  public void saveStorage(String database) {
    ProjectDto project = getView().getProject();
    project.setDatabase(database);

    ResponseCodeCallback callbackHandler = new CreateOrUpdateCallBack(project);
    ResourceRequestBuilderFactory.newBuilder().forResource("database/" + project.getName()) //
        .put() //
        .withResourceBody(ProjectDto.stringify(project)) //
        .withCallback(Response.SC_OK, callbackHandler) //
        .withCallback(Response.SC_CREATED, callbackHandler) //
        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  @Override
  public void save() {
    //TODO
  }

  @Override
  public void cancel() {
    //TODO
  }

  @Override
  public void delete() {
    removeConfirmation = new RemoveRunnable(project);
    fireEvent(ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeProject", "confirmRemoveProject"));
  }

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

  private class RemoveRunnable implements Runnable {

    private ProjectDto projectDto;

    private RemoveRunnable(ProjectDto projectDto) {
      this.projectDto = projectDto;
    }

    @Override
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            PlaceRequest projectsRequest = new PlaceRequest.Builder().nameToken(Places.PROJECTS).build();
            placeManager.revealPlace(projectsRequest);
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      ResourceRequestBuilderFactory.newBuilder().forResource(projectDto.getLink()).delete()
          .withCallback(SC_OK, callbackHandler).withCallback(SC_FORBIDDEN, callbackHandler)
          .withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(SC_NOT_FOUND, callbackHandler).send();
    }
  }

  private class CreateOrUpdateCallBack implements ResponseCodeCallback {

    private final ProjectDto dto;

    private CreateOrUpdateCallBack(ProjectDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      Event<?> event = null;
      switch(response.getStatusCode()) {
        case Response.SC_OK:
          event = new ProjectUpdatedEvent(dto);
          break;
        case Response.SC_CREATED:
          event = new ProjectCreatedEvent(dto);
          break;
        default:
          //TODO supports DatabaseAlreadyExists
          event = NotificationEvent.newBuilder().error(response.getText()).build();
      }
      getEventBus().fireEvent(event);
    }
  }

  public interface Display extends View, HasUiHandlers<ProjectEditionUiHandlers> {

    void setProject(ProjectDto project);

    ProjectDto getProject();

    void setAvailableDatabases(JsArray<DatabaseDto> dtos);
  }

}
