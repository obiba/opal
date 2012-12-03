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

import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ScheduleDto;
import org.obiba.opal.web.model.client.opal.ScheduleType;
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
    UPDATE
  }

  private MethodValidationHandler methodValidationHandler;

  @Inject
  public IndexPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.UPDATE);

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

//  /**
//   * Setup the dialog for creating a method
//   */
//  public void createNewDatabase() {
//    setDialogMode(Mode.CREATE);
//  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
  public void updateSchedule(TableIndexStatusDto dto) {
    setDialogMode(Mode.UPDATE);
    displaySchedule(dto.getSchedule());
  }

  private void displaySchedule(ScheduleDto dto) {
    getView().getType().setText(dto.getType().getName());
//    getView().getDriver().setText(dto.getDriverClass());
//    getView().getUrl().setText(dto.getUrl());
//    getView().getUsername().setText(dto.getUsername());
//    getView().getPassword().setText(dto.getPassword());
//    getView().getProperties().setText(dto.getProperties());
  }

  private void updateDatabase() {
    if(methodValidationHandler.validate()) {
      putDatabase(getTableIndexStatusDto());
    }
  }

  private void postDatabase(TableIndexStatusDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
//    ResourceRequestBuilderFactory.newBuilder().forResource(Resources.databases()).post()//
//        .withResourceBody(JdbcDataSourceDto.stringify(dto))//
//        .withCallback(Response.SC_OK, callbackHandler)//
//        .withCallback(Response.SC_CREATED, callbackHandler)//
//        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private void putDatabase(TableIndexStatusDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
//    ResourceRequestBuilderFactory.newBuilder().forResource(Resources.database(getView().getName().getText())).put()//
//        .withResourceBody(JdbcDataSourceDto.stringify(dto))//
//        .withCallback(Response.SC_OK, callbackHandler)//
//        .withCallback(Response.SC_CREATED, callbackHandler)//
//        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private TableIndexStatusDto getTableIndexStatusDto() {
    TableIndexStatusDto dto = TableIndexStatusDto.create();
    //dto.setTable(getView().getTable().getText());
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
//        validators = new LinkedHashSet<FieldValidator>();
//        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired"));
//        validators.add(new RequiredTextValidator(getView().getDriver(), "DriverIsRequired"));
//        validators.add(new RequiredTextValidator(getView().getUrl(), "UrlIsRequired"));
//        validators.add(new RequiredTextValidator(getView().getUsername(), "UsernameIsRequired"));
      }
      return validators;
    }

  }

  private class AlreadyExistMethodCallBack implements ResourceCallback<TableIndexStatusDto> {

    @Override
    public void onResource(Response response, TableIndexStatusDto resource) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("DatabaseAlreadyExists").build());
    }

  }

  public class CreateOrUpdateMethodClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.UPDATE) {
        updateDatabase();
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

    void setAvailableType(JsArray<ScheduleType> resource);

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HasText getType();

  }

}
