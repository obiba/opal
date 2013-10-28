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
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceDataProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.opal.AclAction;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class DataDatabasesPresenter extends PresenterWidget<DataDatabasesPresenter.Display>
    implements RequestAdministrationPermissionEvent.Handler, DataDatabasesUiHandlers {

  private final ModalProvider<SqlDatabasePresenter> sqlDatabaseModalProvider;

  private final ModalProvider<MongoDatabasePresenter> mongoDatabaseModalProvider;

  private final AuthorizationPresenter authorizationPresenter;

  private final ResourceDataProvider<DatabaseDto> resourceSqlDatabasesProvider = new ResourceDataProvider<DatabaseDto>(
      DatabaseResources.sqlDatabases());

  private final ResourceDataProvider<DatabaseDto> resourceMongoDbProvider = new ResourceDataProvider<DatabaseDto>(
      DatabaseResources.mongoDatabases());

  private Command confirmedCommand;

  @Inject
  public DataDatabasesPresenter(Display display, EventBus eventBus,
      ModalProvider<SqlDatabasePresenter> sqlDatabaseModalProvider,
      ModalProvider<MongoDatabasePresenter> mongoDatabaseModalProvider,
      Provider<AuthorizationPresenter> authorizationPresenter) {
    super(eventBus, display);
    this.sqlDatabaseModalProvider = sqlDatabaseModalProvider.setContainer(this);
    this.mongoDatabaseModalProvider = mongoDatabaseModalProvider.setContainer(this);
    this.authorizationPresenter = authorizationPresenter.get();
    getView().setUiHandlers(this);
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.sqlDatabases()) //
        .authorize(event == null
            ? new ListSqlDatabasesAuthorization()
            : new CompositeAuthorizer(event.getHasAuthorization(), new ListSqlDatabasesAuthorization())) //
        .get().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.mongoDatabases()) //
        .authorize(event == null
            ? new ListMongoDbAuthorization()
            : new CompositeAuthorizer(event.getHasAuthorization(), new ListMongoDbAuthorization())) //
        .get().send();
  }

  @Override
  protected void onReveal() {
    refresh();
  }

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
  protected void onBind() {
    super.onBind();

    addRegisteredHandler(DatabaseCreatedEvent.getType(), new DatabaseCreatedEvent.DatabaseCreatedHandler() {
      @Override
      public void onDatabaseCreated(DatabaseCreatedEvent event) {
        if(!event.getDto().getUsedForIdentifiers()) {
          refresh();
        }
      }
    });
    addRegisteredHandler(DatabaseUpdatedEvent.getType(), new DatabaseUpdatedEvent.DatabaseUpdatedHandler() {
      @Override
      public void onDatabaseUpdated(DatabaseUpdatedEvent event) {
        if(!event.getDto().getUsedForIdentifiers()) {
          refresh();
        }
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {

      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if(event.getSource() == confirmedCommand && event.isConfirmed()) {
          confirmedCommand.execute();
        }
      }
    });

    getView().getActions().setActionHandler(new ActionHandler<DatabaseDto>() {

      @Override
      public void doAction(DatabaseDto dto, String actionName) {
        if(dto.getEditable() && actionName.equalsIgnoreCase(DELETE_ACTION)) {

          getEventBus().fireEvent(ConfirmationRequiredEvent
              .createWithKeys(confirmedCommand = new DeleteDatabaseCommand(dto), "deleteDatabase",
                  "confirmDeleteDatabase"));

        } else if(dto.getEditable() && actionName.equalsIgnoreCase(EDIT_ACTION)) {

          if(dto.hasSqlSettings()) {
            sqlDatabaseModalProvider.get().editDatabase(dto);
          } else if(dto.hasMongoDbSettings()) {
            mongoDatabaseModalProvider.get().editDatabase(dto);
          }

        } else if(actionName.equalsIgnoreCase(Display.TEST_ACTION)) {
          DatabaseAdministrationPresenter.testConnection(getEventBus(), dto.getName());
        }
      }

    });

    authorizationPresenter
        .setAclRequest("databases", new AclRequest(AclAction.DATABASES_ALL, DatabaseResources.databases()));
  }

  private void refresh() {
    getView().getSqlTable().setVisibleRangeAndClearData(new Range(0, 10), true);
    getView().getMongoTable().setVisibleRangeAndClearData(new Range(0, 10), true);
  }

  @Override
  public void createSql(boolean storageOnly) {
    sqlDatabaseModalProvider.get().createNewDatabase(storageOnly);
  }

  @Override
  public void createMongo(boolean storageOnly) {
    mongoDatabaseModalProvider.get().createNewDatabase(storageOnly);
  }

  public interface Display extends View, HasUiHandlers<DataDatabasesUiHandlers> {

    String TEST_ACTION = "Test";

    HasActionHandler<DatabaseDto> getActions();

    HasData<DatabaseDto> getSqlTable();

    HasData<DatabaseDto> getMongoTable();

  }

  private class DeleteDatabaseCommand implements Command {

    private final DatabaseDto dto;

    private DeleteDatabaseCommand(DatabaseDto dto) {
      this.dto = dto;
    }

    @Override
    public void execute() {
      ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
          .forResource(DatabaseResources.database(dto.getName())) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              refresh();
            }

          }) //
          .delete().send();
    }
  }

  private final class ListSqlDatabasesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      // Only bind the table to its data provider if we're authorized
      if(resourceSqlDatabasesProvider.getDataDisplays().isEmpty()) {
        resourceSqlDatabasesProvider.addDataDisplay(getView().getSqlTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }

  private final class ListMongoDbAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      // Only bind the table to its data provider if we're authorized
      if(resourceMongoDbProvider.getDataDisplays().isEmpty()) {
        resourceMongoDbProvider.addDataDisplay(getView().getMongoTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }

}
