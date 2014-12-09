package org.obiba.opal.web.gwt.app.client.administration.presenter;

import java.util.Arrays;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

public class AdministrationPresenter extends Presenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy>
    implements HasPageTitle {

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

  private final Translations translations;

  @Inject
  public AdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, Translations translations,
      TokenFormatter tokenFormatter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    setHistoryTokens(tokenFormatter);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageAdministrationTitle();
  }

  public void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()).get()
        .authorize(composeAuthorizer(getView().getUsersGroupsAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROFILES.create().build()).get()
        .authorize(composeAuthorizer(getView().getProfilesAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_R.create().build()).get()
        .authorize(composeAuthorizer(getView().getRAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_DATASHIELD.create().build()).get()
        .authorize(composeAuthorizer(getView().getDataShieldAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SERVICE_SEARCH_INDICES.create().build()).get()
        .authorize(composeAuthorizer(getView().getSearchAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SYSTEM_ENV.create().build()).get()
        .authorize(composeAuthorizer(getView().getGeneralSettingsAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATABASES.create().build()).get()
        .authorize(composeAuthorizer(getView().getDatabasesAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SYSTEM_ENV.create().build()).get()
        .authorize(composeAuthorizer(getView().getJVMAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.IDENTIFIERS_TABLES.create().build()).get()
        .authorize(composeAuthorizer(getView().getIdentifiersAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.REPORT_TEMPLATES.create().build())
        .get().authorize(composeAuthorizer(getView().getReportsAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SHELL_COMMANDS.create().build())
        .get().authorize(composeAuthorizer(getView().getTasksAuthorizer())).send();
  }

  private HasAuthorization composeAuthorizer(HasAuthorization authorization) {
    // Must use a CompositeAuthorizer and include ViewAuthorization to update the group widgets
    return new CompositeAuthorizer(authorization, new ViewAuthorization());
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    authorize();
  }

  private void setHistoryTokens(TokenFormatter tokenFormatter) {
    PlaceRequest adminPlace = createRequest(Places.ADMINISTRATION);
    getView().setUsersGroupsHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.USERS));
    getView().setProfilesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.PROFILES));
    getView().setDatabasesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.DATABASES));
    getView().setIndexHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.INDEX));
    getView().setRHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.R));
    getView().setIdentifiersMappingsHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.IDENTIFIERS));
    getView().setFilesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.FILES));
    getView().setTasksHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.TASKS));
    getView().setDataShieldHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.DATASHIELD));
    getView().setReportsHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.REPORT_TEMPLATES));
    getView().setJavaHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.JVM));
    getView().setServerHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.SERVER));
    getView().setTaxonomiesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.TAXONOMIES));
  }

  private String getHistoryToken(TokenFormatter tokenFormatter, PlaceRequest adminPlace, String place) {
    return tokenFormatter.toHistoryToken(Arrays.asList(adminPlace, createRequest(place)));
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
