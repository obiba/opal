/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class DatabasePresenter extends PresenterWidget<DatabasePresenter.Display> {

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  private MethodValidationHandler methodValidationHandler;

  @Inject
  public DatabasePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);

    registerHandler(getView().getSaveButton().addClickHandler(new CreateOrUpdateMethodClickHandler()));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

    this.methodValidationHandler = new MethodValidationHandler(getEventBus());
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
    getView().clear();
  }

  /**
   * Setup the dialog for updating an existing method
   * @param dto method to update
   */
  public void updateDatabase(JdbcDataSourceDto dto) {
    setDialogMode(Mode.UPDATE);
    displayDatabase(dto.getName(), dto);
  }

  /**
   * Setup the dialog for copying an existing method
   * @param dto method to copy
   */
  public void copyDatabase(JdbcDataSourceDto dto) {
    setDialogMode(Mode.CREATE);
    displayDatabase("copy_of_" + dto.getName(), dto);
  }

  private void displayDatabase(String name, JdbcDataSourceDto dto) {
    getView().getName().setText(name);
    getView().getDriver().setText(dto.getDriverClass());
    getView().getUrl().setText(dto.getUrl());
    getView().getUsername().setText(dto.getUsername());
    getView().getPassword().setText(dto.getPassword());
  }

  private void updateDatabase() {
    if(methodValidationHandler.validate()) {
      putDatabase(getJdbcDataSourceDto());
    }
  }

  private void createDatabase() {
    if(methodValidationHandler.validate()) {
      CreateMethodCallBack createCallback = new CreateMethodCallBack();
      AlreadyExistMethodCallBack alreadyExistCallback = new AlreadyExistMethodCallBack();
      ResourceRequestBuilderFactory.<JdbcDataSourceDto> newBuilder().forResource("/jdbc/database/" + (getView().getName().getText())).get()//
      .withCallback(alreadyExistCallback)//
      .withCallback(Response.SC_NOT_FOUND, createCallback).send();
    }
  }

  private void postDatabase(JdbcDataSourceDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource("/jdbc/databases").post()//
    .withResourceBody(JdbcDataSourceDto.stringify(dto))//
    .withCallback(Response.SC_OK, callbackHandler)//
    .withCallback(Response.SC_CREATED, callbackHandler)//
    .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private void putDatabase(JdbcDataSourceDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource("/jdbc/database/" + (getView().getName().getText())).put()//
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
    return dto;
  }

  //
  // Inner classes and interfaces
  //

  private class MethodValidationHandler extends AbstractValidationHandler {

    public MethodValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "DataShieldMethodNameIsRequired"));
        validators.add(new RequiredTextValidator(getView().getUrl(), "DataShieldMethodNameIsRequired"));
        validators.add(new RequiredTextValidator(getView().getDriver(), "DataShieldMethodNameIsRequired"));
        validators.add(new RequiredTextValidator(getView().getUsername(), "DataShieldMethodNameIsRequired"));
        validators.add(new RequiredTextValidator(getView().getPassword(), "DataShieldMethodNameIsRequired"));
      }
      return validators;
    }

  }

  private class AlreadyExistMethodCallBack implements ResourceCallback<JdbcDataSourceDto> {

    @Override
    public void onResource(Response response, JdbcDataSourceDto resource) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("DataShieldMethodAlreadyExistWithTheSpecifiedName").build());
    }

  }

  private class CreateMethodCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      postDatabase(getJdbcDataSourceDto());
    }
  }

  public class CreateOrUpdateMethodClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.CREATE) {
        createDatabase();
      } else if(dialogMode == Mode.UPDATE) {
        updateDatabase();
      }
    }

  }

  private class CreateOrUpdateMethodCallBack implements ResponseCodeCallback {

    JdbcDataSourceDto dto;

    public CreateOrUpdateMethodCallBack(JdbcDataSourceDto dto) {
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

  public interface Display extends PopupView {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HasText getName();

    HasText getUrl();

    HasText getDriver();

    HasText getUsername();

    HasText getPassword();

    void clear();

  }

}
