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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
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
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ProjectsPresenter extends Presenter<ProjectsPresenter.Display, ProjectsPresenter.Proxy>
    implements ProjectsUiHandlers {

  private final PlaceManager placeManager;

  private JsArray<ProjectDto> projects;

  private final Translations translations;

  @Inject
  public ProjectsPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.translations = translations;
    this.placeManager = placeManager;
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageProjectsTitle();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    refresh();
  }

  public void refresh() {
    ResourceRequestBuilderFactory.<JsArray<ProjectDto>>newBuilder().forResource("/projects").get()
        .withCallback(new ResourceCallback<JsArray<ProjectDto>>() {
          @Override
          public void onResource(Response response, JsArray<ProjectDto> resource) {
            projects = JsArrays.toSafeArray(resource);
            getView().setProjects(projects);
          }
        }).send();
  }

  @Override
  public void onProjectSelection(ProjectDto project) {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.project)
        .with(ParameterTokens.TOKEN_NAME, project.getName()).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onProjectTableSelection(ProjectDto project, String table) {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.project)
        .with(ParameterTokens.TOKEN_NAME, project.getName()) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.tables.toString()) //
        .with(ParameterTokens.TOKEN_PATH, project.getName() + "." + table).build();
    placeManager.revealPlace(request);
  }

  @Override
  public boolean onAddProject(ProjectFactoryDto project) {
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
          refresh();
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

  public interface Display extends View, HasUiHandlers<ProjectsUiHandlers> {

    void setProjects(JsArray<ProjectDto> projects);

    void setNameError(String message);
  }

  @ProxyStandard
  @NameToken(Places.projects)
  public interface Proxy extends ProxyPlace<ProjectsPresenter> {}

}
