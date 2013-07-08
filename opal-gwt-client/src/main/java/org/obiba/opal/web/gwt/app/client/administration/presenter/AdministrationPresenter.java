package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class AdministrationPresenter
    extends Presenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy> {

  @Inject
  public AdministrationPresenter(Display display, EventBus eventBus, Proxy proxy) {
    super(eventBus, display, proxy);
    addHandlers();
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, ApplicationPresenter.WORKBENCH, this);
  }

  @ProxyStandard
  @NameToken(Places.administration)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {}

  //
  // Private Methods
  //

  private void addHandlers() {

    getView().getDatabasesPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.databasesPlace));
      }
    });

    getView().getIndexPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.indexPlace));
      }
    });

    getView().getRPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.rPlace));
      }
    });


    getView().getUnitsPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.unitsPlace));
      }
    });

    getView().getFilesPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.filesPlace));
      }
    });

    getView().getTasksPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.jobsPlace));
      }
    });

    getView().getDataShieldPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.datashieldPlace));
      }
    });
  }



  public interface Display extends View {
    Anchor getUsersGroupsPlace();
    Anchor getUnitsPlace();
    Anchor getDatabasesPlace();
    Anchor getMongoDbPlace();
    Anchor getEsPlace();
    Anchor getIndexPlace();
    Anchor getRPlace();
    Anchor getDataShieldPlace();
    Anchor getPluginsPlace();
    Anchor getReportsPlace();
    Anchor getFilesPlace();
    Anchor getTasksPlace();
    Anchor getJavaPlace();
    Anchor getServerPlace();
  }

}
