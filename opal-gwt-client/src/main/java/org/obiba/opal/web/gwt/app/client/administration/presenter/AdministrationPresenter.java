package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class AdministrationPresenter extends Presenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy>
    implements HasPageTitle, RequestAdministrationPermissionEvent.Handler {

  @ProxyStandard
  @NameToken(Places.ADMINISTRATION)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {}

  public interface Display extends View {

    HasAuthorization getGeneralSettingsAuthorizer();

    HasAuthorization getTasksAuthorizer();

    HasAuthorization getReportsAuthorizer();

    HasAuthorization getJVMAuthorizer();

    HasAuthorization getDatabasesAuthorizer();

    HasAuthorization getSearchAuthorizer();

    HasAuthorization getDataShieldAuthorizer();

    HasAuthorization getRAuthorizer();

    HasAuthorization getProfilesAuthorizer();

    HasAuthorization getUsersGroupsAuthorizer();

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

    void postAutorizationUpdate();
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


  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()).get()
        .authorize(getView().getUsersGroupsAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROFILES.create().build()).get()
        .authorize(getView().getProfilesAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_R.create().build()).get()
        .authorize(getView().getRAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_DATASHIELD.create().build()).get()
        .authorize(getView().getDataShieldAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SERVICE_SEARCH_INDICES.create().build()).get()
        .authorize(getView().getSearchAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SYSTEM_ENV.create().build()).get()
        .authorize(getView().getGeneralSettingsAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATABASES.create().build()).get()
        .authorize(getView().getDatabasesAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SYSTEM_ENV.create().build()).get()
        .authorize(getView().getJVMAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.IDENTIFIERS_TABLES.create().build()).get()
        .authorize(getView().getIdentifiersAuthorizer()).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.REPORT_TEMPLATES.create().build())
        .get().authorize(getView().getReportsAuthorizer()).send();

    // Must use a CompositeAuthorizer and include ViewAuthorization to update the group widgets
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SHELL_COMMANDS.create().build())
        .get().authorize(new CompositeAuthorizer(getView().getTasksAuthorizer(), new ViewAuthorization())).send();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
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


  private final class ViewAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      getView().postAutorizationUpdate();

    }

    @Override
    public void unauthorized() {
      getView().postAutorizationUpdate();
    }
  }
}
