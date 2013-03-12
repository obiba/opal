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
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceDataProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.EDIT_ACTION;

public class DatabaseAdministrationPresenter extends
    ItemAdministrationPresenter<DatabaseAdministrationPresenter.Display, DatabaseAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken("!admin.databases")
  @TabInfo(container = AdministrationPresenter.class, label = "Databases", priority = 1)
  public interface Proxy extends TabContentProxyPlace<DatabaseAdministrationPresenter> {}

  public interface Display extends View {

    String TEST_ACTION = "Test";

    enum Slots {
      Drivers, Permissions
    }

    HasActionHandler<JdbcDataSourceDto> getActions();

    HasClickHandlers getAddButton();

    HasAuthorization getPermissionsAuthorizer();

    HasData<JdbcDataSourceDto> getDatabaseTable();

  }

  private final Provider<DatabasePresenter> jdbcDataSourcePresenter;

  private final AuthorizationPresenter authorizationPresenter;

  private final ResourceDataProvider<JdbcDataSourceDto> resourceDataProvider
      = new ResourceDataProvider<JdbcDataSourceDto>(Resources.databases());

  private Command confirmedCommand;

  @Inject
  public DatabaseAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      Provider<DatabasePresenter> jdbcDataSourcePresenter, Provider<AuthorizationPresenter> authorizationPresenter) {
    super(eventBus, display, proxy);
    this.jdbcDataSourcePresenter = jdbcDataSourcePresenter;
    this.authorizationPresenter = authorizationPresenter.get();
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.databases()).post()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListDatabasesAuthorization())).send();
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, AdministrationPresenter.TabSlot, this);
  }

  @Override
  public String getName() {
    return "Databases";
  }

  @Override
  protected void onReveal() {
    refresh();
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.databases()).post()
        .authorize(authorizer).send();
  }

  @Override
  protected void onBind() {

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

    getView().getActions().setActionHandler(new ActionHandler<JdbcDataSourceDto>() {

      @Override
      public void doAction(final JdbcDataSourceDto object, String actionName) {
        if(object.getEditable() && actionName.equalsIgnoreCase(DELETE_ACTION)) {
          getEventBus().fireEvent(ConfirmationRequiredEvent.createWithKeys(confirmedCommand = new Command() {
            @Override
            public void execute() {
              deleteDatabase(object);
            }
          }, "deleteDatabase", "confirmDeleteDatabase"));
        } else if(object.getEditable() && actionName.equalsIgnoreCase(EDIT_ACTION)) {
          DatabasePresenter dialog = jdbcDataSourcePresenter.get();
          dialog.updateDatabase(object);
          addToPopupSlot(dialog);
        } else if(actionName.equalsIgnoreCase(Display.TEST_ACTION)) {
          ResponseCodeCallback callback = new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == 200) {
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
          ResourceRequestBuilderFactory.<JsArray<JdbcDataSourceDto>>newBuilder()//
              .forResource(Resources.database(object.getName(), "connections")).accept("application/json")//
              .withCallback(200, callback).withCallback(503, callback).post().send();
        }
      }

    });

    registerHandler(getView().getAddButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        DatabasePresenter dialog = jdbcDataSourcePresenter.get();
        dialog.createNewDatabase();
        addToPopupSlot(dialog);
      }

    }));

    authorizationPresenter.setAclRequest("databases", new AclRequest(AclAction.DATABASES_ALL, Resources.databases()));
  }

  private void deleteDatabase(JdbcDataSourceDto database) {
    ResourceRequestBuilderFactory.<JsArray<JdbcDataSourceDto>>newBuilder()
        .forResource(Resources.database(database.getName())).withCallback(200, new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        refresh();
      }

    }).delete().send();
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
      if(resourceDataProvider.getDataDisplays().size() == 0) {
        resourceDataProvider.addDataDisplay(getView().getDatabaseTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }

  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(Display.Slots.Permissions);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      setInSlot(Display.Slots.Permissions, authorizationPresenter);
    }
  }

}
