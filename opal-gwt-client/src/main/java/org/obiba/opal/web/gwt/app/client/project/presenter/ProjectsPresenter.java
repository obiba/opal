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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.core.client.JsArray;
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

  private final ModalProvider<AddProjectModalPresenter> addProjectModalProvider;

  @Inject
  public ProjectsPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager, ModalProvider<AddProjectModalPresenter> addProjectModalProvider) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.translations = translations;
    this.placeManager = placeManager;
    this.addProjectModalProvider = addProjectModalProvider.setContainer(this);
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageProjectsTitle();
  }

  @Override
  public void onBind() {
    super.onBind();
    registerHandler(getEventBus().addHandler(ProjectCreatedEvent.getType(), new ProjectCreatedEvent.Handler() {
      @Override
      public void onProjectCreated(ProjectCreatedEvent event) {
        refresh();
      }
    }));
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
  public void showAddProject() {
    AddProjectModalPresenter presenter = addProjectModalProvider.get();
    presenter.setProjects(projects);
  }


  public interface Display extends View, HasUiHandlers<ProjectsUiHandlers> {
    void setProjects(JsArray<ProjectDto> projects);
  }

  @ProxyStandard
  @NameToken(Places.projects)
  public interface Proxy extends ProxyPlace<ProjectsPresenter> {}

}
