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

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceDataProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.DatabaseDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class DatabaseAdministrationPresenter extends
    ItemAdministrationPresenter<DatabaseAdministrationPresenter.Display, DatabaseAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.DATA_DATABASES)
  public interface Proxy extends ProxyPlace<DatabaseAdministrationPresenter> {}

  public interface Display extends View, HasBreadcrumbs {

    String TEST_ACTION = "Test";

    enum Slots {
      DRIVERS, HEADER
    }

    HasActionHandler<DatabaseDto> getActions();

    HasClickHandlers getAddButton();

    HasData<DatabaseDto> getDatabaseTable();
  }

  private final ModalProvider<DatabasePresenter> databaseModalProvider;

  private final AuthorizationPresenter authorizationPresenter;

  private final ResourceDataProvider<DatabaseDto> resourceDataProvider = new ResourceDataProvider<DatabaseDto>(
      DatabaseResources.sqlDatabases());

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  private Command confirmedCommand;

  @Inject
  public DatabaseAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<DatabasePresenter> databaseModalProvider, Provider<AuthorizationPresenter> authorizationPresenter,
      BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy);
    this.databaseModalProvider = databaseModalProvider.setContainer(this);
    this.authorizationPresenter = authorizationPresenter.get();
    this.breadcrumbsBuilder = breadcrumbsBuilder;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.sqlDatabases()) //
        .get() //
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListDatabasesAuthorization())) //
        .send();
  }

  @Override
  public String getName() {
    return "Data Databases";
  }

  @Override
  protected void onReveal() {
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
    refresh();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.sqlDatabases()) //
        .get() //
        .authorize(authorizer) //
        .send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageDataDatabasesTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();

    registerHandler(getEventBus().addHandler(DatabaseCreatedEvent.getType(), new DatabaseCreatedEvent.Handler() {

      @Override
      public void onCreated(DatabaseCreatedEvent event) {
        refresh();
      }
    }));
    registerHandler(getEventBus().addHandler(DatabaseUpdatedEvent.getType(), new DatabaseUpdatedEvent.Handler() {

      @Override
      public void onUpdated(DatabaseUpdatedEvent event) {
        refresh();
      }
    }));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {

      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if(event.getSource() == confirmedCommand && event.isConfirmed()) {
          confirmedCommand.execute();
        }
      }
    }));

    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs());

    getView().getActions().setActionHandler(new ActionHandler<DatabaseDto>() {

      @Override
      public void doAction(final DatabaseDto dto, String actionName) {
        if(dto.getEditable() && actionName.equalsIgnoreCase(DELETE_ACTION)) {
          getEventBus().fireEvent(ConfirmationRequiredEvent.createWithKeys(confirmedCommand = new Command() {
            @Override
            public void execute() {
              deleteDatabase(dto);
            }

            private void deleteDatabase(DatabaseDto database) {
              ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder()
                  .forResource(DatabaseResources.database(database.getName()))
                  .withCallback(Response.SC_OK, new ResponseCodeCallback() {

                    @Override
                    public void onResponseCode(Request request, Response response) {
                      refresh();
                    }

                  }).delete().send();
            }

          }, "deleteDatabase", "confirmDeleteDatabase"));
        } else if(dto.getEditable() && actionName.equalsIgnoreCase(EDIT_ACTION)) {
          databaseModalProvider.get().updateDatabase(dto);
        } else if(actionName.equalsIgnoreCase(Display.TEST_ACTION)) {
          ResponseCodeCallback callback = new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == Response.SC_OK) {
                getEventBus()
                    .fireEvent(NotificationEvent.Builder.newNotification().info("DatabaseConnectionOk").build());
              } else {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getEventBus().fireEvent(
                    NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                        .build());
              }
            }

          };
          ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder()//
              .forResource(DatabaseResources.database(dto.getName(), "connections")).accept("application/json")//
              .withCallback(Response.SC_OK, callback).withCallback(Response.SC_SERVICE_UNAVAILABLE, callback).post()
              .send();
        }
      }

    });

    registerHandler(getView().getAddButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        DatabasePresenter dialog = databaseModalProvider.get();
        dialog.createNewDatabase();
      }

    }));

    authorizationPresenter
        .setAclRequest("databases", new AclRequest(AclAction.DATABASES_ALL, DatabaseResources.sqlDatabases()));
  }

  private void refresh() {
    getView().getDatabaseTable().setVisibleRangeAndClearData(new Range(0, 10), true);
  }

  private final class ListDatabasesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      // Only bind the table to its data provider if we're authorized
      if(resourceDataProvider.getDataDisplays().isEmpty()) {
        resourceDataProvider.addDataDisplay(getView().getDatabaseTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }

}
