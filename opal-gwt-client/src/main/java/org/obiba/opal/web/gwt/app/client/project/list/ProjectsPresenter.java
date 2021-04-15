/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.list;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.edit.EditProjectModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.project.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
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

import java.util.Collections;
import java.util.List;

public class ProjectsPresenter extends Presenter<ProjectsPresenter.Display, ProjectsPresenter.Proxy>
    implements ProjectsUiHandlers {

  @ProxyStandard
  @NameToken(Places.PROJECTS)
  public interface Proxy extends ProxyPlace<ProjectsPresenter> {}

  private final PlaceManager placeManager;

  private JsArray<ProjectDto> projects;

  private List<String> tags = Lists.newArrayList();

  private String projectsFilter;

  private String selectedTag;

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

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    selectedTag = request.getParameter(ParameterTokens.TOKEN_TAG, null);
  }

  private void authorize() {
    // add project
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECTS.create().build()).post()//
        .authorize(getView().getAddProjectAuthorizer())//
        .send();
  }

  private void refresh() {
    getView().beforeRenderProjects();
    ResourceRequestBuilderFactory.<JsArray<ProjectDto>>newBuilder() //
        .forResource(UriBuilders.PROJECTS.create().query("digest", "true").build()) //
        .withCallback(new ResourceCallback<JsArray<ProjectDto>>() {
          @Override
          public void onResource(Response response, JsArray<ProjectDto> resource) {
            projects = JsArrays.toSafeArray(resource);
            tags.clear();
            for (ProjectDto project : JsArrays.toIterable(projects)) {
              for (String tag : JsArrays.toIterable(project.getTagsArray())) {
                if (!tags.contains(tag)) tags.add(tag);
              }
            }
            Collections.sort(tags);
            getView().setTags(tags, selectedTag);
            onProjectsFilterUpdate(selectedTag, projectsFilter);
          }
        }) //
        .get().send();
    authorize();
  }

  @Override
  public void onRefresh() {
    refresh();
  }

  @Override
  public void onProjectSelection(ProjectDto project) {
    goToProject(project.getName());
  }

  private void goToProject(String name) {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.PROJECT)
        .with(ParameterTokens.TOKEN_NAME, name)
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.DASHBOARD.toString()).build();
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
    presenter.setTag(getView().getSelectedTag());
  }

  @Override
  public void onProjectsFilterUpdate(String tag, String filter) {
    projectsFilter = filter;
    if(Strings.isNullOrEmpty(filter)) {
      getView().setProjects(projects);
    } else {
      JsArray<ProjectDto> filtered = JsArrays.create();
      for(ProjectDto project : JsArrays.toIterable(projects)) {
        if(projectMatches(project, filter)) {
          filtered.push(project);
        }
      }
      getView().setProjects(filtered);
    }
    PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.PROJECTS);
    if (!Strings.isNullOrEmpty(tag)) builder.with(ParameterTokens.TOKEN_TAG, tag);
    placeManager.updateHistory(builder.build(), true);
  }

  /**
   * Check if project name matches all the words of the project filter.
   *
   * @param project
   * @param filter
   * @return
   */
  private boolean projectMatches(ProjectDto project, String filter) {
    String name = project.getName().toLowerCase();
    return FilterHelper.matches(name, FilterHelper.tokenize(filter));
  }

  public interface Display extends View, HasUiHandlers<ProjectsUiHandlers> {

    void beforeRenderProjects();

    void setTags(List<String> tags, String selectedTag);

    void setProjects(JsArray<ProjectDto> projects);

    HasAuthorization getAddProjectAuthorizer();

    String getSelectedTag();
  }

  private class ProjectUpdatedHandler
      implements ProjectCreatedEvent.ProjectCreatedHandler, ProjectUpdatedEvent.ProjectUpdatedHandler {

    @Override
    public void onProjectCreated(ProjectCreatedEvent event) {
      goToProject(event.getName());
    }

    @Override
    public void onProjectUpdated(ProjectUpdatedEvent event) {
      refresh();
    }
  }

}
