package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class AdministrationPresenter extends Presenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy>
    implements HasPageTitle {

  @ProxyStandard
  @NameToken(Places.ADMINISTRATION)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {}

  public interface Display extends View {
    void setUsersGroupsHistoryToken(String historyToken);

    void setUnitsHistoryToken(String historyToken);

    void setDatabasesHistoryToken(String historyToken);

    void setIndexHistoryToken(String historyToken);

    void setRHistoryToken(String historyToken);

    void setDataShieldHistoryToken(String historyToken);

    void setPluginsHistoryToken(String historyToken);

    void setReportsHistoryToken(String historyToken);

    void setFilesHistoryToken(String historyToken);

    void setTasksHistoryToken(String historyToken);

    void setJavaHistoryToken(String historyToken);

    void setServerHistoryToken(String historyToken);

  }

  //
  // Data members
  //

  private final PlaceManager placeManager;

  private final Translations translations;

  @Inject
  public AdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      Translations translations) {
    super(eventBus, display, proxy);
    this.placeManager = placeManager;
    this.translations = translations;
    setHistoryTokens();
  }

  @Override
  @TitleFunction
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

  private void setHistoryTokens() {
    getView().setUsersGroupsHistoryToken(getHistoryToken(Places.USERS_GROUPS));
    getView().setDatabasesHistoryToken(getHistoryToken(Places.DATABASES));
    getView().setIndexHistoryToken(getHistoryToken(Places.INDEX));
    getView().setRHistoryToken(getHistoryToken(Places.R));
    getView().setUnitsHistoryToken(getHistoryToken(Places.UNITS));
    getView().setFilesHistoryToken(getHistoryToken(Places.FILES));
    getView().setTasksHistoryToken(getHistoryToken(Places.JOBS));
    getView().setDataShieldHistoryToken(getHistoryToken(Places.DATASHIELD));
    getView().setReportsHistoryToken(getHistoryToken(Places.REPORT_TEMPLATES));
    getView().setJavaHistoryToken(getHistoryToken(Places.JVM));
    getView().setServerHistoryToken(getHistoryToken(Places.SERVER));
  }

  private String getHistoryToken(String place) {
    return placeManager.buildRelativeHistoryToken(createRequest(place), 1);
  }

  private PlaceRequest createRequest(String nameToken) {
    return new PlaceRequest.Builder().nameToken(nameToken).build();
  }

}
