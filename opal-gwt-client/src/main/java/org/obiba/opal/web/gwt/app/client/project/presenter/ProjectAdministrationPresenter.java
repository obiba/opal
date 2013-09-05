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
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.DatabaseDto;
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

public class ProjectAdministrationPresenter extends PresenterWidget<ProjectAdministrationPresenter.Display>
    implements ProjectEditionUiHandlers {

  @Inject
  public ProjectAdministrationPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder().forResource(DatabaseResources.databases())
        .withCallback(new ResourceCallback<JsArray<DatabaseDto>>() {

          @Override
          public void onResource(Response response, JsArray<DatabaseDto> resource) {
            getView().setAvailableDatabases(resource);
          }
        }).get().send();
  }

  public void setProject(ProjectDto project) {
    getView().setProject(project);
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
