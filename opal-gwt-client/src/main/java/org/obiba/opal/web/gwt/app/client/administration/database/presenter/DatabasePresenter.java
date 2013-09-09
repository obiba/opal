/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.DatabaseDto;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;
import org.obiba.opal.web.model.client.opal.SqlDatabaseDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class DatabasePresenter extends ModalPresenterWidget<DatabasePresenter.Display> implements DatabaseUiHandlers {

  public enum Mode {
    CREATE, UPDATE
  }

  private Mode dialogMode;

  private ValidationHandler methodValidationHandler;

  @Inject
  public DatabasePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    switch(dialogMode) {
      case CREATE:
        createDatabase();
        break;
      case UPDATE:
        updateDatabase();
        break;
    }
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);

    ResourceRequestBuilderFactory.<JsArray<JdbcDriverDto>>newBuilder().forResource(DatabaseResources.drivers())
        .withCallback(new ResourceCallback<JsArray<JdbcDriverDto>>() {

          @Override
          public void onResource(Response response, JsArray<JdbcDriverDto> resource) {
            getView().setAvailableDrivers(resource);
          }
        }).get().send();

    methodValidationHandler = new MethodValidationHandler();
  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  /**
   * Setup the dialog for creating a method
   */
  public void createNewDatabase() {
    setDialogMode(Mode.CREATE);
  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
  public void updateDatabase(DatabaseDto dto) {
    setDialogMode(Mode.UPDATE);
    displayDatabase(dto);
  }

  private void displayDatabase(DatabaseDto dto) {
    SqlDatabaseDto sqlDatabaseDto = (SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings);
    getView().getName().setText(dto.getName());
    getView().getUsage().setText(dto.getType());
    getView().getDriver().setText(sqlDatabaseDto.getDriverClass());
    getView().getDefaultStorage().setValue(dto.getDefaultStorage());
    getView().getUrl().setText(sqlDatabaseDto.getUrl());
    getView().getUsername().setText(sqlDatabaseDto.getUsername());
    getView().getPassword().setText(sqlDatabaseDto.getPassword());
    getView().getProperties().setText(sqlDatabaseDto.getProperties());
    getView().getSQLSchema().setText(sqlDatabaseDto.getMagmaDatasourceType());
  }

  private DatabaseDto getDto() {
    DatabaseDto dto = DatabaseDto.create();
    SqlDatabaseDto sqlDto = SqlDatabaseDto.create();

    dto.setName(getView().getName().getText());
    dto.setType(getView().getUsage().getText());
    dto.setDefaultStorage(getView().getDefaultStorage().getValue());

    sqlDto.setUrl(getView().getUrl().getText());
    sqlDto.setDriverClass(getView().getDriver().getText());
    sqlDto.setUsername(getView().getUsername().getText());
    sqlDto.setPassword(getView().getPassword().getText());
    sqlDto.setProperties(getView().getProperties().getText());
    sqlDto.setMagmaDatasourceType(getView().getSQLSchema().getText());

    dto.setExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings, sqlDto);
    return dto;
  }

  private void updateDatabase() {
    if(methodValidationHandler.validate()) {
      DatabaseDto dto = getDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder().forResource(DatabaseResources.database(dto.getName())) //
          .put() //
          .withResourceBody(DatabaseDto.stringify(dto)) //
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_CREATED, callbackHandler) //
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private void createDatabase() {
    if(methodValidationHandler.validate()) {
      DatabaseDto dto = getDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder().forResource(DatabaseResources.databases()) //
          .post() //
          .withResourceBody(DatabaseDto.stringify(dto)) //
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_CREATED, callbackHandler) //
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private class MethodValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators
            .add(new RequiredTextValidator(getView().getName(), "NameIsRequired").setId(Display.FormField.NAME.name()));
        validators
            .add(new RequiredTextValidator(getView().getUrl(), "UrlIsRequired").setId(Display.FormField.URL.name()));
        validators.add(new RequiredTextValidator(getView().getUsername(), "UsernameIsRequired")
            .setId(Display.FormField.USERNAME.name()));
        validators.add(new RequiredTextValidator(getView().getPassword(), "PasswordIsRequired")
            .setId(Display.FormField.PASSWORD.name()));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  private class CreateOrUpdateCallBack implements ResponseCodeCallback {

    private final DatabaseDto dto;

    private CreateOrUpdateCallBack(DatabaseDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      Event<?> event = null;
      switch(response.getStatusCode()) {
        case Response.SC_OK:
          event = new DatabaseUpdatedEvent(dto);
          break;
        case Response.SC_CREATED:
          event = new DatabaseCreatedEvent(dto);
          break;
        default:
          //TODO supports DatabaseAlreadyExists
          event = NotificationEvent.newBuilder().error(response.getText()).build();
      }
      getEventBus().fireEvent(event);
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DatabaseUiHandlers> {

    enum FormField {
      NAME,
      URL,
      USERNAME,
      PASSWORD
    }

    void hideDialog();

    void setAvailableDrivers(JsArray<JdbcDriverDto> resource);

    void setDialogMode(Mode dialogMode);

    void showError(FormField formField, String message);

    HasText getName();

    HasText getUsage();

    HasText getSQLSchema();

    HasText getUrl();

    HasText getDriver();

    HasText getUsername();

    HasText getPassword();

    HasText getProperties();

    HasValue<Boolean> getDefaultStorage();

  }

}
