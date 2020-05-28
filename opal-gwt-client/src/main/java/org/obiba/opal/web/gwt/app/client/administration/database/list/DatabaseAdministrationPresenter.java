/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.list;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.list.data.DataDatabasesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.list.identifiers.IdentifiersDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
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

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public DatabaseAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      IdentifiersDatabasePresenter identifiersDatabasePresenter, DataDatabasesPresenter dataDatabasesPresenter,
      BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy);
    this.identifiersDatabasePresenter = identifiersDatabasePresenter;
    this.dataDatabasesPresenter = dataDatabasesPresenter;
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
        .forResource(UriBuilders.DATABASES_SQL.create().build()) //
        .authorize(authorizer) //
        .get().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.DATABASES_MONGO_DB.create().build()) //
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

    addRegisteredHandler(DatabaseCreatedEvent.getType(), new DatabaseCreatedEvent.DatabaseCreatedHandler() {
      @Override
      public void onDatabaseCreated(DatabaseCreatedEvent event) {
        testConnection(event.getDto());
      }
    });

    addRegisteredHandler(DatabaseUpdatedEvent.getType(), new DatabaseUpdatedEvent.DatabaseUpdatedHandler() {
      @Override
      public void onDatabaseUpdated(DatabaseUpdatedEvent event) {
        testConnection(event.getDto());
      }
    });

  }

  public static void testConnection(EventBus eventBus, String database) {
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
        .forResource(UriBuilders.DATABASE_CONNECTIONS.create().build(database)) //
        .withCallback(Response.SC_OK, new TestConnectionSuccessCallback(eventBus, database)) //
        .withCallback(Response.SC_SERVICE_UNAVAILABLE, new TestConnectionFailCallback(eventBus)) //
        .post().send();
  }

  /**
   * Test connection triggered by the event handler
   */
  private void testConnection(DatabaseDto dto) {
    if(dto.getUsedForIdentifiers()) {
      identifiersDatabasePresenter.testConnection();
    } else {
      dataDatabasesPresenter.testConnection(dto);
    }
  }

  public interface Display extends View, HasBreadcrumbs {

  }

  static class TestConnectionSuccessCallback implements ResponseCodeCallback {

    private final EventBus eventBus;

    private final String database;

    TestConnectionSuccessCallback(EventBus eventBus, String database) {
      this.eventBus = eventBus;
      this.database = "_identifiers".equals(database) ? "identifiers" : database;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(NotificationEvent.newBuilder().info("DatabaseConnectionOk").args(database).build());
    }
  }

  static class TestConnectionFailCallback implements ResponseCodeCallback {

    private final EventBus eventBus;

    TestConnectionFailCallback(EventBus eventBus) {
      this.eventBus = eventBus;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
      eventBus
          .fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).args(error.getArgumentsArray()).build());
    }
  }

}
