/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.list;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.edit.EditProjectModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.common.base.Strings;
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
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ProjectsPresenter extends Presenter<ProjectsPresenter.Display, ProjectsPresenter.Proxy>
    implements ProjectsUiHandlers {

  @ProxyStandard
  @NameToken(Places.PROJECTS)
  public interface Proxy extends ProxyPlace<ProjectsPresenter> {}

  private final PlaceManager placeManager;

  private JsArray<ProjectDto> projects;

  private String projectsFilter;

  private final Translations translations;

  private final ModalProvider<EditProjectModalPresenter> addProjectModalProvider;

  @Inject
  public ProjectsPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager, ModalProvider<EditProjectModalPresenter> addProjectModalProvider) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.placeManager = placeManager;
    this.addProjectModalProvider = addProjectModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageProjectsTitle();
  }

  @Override
  public void onBind() {
    super.onBind();
    addRegisteredHandler(ProjectCreatedEvent.getType(), new ProjectUpdatedHandler());
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    refresh();
  }

  private void authorize() {
    // add project
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECTS.create().build()).post()//
        .authorize(getView().getAddProjectAuthorizer())//
        .send();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<JsArray<ProjectDto>>newBuilder() //
        .forResource("/projects") //
        .withCallback(new ResourceCallback<JsArray<ProjectDto>>() {
          @Override
          public void onResource(Response response, JsArray<ProjectDto> resource) {
            projects = JsArrays.toSafeArray(resource);
            onProjectsFilterUpdate(projectsFilter);
          }
        }) //
        .get().send();
    authorize();
  }

  @Override
  public void onProjectSelection(ProjectDto project) {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.PROJECT)
        .with(ParameterTokens.TOKEN_NAME, project.getName())
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onProjectTableSelection(ProjectDto project, String table) {
    PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.PROJECT).with(ParameterTokens.TOKEN_NAME,
        project.getName()) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString());

    if(!Strings.isNullOrEmpty(table)) {
      builder.with(ParameterTokens.TOKEN_PATH, project.getName() + "." + table).build();
    }

    placeManager.revealPlace(builder.build());
  }

  @Override
  public void showAddProject() {
    EditProjectModalPresenter presenter = addProjectModalProvider.get();
    presenter.setProjects(projects);
  }

  @Override
  public void onProjectsFilterUpdate(String filter) {
    projectsFilter = filter;
    if(Strings.isNullOrEmpty(filter)) {
      getView().setProjects(projects);
    } else {
      JsArray<ProjectDto> filtered = JsArrays.create();
      for(ProjectDto table : JsArrays.toIterable(projects)) {
        if(tableMatches(table, filter)) {
          filtered.push(table);
        }
      }
      getView().setProjects(filtered);
    }
  }

  /**
   * Check if project name matches all the words of the project filter.
   *
   * @param project
   * @param filter
   * @return
   */
  private boolean tableMatches(ProjectDto project, String filter) {
    String name = project.getName().toLowerCase();
    for(String token : filter.toLowerCase().split(" ")) {
      if(!Strings.isNullOrEmpty(token)) {
        if(!name.contains(token)) return false;
      }
    }
    return true;
  }

  public interface Display extends View, HasUiHandlers<ProjectsUiHandlers> {
    void setProjects(JsArray<ProjectDto> projects);

    HasAuthorization getAddProjectAuthorizer();
  }

  private class ProjectUpdatedHandler
      implements ProjectCreatedEvent.ProjectCreatedHandler, ProjectUpdatedEvent.ProjectUpdatedHandler {

    @Override
    public void onProjectCreated(ProjectCreatedEvent event) {
      refresh();
    }

    @Override
    public void onProjectUpdated(ProjectUpdatedEvent event) {
      refresh();
    }
  }

}
