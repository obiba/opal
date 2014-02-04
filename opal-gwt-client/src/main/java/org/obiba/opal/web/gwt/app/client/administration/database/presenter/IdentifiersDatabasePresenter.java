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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.MongoDbSettingsDto;
import org.obiba.opal.web.model.client.database.SqlSettingsDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class IdentifiersDatabasePresenter extends PresenterWidget<IdentifiersDatabasePresenter.Display>
    implements IdentifiersDatabaseUiHandlers, RequestAdministrationPermissionEvent.Handler {

  public static final String IDENTIFIERS_DATABASE_NAME = "_identifiers";

  private final ModalProvider<SqlDatabaseModalPresenter> sqlDatabaseModalProvider;

  private final ModalProvider<MongoDatabaseModalPresenter> mongoDatabaseModalProvider;

  private DatabaseDto databaseDto;

  @Inject
  public IdentifiersDatabasePresenter(Display display, EventBus eventBus,
      ModalProvider<SqlDatabaseModalPresenter> sqlDatabaseModalProvider,
      ModalProvider<MongoDatabaseModalPresenter> mongoDatabaseModalProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.sqlDatabaseModalProvider = sqlDatabaseModalProvider.setContainer(this);
    this.mongoDatabaseModalProvider = mongoDatabaseModalProvider.setContainer(this);
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  protected void onReveal() {
    refresh();
  }

  @Override
  protected void onBind() {
    super.onBind();

    addRegisteredHandler(DatabaseCreatedEvent.getType(), new DatabaseCreatedEvent.DatabaseCreatedHandler() {
      @Override
      public void onDatabaseCreated(DatabaseCreatedEvent event) {
        DatabaseDto dto = event.getDto();
        if(dto.getUsedForIdentifiers()) {
          databaseDto = dto;
          getView().setDatabase(dto);
          getView().enableEditionDeletion(true);
        }
      }
    });
    addRegisteredHandler(DatabaseUpdatedEvent.getType(), new DatabaseUpdatedEvent.DatabaseUpdatedHandler() {
      @Override
      public void onDatabaseUpdated(DatabaseUpdatedEvent event) {
        DatabaseDto dto = event.getDto();
        if(dto.getUsedForIdentifiers()) {
          databaseDto = dto;
          getView().setDatabase(dto);
          refreshDeletionCapability();
        }
      }
    });
  }

  @Override
  public void createSql() {
    DatabaseDto dto = createDefaultIdentifiersDatabaseDto();
    SqlSettingsDto sqlSettingsDto = SqlSettingsDto.create();
    sqlSettingsDto.setSqlSchema(SqlSettingsDto.SqlSchema.HIBERNATE);
    dto.setSqlSettings(sqlSettingsDto);
    sqlDatabaseModalProvider.get().createNewIdentifierDatabase(dto);
  }

  @Override
  public void createMongo() {
    DatabaseDto dto = createDefaultIdentifiersDatabaseDto();
    dto.setMongoDbSettings(MongoDbSettingsDto.create());
    mongoDatabaseModalProvider.get().createNewIdentifierDatabase(dto);
  }

  @Override
  public void edit() {
    if(databaseDto.hasSqlSettings()) {
      sqlDatabaseModalProvider.get().editDatabase(databaseDto);
    } else if(databaseDto.hasMongoDbSettings()) {
      mongoDatabaseModalProvider.get().editDatabase(databaseDto);
    }
  }

  @Override
  public void testConnection() {
    DatabaseAdministrationPresenter.testConnection(getEventBus(), IDENTIFIERS_DATABASE_NAME);
  }

  @Override
  public void deleteDatabase() {
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
        .forResource(UriBuilders.DATABASE.create().build(databaseDto.getName())) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            refresh();
            getEventBus().fireEvent(new DatabaseDeletedEvent(databaseDto));
          }

        }) //
        .delete().send();
  }

  private DatabaseDto createDefaultIdentifiersDatabaseDto() {
    DatabaseDto dto = DatabaseDto.create();
    dto.setUsedForIdentifiers(true);
    dto.setName(IDENTIFIERS_DATABASE_NAME);
    dto.setUsage(DatabaseDto.Usage.STORAGE);
    dto.setDefaultStorage(false);
    return dto;
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<DatabaseDto>newBuilder() //
        .forResource(UriBuilders.DATABASE_IDENTIFIERS.create().build()) //
        .withCallback(new ResourceCallback<DatabaseDto>() {
          @Override
          public void onResource(Response response, @Nullable DatabaseDto dto) {
            databaseDto = dto;
            getView().setDatabase(dto);
          }
        }) //
        .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            databaseDto = null;
            getView().setDatabase(null);
          }
        }).get().send();

    refreshDeletionCapability();
  }

  private void refreshDeletionCapability() {
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder() //
        .forResource("/identifiers/tables?counts=true") //
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, @Nullable JsArray<TableDto> tables) {
            for(TableDto table : JsArrays.toIterable(tables)) {
              if(table.getValueSetCount() > 0) {
                  getView().enableEditionDeletion(false);
                return;
              }
            }
            getView().enableEditionDeletion(true);
          }
        }) //
        .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().enableEditionDeletion(false);
          }
        }).get().send();
  }

  public interface Display extends View, HasUiHandlers<IdentifiersDatabaseUiHandlers> {

    void setDatabase(@Nullable DatabaseDto database);

    void enableEditionDeletion(boolean value);
  }

}
