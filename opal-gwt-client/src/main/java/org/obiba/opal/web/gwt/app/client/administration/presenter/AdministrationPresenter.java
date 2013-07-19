package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;

import com.google.gwt.core.client.GWT;
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
  @NameToken(Places.administration)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {}

  public interface Display extends View {
    void setUsersGroupsHistoryToken(String historyToken);

    void setUnitsHistoryToken(String historyToken);

    void setDatabasesHistoryToken(String historyToken);

    void setMongoDbHistoryToken(String historyToken);

    void setEsHistoryToken(String historyToken);

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
  private static final Translations translations = GWT.create(Translations.class);

  private final PlaceManager placeManager;

  @Inject
  public AdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager) {
    super(eventBus, display, proxy);
    this.placeManager = placeManager;
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

    getView().setUsersGroupsHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.usersGroups)));
    getView().setDatabasesHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.databases)));
    getView().setIndexHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.index)));
    getView().setRHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.r)));
    getView().setUnitsHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.units)));
    getView().setFilesHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.files)));
    getView().setTasksHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.jobs)));
    getView().setDataShieldHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.datashield)));
    getView().setReportsHistoryToken(placeManager.buildRelativeHistoryToken(createRequest(Places.reportTemplates)));
  }

  private PlaceRequest createRequest(String nameToken) {
    return new PlaceRequest.Builder().nameToken(nameToken).build();
  }

}
