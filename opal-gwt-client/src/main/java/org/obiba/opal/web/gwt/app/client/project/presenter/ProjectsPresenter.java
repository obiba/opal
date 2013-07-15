package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class ProjectsPresenter extends Presenter<ProjectsPresenter.Display,ProjectsPresenter.Proxy> implements ProjectsUiHandlers,
    HasPageTitle {

  @Inject
  public ProjectsPresenter(EventBus eventBus, Display display, Proxy proxy) {
    super(eventBus,display,proxy);
    getView().setUiHandlers(this);
  }
  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, PageContainerPresenter.CONTENT, this);
  }
  @Override
  public void onRefresh() {
    GWT.log("ProjectsPresenter.onRefresh()");
  }

  @Override
  public String getTitle() {
    return "Projects";
  }

  public interface Display extends View, HasUiHandlers<ProjectsUiHandlers> {
    void setError(String errorText);
  }

  @ProxyStandard
  @NameToken(Places.projects)
  public interface Proxy extends ProxyPlace<ProjectsPresenter> {}


}
