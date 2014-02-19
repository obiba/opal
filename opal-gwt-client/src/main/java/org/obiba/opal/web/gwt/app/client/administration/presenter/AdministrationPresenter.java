package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

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

public class AdministrationPresenter extends Presenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy>
    implements HasPageTitle {

  @ProxyStandard
  @NameToken(Places.ADMINISTRATION)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {}

  public interface Display extends View {

    HasAuthorization getDataAccessAuthorizer();

    HasAuthorization getDataAnalysisAuthorizer();

    HasAuthorization getSystemAuthorizer();

    HasAuthorization getIdentifiersAuthorizer();

    void setUsersGroupsHistoryToken(String historyToken);

    void setProfilesHistoryToken(String historyToken);

    void setIdentifiersMappingsHistoryToken(String historyToken);

    void setDatabasesHistoryToken(String historyToken);

    void setIndexHistoryToken(String historyToken);

    void setRHistoryToken(String historyToken);

    void setDataShieldHistoryToken(String historyToken);

    void setReportsHistoryToken(String historyToken);

    void setFilesHistoryToken(String historyToken);

    void setTasksHistoryToken(String historyToken);

    void setJavaHistoryToken(String historyToken);

    void setServerHistoryToken(String historyToken);

    void setTaxonomiesHistoryToken(String historyToken);

  }

  //
  // Data members
  //

  private final PlaceManager placeManager;

  private final Translations translations;

  @Inject
  public AdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      Translations translations) {
    super(eventBus, display, proxy, PageContainerPresenter.CONTENT);
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
  protected void onReveal() {
    super.onReveal();
    authorize();
  }

  private void authorize() {
    fireEvent(new RequestAdministrationPermissionEvent(new HasAuthorization() {

      @Override
      public void unauthorized() {
      }

      @Override
      public void beforeAuthorization() {
        getView().getDataAccessAuthorizer().beforeAuthorization();
        getView().getDataAnalysisAuthorizer().beforeAuthorization();
        getView().getSystemAuthorizer().beforeAuthorization();
        getView().getIdentifiersAuthorizer().beforeAuthorization();
      }

      @Override
      public void authorized() {
        getView().getDataAccessAuthorizer().authorized();
        getView().getDataAnalysisAuthorizer().authorized();
        getView().getSystemAuthorizer().authorized();
        getView().getIdentifiersAuthorizer().authorized();
      }
    }));
  }

  private void setHistoryTokens() {
    getView().setUsersGroupsHistoryToken(getHistoryToken(Places.USERS));
    getView().setProfilesHistoryToken(getHistoryToken(Places.PROFILES));
    getView().setDatabasesHistoryToken(getHistoryToken(Places.DATABASES));
    getView().setIndexHistoryToken(getHistoryToken(Places.INDEX));
    getView().setRHistoryToken(getHistoryToken(Places.R));
    getView().setIdentifiersMappingsHistoryToken(getHistoryToken(Places.IDENTIFIERS));
    getView().setFilesHistoryToken(getHistoryToken(Places.FILES));
    getView().setTasksHistoryToken(getHistoryToken(Places.TASKS));
    getView().setDataShieldHistoryToken(getHistoryToken(Places.DATASHIELD));
    getView().setReportsHistoryToken(getHistoryToken(Places.REPORT_TEMPLATES));
    getView().setJavaHistoryToken(getHistoryToken(Places.JVM));
    getView().setServerHistoryToken(getHistoryToken(Places.SERVER));
    getView().setTaxonomiesHistoryToken(getHistoryToken(Places.TAXONOMIES));
  }

  private String getHistoryToken(String place) {
    return placeManager.buildRelativeHistoryToken(createRequest(place), 1);
  }

  private PlaceRequest createRequest(String nameToken) {
    return new PlaceRequest.Builder().nameToken(nameToken).build();
  }

}
