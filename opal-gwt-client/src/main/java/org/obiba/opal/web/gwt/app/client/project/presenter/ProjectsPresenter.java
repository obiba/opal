package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ProjectsPresenter extends Presenter<ProjectsPresenter.Display, ProjectsPresenter.Proxy>
    implements ProjectsUiHandlers, HasPageTitle {

  @Inject
  public ProjectsPresenter(EventBus eventBus, Display display, Proxy proxy) {
    super(eventBus, display, proxy, PageContainerPresenter.CONTENT);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    refresh();
  }

  @Override
  public String getTitle() {
    return "Projects";
  }

  public void refresh() {
    ResourceRequestBuilderFactory.<JsArray<ProjectDto>>newBuilder().forResource("/projects").get()
        .withCallback(new ResourceCallback<JsArray<ProjectDto>>() {
          @Override
          public void onResource(Response response, JsArray<ProjectDto> resource) {
            getView().setProjects(JsArrays.toSafeArray(resource));
          }
        }).send();
  }

  @Override
  public void onProjectSelection(ProjectDto project) {
    GWT.log("selected: " + project.getName());
  }

  public interface Display extends View, HasUiHandlers<ProjectsUiHandlers> {
    void setProjects(JsArray<ProjectDto> projects);
  }

  @ProxyStandard
  @NameToken(Places.projects)
  public interface Proxy extends ProxyPlace<ProjectsPresenter> {}

}
