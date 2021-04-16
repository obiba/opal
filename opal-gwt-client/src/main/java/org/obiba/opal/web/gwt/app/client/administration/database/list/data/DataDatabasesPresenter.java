/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.list.data;

import org.obiba.opal.web.gwt.app.client.administration.database.edit.mongo.MongoDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.sql.SqlDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.list.DatabaseAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceDataProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class DataDatabasesPresenter extends PresenterWidget<DataDatabasesPresenter.Display>
    implements RequestAdministrationPermissionEvent.Handler, DataDatabasesUiHandlers {

  private final ModalProvider<SqlDatabaseModalPresenter> sqlDatabaseModalProvider;

  private final ModalProvider<MongoDatabaseModalPresenter> mongoDatabaseModalProvider;

  private final ResourceDataProvider<DatabaseDto> resourceDatabasesProvider = new ResourceDataProvider<>(
      UriBuilders.DATABASES_WITH_SETTINGS.create().build());

  private Command confirmedCommand;

  private final TranslationMessages translationMessages;

  @Inject
  public DataDatabasesPresenter(Display display, EventBus eventBus,
      ModalProvider<SqlDatabaseModalPresenter> sqlDatabaseModalProvider,
      ModalProvider<MongoDatabaseModalPresenter> mongoDatabaseModalProvider, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.sqlDatabaseModalProvider = sqlDatabaseModalProvider.setContainer(this);
    this.mongoDatabaseModalProvider = mongoDatabaseModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.DATABASES.create().build()) //
        .authorize(event == null
            ? new ListDatabasesAuthorization()
            : new CompositeAuthorizer(event.getHasAuthorization(), new ListDatabasesAuthorization())) //
        .get().send();
  }

  @Override
  protected void onReveal() {
    refresh();
  }

  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.DATABASES.create().build()) //
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
  }

  private void refresh() {
    getView().getTable().setVisibleRangeAndClearData(new Range(0, 10), true);
  }

  @Override
  public void createSql(boolean storageOnly) {
    sqlDatabaseModalProvider.get().createNewDatabase(storageOnly);
  }

  @Override
  public void createMongo(boolean storageOnly) {
    mongoDatabaseModalProvider.get().createNewDatabase(storageOnly);
  }

  @Override
  public void edit(DatabaseDto dto) {
    if(dto.hasSqlSettings()) {
      sqlDatabaseModalProvider.get().editDatabase(dto);
    } else if(dto.hasMongoDbSettings()) {
      mongoDatabaseModalProvider.get().editDatabase(dto);
    }
  }

  @Override
  public void testConnection(DatabaseDto dto) {
    DatabaseAdministrationPresenter.testConnection(getEventBus(), dto.getName());
  }

  @Override
  public void deleteDatabase(DatabaseDto dto) {
    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(confirmedCommand = new DeleteDatabaseCommand(dto), translationMessages.unregisterDatabase(),
            translationMessages.confirmDeleteDatabase()));
  }

  public interface Display extends View, HasUiHandlers<DataDatabasesUiHandlers> {

    HasData<DatabaseDto> getTable();
  }

  private class DeleteDatabaseCommand implements Command {

    private final DatabaseDto dto;

    private DeleteDatabaseCommand(DatabaseDto dto) {
      this.dto = dto;
    }

    @Override
    public void execute() {
      ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
          .forResource(UriBuilders.DATABASE.create().build(dto.getName())) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(new ConfirmationTerminatedEvent());
              refresh();
              getEventBus().fireEvent(new DatabaseDeletedEvent(dto));
            }

          }) //
          .delete().send();
    }
  }

  private final class ListDatabasesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      // Only bind the table to its data provider if we're authorized
      if(resourceDatabasesProvider.getDataDisplays().isEmpty()) {
        resourceDatabasesProvider.addDataDisplay(getView().getTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }
}
