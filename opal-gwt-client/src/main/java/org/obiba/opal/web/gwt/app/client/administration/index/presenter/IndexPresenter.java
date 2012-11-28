/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

import com.google.gwt.core.client.JsArray;
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

public class IndexPresenter extends PresenterWidget<IndexPresenter.Display> {

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  private MethodValidationHandler methodValidationHandler;

  @Inject
  public IndexPresenter(Display display, EventBus eventBus) {
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

//    ResourceRequestBuilderFactory.<JsArray<JdbcDriverDto>> newBuilder().forResource(
//        org.obiba.opal.web.gwt.app.client.administration.database.presenter.Resources.drivers()).withCallback(new ResourceCallback<JsArray<JdbcDriverDto>>() {
//
//      @Override
//      public void onResource(Response response, JsArray<JdbcDriverDto> resource) {
//        getView().setAvailableDrivers(resource);
//      }
//    }).get().send();

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
  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
  public void updateDatabase(TableIndexStatusDto dto) {
    setDialogMode(Mode.UPDATE);
    displayIndex(dto.getDatasource(), dto);
  }

  private void displayIndex(String name, TableIndexStatusDto dto) {
    getView().getName().setText(name);
//    getView().getDriver().setText(dto.getDriverClass());
//    getView().getUrl().setText(dto.getUrl());
//    getView().getUsername().setText(dto.getUsername());
//    getView().getPassword().setText(dto.getPassword());
//    getView().getProperties().setText(dto.getProperties());
  }

  private void updateIndex() {
    if(methodValidationHandler.validate()) {
      putIndex(getTableIndexationStatusDto());
    }
  }

//  private void createIndex() {
//    if(methodValidationHandler.validate()) {
//      CreateMethodCallBack createCallback = new CreateMethodCallBack();
//      AlreadyExistMethodCallBack alreadyExistCallback = new AlreadyExistMethodCallBack();
//      ResourceRequestBuilderFactory.<JdbcDataSourceDto> newBuilder().forResource(
//          org.obiba.opal.web.gwt.app.client.administration.database.presenter.Resources
//              .database(getView().getName().getText())).get()//
//      .withCallback(alreadyExistCallback)//
//      .withCallback(Response.SC_NOT_FOUND, createCallback).send();
//    }
//  }
//
//  private void postDatabase(JdbcDataSourceDto dto) {
//    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
//    ResourceRequestBuilderFactory.newBuilder().forResource(
//        org.obiba.opal.web.gwt.app.client.administration.database.presenter.Resources.databases()).post()//
//    .withResourceBody(JdbcDataSourceDto.stringify(dto))//
//    .withCallback(Response.SC_OK, callbackHandler)//
//    .withCallback(Response.SC_CREATED, callbackHandler)//
//    .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
//  }

  private void putIndex(TableIndexStatusDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource(
        org.obiba.opal.web.gwt.app.client.administration.index.presenter.Resources
            .index(getView().getName().getText(), getView().getName().getText())).put()//
        .withResourceBody(TableIndexStatusDto.stringify(dto))//
        .withCallback(Response.SC_OK, callbackHandler)//
        .withCallback(Response.SC_CREATED, callbackHandler)//
        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private TableIndexStatusDto getTableIndexationStatusDto() {
    TableIndexStatusDto dto = TableIndexStatusDto.create();
    dto.setDatasource(getView().getName().getText());
//    dto.setUrl(getView().getUrl().getText());
//    dto.setDriverClass(getView().getDriver().getText());
//    dto.setUsername(getView().getUsername().getText());
//    dto.setPassword(getView().getPassword().getText());
//    dto.setProperties(getView().getProperties().getText());
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

//  private class CreateMethodCallBack implements ResponseCodeCallback {
//
//    @Override
//    public void onResponseCode(Request request, Response response) {
//      postDatabase(getTableIndexationStatusDto());
//    }
//  }

  public class CreateOrUpdateMethodClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.CREATE) {
//        createIndex();
      } else if(dialogMode == Mode.UPDATE) {
        updateIndex();
      }
    }

  }

  private class CreateOrUpdateMethodCallBack implements ResponseCodeCallback {

    TableIndexStatusDto dto;

    public CreateOrUpdateMethodCallBack(TableIndexStatusDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
//      if(response.getStatusCode() == Response.SC_OK) {
//        getEventBus().fireEvent(new DatabaseUpdatedEvent(dto));
//      } else if(response.getStatusCode() == Response.SC_CREATED) {
//        getEventBus().fireEvent(new DatabaseCreatedEvent(dto));
//      } else {
//        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
//      }
    }
  }

  public interface Display extends PopupView {

    void hideDialog();

    void setAvailableDrivers(JsArray<JdbcDriverDto> resource);

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HasText getName();

    HasText getUrl();

    HasText getDriver();

    HasText getUsername();

    HasText getPassword();

    HasText getProperties();

  }

}
