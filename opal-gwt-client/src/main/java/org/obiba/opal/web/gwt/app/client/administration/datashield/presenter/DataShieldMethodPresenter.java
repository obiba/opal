/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RFunctionDataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RScriptDataShieldMethodDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class DataShieldMethodPresenter extends PresenterWidget<DataShieldMethodPresenter.Display> {

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public enum MethodType {
    RSCRIPT, RFUNCTION
  }

  private String environement;

  private MethodValidationHandler methodValidationHandler;

  @Inject
  public DataShieldMethodPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public void setEnvironement(String environement) {
    this.environement = environement;
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);

    registerHandler(getView().getSaveButton().addClickHandler(new CreateOrUpdateMethodClickHandler()));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

    methodValidationHandler = new MethodValidationHandler(getEventBus());
  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  /**
   * Setup the dialog for creating a method
   */
  public void createNewMethod() {
    setDialogMode(Mode.CREATE);
    getView().clear();
  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
  public void updateMethod(DataShieldMethodDto dto) {
    setDialogMode(Mode.UPDATE);
    displayMethod(dto.getName(), dto);
  }

  /**
   * Setup the dialog for copying an existing method
   *
   * @param dto method to copy
   */
  public void copyMethod(DataShieldMethodDto dto) {
    setDialogMode(Mode.CREATE);
    displayMethod("copy_of_" + dto.getName(), dto);
  }

  private void displayMethod(String name, DataShieldMethodDto dto) {
    getView().setName(name);
    MethodType type;
    if(dto.getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method) != null) {
      type = MethodType.RSCRIPT;
      getView().setScript(((RScriptDataShieldMethodDto) dto
          .getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method)).getScript());
    } else {
      type = MethodType.RFUNCTION;
      getView().setFunction(((RFunctionDataShieldMethodDto) dto
          .getExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method)).getFunc());
    }
    getView().setType(type);
  }

  private String method(String method) {
    return UriBuilder.create().segment("datashield", "env", environement, "method", "{method}").build(method);
  }

  private String methods() {
    return UriBuilder.create().segment("datashield", "env", environement, "methods").build();
  }

  private void updateMethod() {
    if(methodValidationHandler.validate()) {
      putMethod(getDataShieldMethodDto());
    }
  }

  private void createMethod() {
    if(methodValidationHandler.validate()) {
      ResourceRequestBuilderFactory.<DataShieldMethodDto>newBuilder().forResource(method(getView().getName().getText()))
          .get()//
          .withCallback(new AlreadyExistMethodCallBack())//
          .withCallback(Response.SC_NOT_FOUND, new CreateMethodCallBack()).send();
    }
  }

  private void postMethod(DataShieldMethodDto dto) {
    ResponseCodeCallback callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource(methods()).post()//
        .withResourceBody(DataShieldMethodDto.stringify(dto))//
        .withCallback(Response.SC_OK, callbackHandler)//
        .withCallback(Response.SC_CREATED, callbackHandler)//
        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private void putMethod(DataShieldMethodDto dto) {
    ResponseCodeCallback callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource(method(getView().getName().getText())).put()//
        .withResourceBody(DataShieldMethodDto.stringify(dto))//
        .withCallback(Response.SC_OK, callbackHandler)//
        .withCallback(Response.SC_CREATED, callbackHandler)//
        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private DataShieldMethodDto getDataShieldMethodDto() {
    DataShieldMethodDto dto = DataShieldMethodDto.create();
    dto.setName(getView().getName().getText());

    if(getView().isScript().getValue()) {
      RScriptDataShieldMethodDto method = RScriptDataShieldMethodDto.create();
      method.setScript(getView().getScript().getText());
      dto.setExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method, method);
    } else {
      RFunctionDataShieldMethodDto method = RFunctionDataShieldMethodDto.create();
      method.setFunc(getView().getFunction().getText());
      dto.setExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method, method);
    }

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
        validators.add(new RequiredTextValidator(getView().getName(), "DataShieldMethodNameIsRequired"));
        validators.add(new ConditionalValidator(getView().isScript(),
            new RequiredTextValidator(getView().getScript(), "DataShieldRScriptIsRequired")));
        validators.add(new ConditionalValidator(getView().isFunction(),
            new RequiredTextValidator(getView().getFunction(), "DataShieldRFunctionIsRequired")));
      }
      return validators;
    }

  }

  private class AlreadyExistMethodCallBack implements ResourceCallback<DataShieldMethodDto> {

    @Override
    public void onResource(Response response, DataShieldMethodDto resource) {
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("DataShieldMethodAlreadyExistWithTheSpecifiedName").build());
    }

  }

  private class CreateMethodCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      postMethod(getDataShieldMethodDto());
    }
  }

  public class CreateOrUpdateMethodClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.CREATE) {
        createMethod();
      } else if(dialogMode == Mode.UPDATE) {
        updateMethod();
      }
    }

  }

  private class CreateOrUpdateMethodCallBack implements ResponseCodeCallback {

    DataShieldMethodDto dto;

    private CreateOrUpdateMethodCallBack(DataShieldMethodDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(new DataShieldMethodUpdatedEvent(dto));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new DataShieldMethodCreatedEvent(dto));
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

    void setName(String name);

    void setType(MethodType type);

    void setScript(String script);

    void setFunction(String func);

    HasText getName();

    HasValue<Boolean> isFunction();

    HasValue<Boolean> isScript();

    HasText getScript();

    HasText getFunction();

    void clear();

  }

}
