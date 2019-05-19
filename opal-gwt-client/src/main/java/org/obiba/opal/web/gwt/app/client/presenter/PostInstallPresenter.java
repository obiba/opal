/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.list.data.DataDatabasesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.list.identifiers.IdentifiersDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.DatabasesStatusDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class PostInstallPresenter extends Presenter<PostInstallPresenter.Display, PostInstallPresenter.Proxy>
    implements PostInstallUiHandlers {

  private HandlerRegistration notificationRegistration;

  public interface Display extends View, HasUiHandlers<PostInstallUiHandlers> {

    void setUsername(String username);

    void enablePageExit(boolean value);
  }

  @ProxyStandard
  @NameToken(Places.INSTALL)
  public interface Proxy extends ProxyPlace<PostInstallPresenter> {}

  public enum Slot {
    IDENTIFIERS, DATA, NOTIFICATION
  }

  private final PlaceManager placeManager;

  private final RequestCredentials credentials;

  private final NotificationPresenter messageDialog;

  private final IdentifiersDatabasePresenter identifiersDatabasePresenter;

  private final DataDatabasesPresenter dataDatabasesPresenter;

  @SuppressWarnings("ConstructorWithTooManyParameters")
  @Inject
  public PostInstallPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      RequestCredentials credentials, NotificationPresenter messageDialog,
      IdentifiersDatabasePresenter identifiersDatabasePresenter, DataDatabasesPresenter dataDatabasesPresenter) {
    super(eventBus, display, proxy, RevealType.Root);
    this.placeManager = placeManager;
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.identifiersDatabasePresenter = identifiersDatabasePresenter;
    this.dataDatabasesPresenter = dataDatabasesPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    setInSlot(Slot.IDENTIFIERS, identifiersDatabasePresenter);
    identifiersDatabasePresenter.onAdministrationPermissionRequest(null);
    setInSlot(Slot.DATA, dataDatabasesPresenter);
    dataDatabasesPresenter.onAdministrationPermissionRequest(null);

    addNotificationHandler();

    addRegisteredHandler(DatabaseCreatedEvent.getType(), new DatabaseCreatedEvent.DatabaseCreatedHandler() {
      @Override
      public void onDatabaseCreated(DatabaseCreatedEvent event) {
        updateView();
        testConnection(event.getDto());
      }
    });

    addRegisteredHandler(DatabaseUpdatedEvent.getType(), new DatabaseUpdatedEvent.DatabaseUpdatedHandler() {
      @Override
      public void onDatabaseUpdated(DatabaseUpdatedEvent event) {
        updateView();
        testConnection(event.getDto());
      }
    });

    addRegisteredHandler(DatabaseDeletedEvent.getType(), new DatabaseDeletedEvent.DatabaseDeletedHandler() {
      @Override
      public void onDatabaseDeleted(DatabaseDeletedEvent event) {
        updateView();
      }
    });
  }

  private void addNotificationHandler() {
    notificationRegistration = getEventBus().addHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        if(isVisible()) {
          messageDialog.setNotification(event);
          setInSlot(Slot.NOTIFICATION, messageDialog);
        }
      }
    });
  }

  @Override
  protected void onHide() {
    super.onHide();
    notificationRegistration.removeHandler();
    messageDialog.close();
  }

  private void testConnection(DatabaseDto dto) {
    if(dto.getUsedForIdentifiers()) {
      identifiersDatabasePresenter.testConnection();
    } else {
      dataDatabasesPresenter.testConnection(dto);
    }
  }

  @Override
  protected void onReveal() {
    getView().setUsername(credentials.getUsername());
    updateView();
  }

  private void updateView() {
    ResourceRequestBuilderFactory.<DatabasesStatusDto>newBuilder()
        .forResource(UriBuilders.SYSTEM_STATUS_DATABASES.create().build()).get()
        .withCallback(new DatabasesStatusResourceCallback())//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            placeManager.revealCurrentPlace();
          }
        }, Response.SC_FORBIDDEN)//
        .send();

  }

  @Override
  public void onQuit() {
    fireEvent(new SessionEndedEvent());
  }

  @Override
  public void onGoToMain() {
    ResourceRequestBuilderFactory.<DatabasesStatusDto>newBuilder()
        .forResource(UriBuilders.SYSTEM_STATUS_DATABASES.create().build()).get()
        .withCallback(new ResourceCallback<DatabasesStatusDto>() {
          @Override
          public void onResource(Response response, DatabasesStatusDto resource) {
            if(resource.getHasIdentifiers() && resource.getHasStorage()) {
              placeManager.revealDefaultPlace();
            } else {
              fireEvent(NotificationEvent.newBuilder().error("PostInstallNotCompleted.").build());
            }
          }
        }).send();
  }

  private class DatabasesStatusResourceCallback implements ResourceCallback<DatabasesStatusDto> {
    @Override
    public void onResource(Response response, final DatabasesStatusDto statusDto) {
      // Test the connection to identifiers database
      ResourceRequestBuilderFactory.<DatabasesStatusDto>newBuilder()
          .forResource(UriBuilders.DATABASE_IDENTIFIERS_CONNECTIONS.create().build()).post()
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getView().enablePageExit(statusDto.getHasIdentifiers() && statusDto.getHasStorage() &&
                  response.getStatusCode() == Response.SC_OK);
            }
          }, Response.SC_OK, Response.SC_BAD_REQUEST, Response.SC_NOT_FOUND, Response.SC_INTERNAL_SERVER_ERROR,
              Response.SC_SERVICE_UNAVAILABLE)//
          .send();
    }
  }

}
