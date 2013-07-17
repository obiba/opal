package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ProjectPresenter extends Presenter<ProjectPresenter.Display, ProjectPresenter.Proxy> implements ProjectUiHandlers {

  private final PlaceManager placeManager;

  private String name;

  private ProjectDto project;

  private final Translations translations;

  @Inject
  public ProjectPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.translations = translations;
    this.placeManager = placeManager;
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    name = request.getParameter(ParameterTokens.TOKEN_ID, null);
    refresh();
  }

  public void refresh() {
    // TODO handle wrong or missing id
    UriBuilder builder = UriBuilder.create().segment("project", name);
    ResourceRequestBuilderFactory.<ProjectDto>newBuilder().forResource(builder.build()).get()
        .withCallback(new ResourceCallback<ProjectDto>() {
          @Override
          public void onResource(Response response, ProjectDto resource) {
            project = resource;
            getView().setProject(project);
          }
        }).send();
  }

  @Override
  public void onProjectsSelection() {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.projects).build();
    placeManager.revealPlace(request);
  }

  public interface Display extends View, HasUiHandlers<ProjectUiHandlers> {

    void setProject(ProjectDto project);

  }

  @ProxyStandard
  @NameToken(Places.project)
  public interface Proxy extends ProxyPlace<ProjectPresenter> {}
}
