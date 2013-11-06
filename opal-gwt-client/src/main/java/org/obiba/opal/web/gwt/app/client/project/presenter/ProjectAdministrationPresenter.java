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

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ProjectDto;

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

import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class ProjectAdministrationPresenter extends PresenterWidget<ProjectAdministrationPresenter.Display>
    implements ProjectAdministrationUiHandlers {

  private final PlaceManager placeManager;

  private final ModalProvider<ProjectPropertiesModalPresenter> projectPropertiesModalProvider;

  private ProjectDto project;

  private Runnable removeConfirmation;

  @Inject
  public ProjectAdministrationPresenter(EventBus eventBus, Display view, PlaceManager placeManager, ModalProvider<ProjectPropertiesModalPresenter> projectPropertiesModalProvider) {
    super(eventBus, view);
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.projectPropertiesModalProvider = projectPropertiesModalProvider.setContainer(this);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());
    addRegisteredHandler(ProjectUpdatedEvent.getType(), new ProjectUpdatedEvent.ProjectUpdatedHandler() {
      @Override
      public void onProjectUpdated(ProjectUpdatedEvent event) {
        placeManager.revealPlace(ProjectPlacesHelper.getAdministrationPlace(project.getName()));
      }
    });
  }

  public void setProject(ProjectDto project) {
    this.project = project;
    getView().setProject(project);
  }

  @Override
  public void onEdit() {
    ProjectPropertiesModalPresenter presenter = projectPropertiesModalProvider.create();
    presenter.initialize(project);
    projectPropertiesModalProvider.show();
  }

  @Override
  public void onDelete() {
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

    private final ProjectDto projectDto;

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

      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(projectDto.getLink()) //
          .withCallback(callbackHandler, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND) //
          .delete().send();
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
        case SC_OK:
          event = new ProjectUpdatedEvent(dto);
          break;
        case SC_CREATED:
          event = new ProjectCreatedEvent(dto);
          break;
        default:
          //TODO supports DatabaseAlreadyExists
          event = NotificationEvent.newBuilder().error(response.getText()).build();
      }
      getEventBus().fireEvent(event);
    }
  }

  public interface Display extends View, HasUiHandlers<ProjectAdministrationUiHandlers> {

    void setProject(ProjectDto project);

    ProjectDto getProject();
  }

}
