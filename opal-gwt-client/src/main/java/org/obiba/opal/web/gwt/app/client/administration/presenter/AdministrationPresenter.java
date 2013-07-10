package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;

import com.google.gwt.core.client.GWT;
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

public class AdministrationPresenter extends Presenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy>
    implements HasPageTitle {

  @ProxyStandard
  @NameToken(Places.administration)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {}

  public interface Display extends View, BreadcrumbDisplay {
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

  //
  // Data members
  //
  private static final Translations translations = GWT.create(Translations.class);

  @Inject
  public AdministrationPresenter(Display display, EventBus eventBus, Proxy proxy) {
    super(eventBus, display, proxy);
    addHandlers();
  }

  @Override
  public String getTitle() {
    return translations.pageAdministrationTitle();
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, PageContainerPresenter.CONTENT, this);
  }

  //
  // Private Methods
  //

  private void addHandlers() {

    getView().getUsersGroupsPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.usersGroupsPlace));
      }
    });

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

    getView().getReportsPlace().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.reportTemplatesPlace));
      }
    });
  }

}
