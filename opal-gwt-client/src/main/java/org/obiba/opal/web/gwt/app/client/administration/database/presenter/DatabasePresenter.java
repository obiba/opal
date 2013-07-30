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
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class DatabasePresenter extends PresenterWidget<DatabasePresenter.Display>
    implements DatabaseUiHandlers {

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  private MethodValidationHandler methodValidationHandler;

  @Inject
  public DatabasePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    if(dialogMode == Mode.CREATE) {
      createDatabase();
    } else if(dialogMode == Mode.UPDATE) {
      updateDatabase();
    }
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);

    ResourceRequestBuilderFactory.<JsArray<JdbcDriverDto>>newBuilder().forResource(Resources.drivers())
        .withCallback(new ResourceCallback<JsArray<JdbcDriverDto>>() {

          @Override
          public void onResource(Response response, JsArray<JdbcDriverDto> resource) {
            getView().setAvailableDrivers(resource);
          }
        }).get().send();

    methodValidationHandler = new MethodValidationHandler(getEventBus());
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
  public void updateDatabase(JdbcDataSourceDto dto) {
    setDialogMode(Mode.UPDATE);
    displayDatabase(dto.getName(), dto);
  }

  private void displayDatabase(String name, JdbcDataSourceDto dto) {
    getView().getName().setText(name);
    getView().getDriver().setText(dto.getDriverClass());
    getView().getUrl().setText(dto.getUrl());
    getView().getUsername().setText(dto.getUsername());
    getView().getPassword().setText(dto.getPassword());
    getView().getProperties().setText(dto.getProperties());
  }

  private void updateDatabase() {
    if(methodValidationHandler.validate()) {
      putDatabase(getJdbcDataSourceDto());
    }
  }

  private void createDatabase() {
    if(methodValidationHandler.validate()) {
      ResourceRequestBuilderFactory.<JdbcDataSourceDto>newBuilder()
          .forResource(Resources.database(getView().getName().getText())).get()//
          .withCallback(new AlreadyExistMethodCallBack())//
          .withCallback(Response.SC_NOT_FOUND, new CreateMethodCallBack()).send();
    }
  }

  private void postDatabase(JdbcDataSourceDto dto) {
    ResponseCodeCallback callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource(Resources.databases()).post()//
        .withResourceBody(JdbcDataSourceDto.stringify(dto))//
        .withCallback(Response.SC_OK, callbackHandler)//
        .withCallback(Response.SC_CREATED, callbackHandler)//
        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private void putDatabase(JdbcDataSourceDto dto) {
    ResponseCodeCallback callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource(Resources.database(getView().getName().getText())).put()//
        .withResourceBody(JdbcDataSourceDto.stringify(dto))//
        .withCallback(Response.SC_OK, callbackHandler)//
        .withCallback(Response.SC_CREATED, callbackHandler)//
        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private JdbcDataSourceDto getJdbcDataSourceDto() {
    JdbcDataSourceDto dto = JdbcDataSourceDto.create();
    dto.setName(getView().getName().getText());
    dto.setUrl(getView().getUrl().getText());
    dto.setDriverClass(getView().getDriver().getText());
    dto.setUsername(getView().getUsername().getText());
    dto.setPassword(getView().getPassword().getText());
    dto.setProperties(getView().getProperties().getText());
    return dto;
  }

  //
  // Inner classes and interfaces
  //

  private class MethodValidationHandler extends AbstractValidationHandler {

    private MethodValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired"));
        validators.add(new RequiredTextValidator(getView().getDriver(), "DriverIsRequired"));
        validators.add(new RequiredTextValidator(getView().getUrl(), "UrlIsRequired"));
        validators.add(new RequiredTextValidator(getView().getUsername(), "UsernameIsRequired"));
      }
      return validators;
    }

  }

  private class AlreadyExistMethodCallBack implements ResourceCallback<JdbcDataSourceDto> {

    @Override
    public void onResource(Response response, JdbcDataSourceDto resource) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("DatabaseAlreadyExists").build());
    }

  }

  private class CreateMethodCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      postDatabase(getJdbcDataSourceDto());
    }
  }

  private class CreateOrUpdateMethodCallBack implements ResponseCodeCallback {

    JdbcDataSourceDto dto;

    private CreateOrUpdateMethodCallBack(JdbcDataSourceDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(new DatabaseUpdatedEvent(dto));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new DatabaseCreatedEvent(dto));
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DatabaseUiHandlers> {

    void hideDialog();

    void setAvailableDrivers(JsArray<JdbcDriverDto> resource);

    void setDialogMode(Mode dialogMode);

    HasText getName();

    HasText getUrl();

    HasText getDriver();

    HasText getUsername();

    HasText getPassword();

    HasText getProperties();

  }

}
