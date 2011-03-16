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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

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
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RFunctionDataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RScriptDataShieldMethodDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class DataShieldMethodPresenter extends WidgetPresenter<DataShieldMethodPresenter.Display> {

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public enum MethodType {
    RSCRIPT, RFUNCTION
  }

  //
  // Instance Variables
  //

  private MethodValidationHandler methodValidationHandler;

  //
  // Constructors
  //

  @Inject
  public DataShieldMethodPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);
    getDisplay().clear();
    addEventHandlers();
    this.methodValidationHandler = new MethodValidationHandler(eventBus);
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  private void addEventHandlers() {
    registerHandler(getDisplay().getSaveButton().addClickHandler(new CreateOrUpdateMethodClickHandler()));

    registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    }));

  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getDisplay().setDialogMode(dialogMode);
  }

  public void updateMethod(DataShieldMethodDto dto) {
    setDialogMode(Mode.UPDATE);
    displayMethod(dto.getName(), dto);
  }

  public void copyMethod(DataShieldMethodDto dto) {
    setDialogMode(Mode.CREATE);
    displayMethod("copy_of_" + dto.getName(), dto);
  }

  private void displayMethod(String name, DataShieldMethodDto dto) {
    getDisplay().setName(name);
    MethodType type;
    if(dto.getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method) != null) {
      type = MethodType.RSCRIPT;
      getDisplay().setScript(((RScriptDataShieldMethodDto) dto.getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method)).getScript());
    } else {
      type = MethodType.RFUNCTION;
      getDisplay().setFunction(((RFunctionDataShieldMethodDto) dto.getExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method)).getFunc());
    }
    getDisplay().setType(type);
  }

  private void updateMethod() {
    if(methodValidationHandler.validate()) {
      putMethod(getDataShieldMethodDto());
    }
  }

  private void createMethod() {
    if(methodValidationHandler.validate()) {
      CreateMethodCallBack createCallback = new CreateMethodCallBack();
      AlreadyExistMethodCallBack alreadyExistCallback = new AlreadyExistMethodCallBack();
      ResourceRequestBuilderFactory.<DataShieldMethodDto> newBuilder().forResource("/datashield/method/" + getDisplay().getName().getText()).get()//
      .withCallback(alreadyExistCallback)//
      .withCallback(Response.SC_NOT_FOUND, createCallback).send();
    }
  }

  private void postMethod(DataShieldMethodDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource("/datashield/methods").post()//
    .withResourceBody(DataShieldMethodDto.stringify(dto))//
    .withCallback(Response.SC_OK, callbackHandler)//
    .withCallback(Response.SC_CREATED, callbackHandler)//
    .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private void putMethod(DataShieldMethodDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource("/datashield/method/" + getDisplay().getName().getText()).put()//
    .withResourceBody(DataShieldMethodDto.stringify(dto))//
    .withCallback(Response.SC_OK, callbackHandler)//
    .withCallback(Response.SC_CREATED, callbackHandler)//
    .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private DataShieldMethodDto getDataShieldMethodDto() {
    DataShieldMethodDto dto = DataShieldMethodDto.create();
    dto.setName(getDisplay().getName().getText());

    if(getDisplay().isScript().getValue()) {
      RScriptDataShieldMethodDto method = RScriptDataShieldMethodDto.create();
      method.setScript(getDisplay().getScript().getText());
      dto.setExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method, method);
    } else {
      RFunctionDataShieldMethodDto method = RFunctionDataShieldMethodDto.create();
      method.setFunc(getDisplay().getFunction().getText());
      dto.setExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method, method);
    }

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
        validators.add(new RequiredTextValidator(getDisplay().getName(), "DataShieldMethodNameIsRequired"));
        validators.add(new ConditionalValidator(getDisplay().isScript(), new RequiredTextValidator(getDisplay().getScript(), "DataShieldRScriptIsRequired")));
        validators.add(new ConditionalValidator(getDisplay().isFunction(), new RequiredTextValidator(getDisplay().getFunction(), "DataShieldRFunctionIsRequired")));
      }
      return validators;
    }

  }

  private class AlreadyExistMethodCallBack implements ResourceCallback<DataShieldMethodDto> {

    @Override
    public void onResource(Response response, DataShieldMethodDto resource) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error("DataShieldMethodAlreadyExistWithTheSpecifiedName").build());
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

    public CreateOrUpdateMethodCallBack(DataShieldMethodDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        eventBus.fireEvent(new DataShieldMethodUpdatedEvent(dto));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new DataShieldMethodCreatedEvent(dto));
      } else {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HasCloseHandlers<DialogBox> getDialog();

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
