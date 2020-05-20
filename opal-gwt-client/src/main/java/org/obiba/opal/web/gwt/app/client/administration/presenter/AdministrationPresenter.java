/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.presenter;

import java.util.Arrays;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.rest.client.*;
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
import org.obiba.opal.web.model.client.database.DatabaseDto;

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

    HasAuthorization getPluginsAuthorizer();

    HasAuthorization getDatabasesAuthorizer();

    HasAuthorization getSearchAuthorizer();

    HasAuthorization getDataShieldAuthorizer();

    HasAuthorization getRAuthorizer();

    HasAuthorization getProfilesAuthorizer();

    HasAuthorization getIDProvidersAuthorizer();

    HasAuthorization getUsersGroupsAuthorizer();

    HasAuthorization getIdentifiersAuthorizer();

    void showDataDatabasesAlert(boolean visible);

    void setUsersGroupsHistoryToken(String historyToken);

    void setProfilesHistoryToken(String historyToken);

    void setIDProvidersHistoryToken(String historyToken);

    void setIdentifiersMappingsHistoryToken(String historyToken);

    void setDatabasesHistoryToken(String historyToken);

    void setIndexHistoryToken(String historyToken);

    void setRHistoryToken(String historyToken);

    void setDataShieldHistoryToken(String historyToken);

    void setReportsHistoryToken(String historyToken);

    void setFilesHistoryToken(String historyToken);

    void setTasksHistoryToken(String historyToken);

    void setJavaHistoryToken(String historyToken);

    void setPluginsHistoryToken(String historyToken);

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
        .forResource(UriBuilders.ID_PROVIDERS.create().build()).get()
        .authorize(composeAuthorizer(getView().getIDProvidersAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SERVICE_R.create().build()).get()
        .authorize(composeAuthorizer(getView().getRAuthorizer())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATASHIELD_PACKAGES.create().build()).get()
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
        .forResource(UriBuilders.PLUGINS.create().build()).get()
        .authorize(composeAuthorizer(getView().getPluginsAuthorizer())).send();

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
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
        .forResource(UriBuilders.DATABASES_FOR_STORAGE.create().build()) //
        .withCallback(new ResourceCallback<JsArray<DatabaseDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatabaseDto> resource) {
            getView().showDataDatabasesAlert(response.getStatusCode() != Response.SC_OK || "[]".equals(response.getText()));
          }
        }).get().send();
  }

  private void setHistoryTokens(TokenFormatter tokenFormatter) {
    PlaceRequest adminPlace = createRequest(Places.ADMINISTRATION);
    getView().setUsersGroupsHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.USERS));
    getView().setProfilesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.PROFILES));
    getView().setIDProvidersHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.ID_PROVIDERS));
    getView().setDatabasesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.DATABASES));
    getView().setIndexHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.INDEX));
    getView().setRHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.R));
    getView().setIdentifiersMappingsHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.IDENTIFIERS));
    getView().setFilesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.FILES));
    getView().setTasksHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.TASKS));
    getView().setDataShieldHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.DATASHIELD));
    getView().setReportsHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.REPORT_TEMPLATES));
    getView().setJavaHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.JVM));
    getView().setPluginsHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.PLUGINS));
    getView().setServerHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.SERVER));
    getView().setTaxonomiesHistoryToken(getHistoryToken(tokenFormatter, adminPlace, Places.TAXONOMIES));
  }

  private String getHistoryToken(TokenFormatter tokenFormatter, PlaceRequest adminPlace, String place) {
    return tokenFormatter.toHistoryToken(Lists.newArrayList(adminPlace, createRequest(place)));
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
