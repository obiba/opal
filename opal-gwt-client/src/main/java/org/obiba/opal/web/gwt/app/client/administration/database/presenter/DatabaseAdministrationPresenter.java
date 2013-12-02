/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class DatabaseAdministrationPresenter extends
    ItemAdministrationPresenter<DatabaseAdministrationPresenter.Display, DatabaseAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.DATABASES)
  public interface Proxy extends ProxyPlace<DatabaseAdministrationPresenter> {}

  public enum Slot {
    IDENTIFIERS, DATA
  }

  private final IdentifiersDatabasePresenter identifiersDatabasePresenter;

  private final DataDatabasesPresenter dataDatabasesPresenter;

  private final AuthorizationPresenter authorizationPresenter;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public DatabaseAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      IdentifiersDatabasePresenter identifiersDatabasePresenter, DataDatabasesPresenter dataDatabasesPresenter,
      Provider<AuthorizationPresenter> authorizationPresenter, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy);
    this.identifiersDatabasePresenter = identifiersDatabasePresenter;
    this.dataDatabasesPresenter = dataDatabasesPresenter;
    this.authorizationPresenter = authorizationPresenter.get();
    this.breadcrumbsBuilder = breadcrumbsBuilder;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    dataDatabasesPresenter.onAdministrationPermissionRequest(event);
    identifiersDatabasePresenter.onAdministrationPermissionRequest(event);
  }

  @Override
  public String getName() {
    return "Databases";
  }

  @Override
  protected void onReveal() {
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.sqlDatabases()) //
        .authorize(authorizer) //
        .get().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.mongoDatabases()) //
        .authorize(authorizer) //
        .get().send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageDatabasesTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs());
    setInSlot(Slot.IDENTIFIERS, identifiersDatabasePresenter);
    setInSlot(Slot.DATA, dataDatabasesPresenter);
    authorizationPresenter
        .setAclRequest("databases", new AclRequest(AclAction.DATABASES_ALL, DatabaseResources.databases()));
  }

  static void testConnection(EventBus eventBus, String database) {
    ResponseCodeCallback testConnectionCallback = new TestConnectionCallback(eventBus, database);
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
        .forResource(DatabaseResources.database(database, "connections")) //
        .withCallback(Response.SC_OK, testConnectionCallback) //
        .withCallback(Response.SC_SERVICE_UNAVAILABLE, testConnectionCallback) //
        .post().send();
  }

  public interface Display extends View, HasBreadcrumbs {

  }

  static class TestConnectionCallback implements ResponseCodeCallback {

    private final EventBus eventBus;

    private String database;

    TestConnectionCallback(EventBus eventBus, String database) {
      this.eventBus = eventBus;
      this.database = database;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      Event<?> event = null;
      if(response.getStatusCode() == Response.SC_OK) {
        event = NotificationEvent.newBuilder().info("DatabaseConnectionOk").args(database).build();
      } else {
        ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
        event = NotificationEvent.newBuilder().error(error.getStatus()).args(error.getArgumentsArray()).build();
      }
      eventBus.fireEvent(event);
    }
  }

}
