/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class FunctionalUnitUpdateModalPresenter extends ModalPresenterWidget<FunctionalUnitUpdateModalPresenter.Display>
    implements FunctionalUnitUpdateModalUiHandlers {

  @SuppressWarnings("TypeMayBeWeakened")
  private final Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  @SuppressWarnings("unused")
  public interface Display extends PopupView, HasUiHandlers<FunctionalUnitUpdateModalUiHandlers> {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    void setName(String name);

    void setDescription(String description);

    void setSelect(String select);

    HasText getName();

    HasText getDescription();

    HasText getSelect();

    void setEnabledFunctionalUnitName(boolean enabled);

    void clear();

  }

  @Inject
  public FunctionalUnitUpdateModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addValidators();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getView().getName(), "FunctionalUnitNameIsRequired"));
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    validators.clear();
  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  @Override
  public void updateFunctionaUnit() {
    if(dialogMode == Mode.CREATE) {
      createFunctionalUnit();
    } else if(dialogMode == Mode.UPDATE) {
      updateFunctionalUnitInternal();
    }
  }

  @Override
  public void onDialogHide() {
    getView().hideDialog();
    onModalHidden();
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass, TypeMayBeWeakened")
  private void updateFunctionalUnitInternal() {
    if(validFunctionalUnit()) {
      FunctionalUnitDto functionalUnit = getFunctionalUnitDto();
      CreateOrUpdateFunctionalUnitCallBack callbackHandler = new CreateOrUpdateFunctionalUnitCallBack(functionalUnit);
      UriBuilder ub = UriBuilder.create().segment("functional-unit", getView().getName().getText());
      ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).put()
          .withResourceBody(FunctionalUnitDto.stringify(functionalUnit)).withCallback(Response.SC_OK, callbackHandler)
          .withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler)
          .send();
    }
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass, TypeMayBeWeakened")
  private void createFunctionalUnit() {
    if(validFunctionalUnit()) {
      CreateFunctionalUnitCallBack createFunctionalUnitCallback = new CreateFunctionalUnitCallBack();
      AlreadyExistFunctionalUnitCallBack alreadyExistFunctionalUnitCallback = new AlreadyExistFunctionalUnitCallBack();
      UriBuilder ub = UriBuilder.create().segment("functional-unit", getView().getName().getText());
      ResourceRequestBuilderFactory.<FunctionalUnitDto>newBuilder().forResource(ub.build()).get()
          .withCallback(alreadyExistFunctionalUnitCallback)
          .withCallback(Response.SC_NOT_FOUND, createFunctionalUnitCallback).send();
    }
  }

  private boolean validFunctionalUnit() {
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
      }
    }

    if(messages.size() > 0) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(messages).build());
      return false;
    } else {
      return true;
    }
  }

  private FunctionalUnitDto getFunctionalUnitDto() {
    FunctionalUnitDto functionalUnit = FunctionalUnitDto.create();
    functionalUnit.setName(getView().getName().getText());
    functionalUnit.setKeyVariableName(getView().getName().getText());
    if(getView().getDescription().getText().trim().length() > 0) {
      functionalUnit.setDescription(getView().getDescription().getText());
    }
    if(getView().getSelect().getText().trim().length() > 0) {
      functionalUnit.setSelect(getView().getSelect().getText());
    }
    return functionalUnit;
  }

  private class AlreadyExistFunctionalUnitCallBack implements ResourceCallback<FunctionalUnitDto> {

    @Override
    public void onResource(Response response, FunctionalUnitDto resource) {
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("FunctionalUnitAlreadyExistWithTheSpecifiedName").build());
    }

  }

  private class CreateFunctionalUnitCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      FunctionalUnitDto functionalUnit = getFunctionalUnitDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateFunctionalUnitCallBack(functionalUnit);
      ResourceRequestBuilderFactory.newBuilder().forResource("/functional-units/").post()
          .withResourceBody(FunctionalUnitDto.stringify(functionalUnit))
          .withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler)
          .send();
    }
  }

  private class CreateOrUpdateFunctionalUnitCallBack implements ResponseCodeCallback {

    FunctionalUnitDto functionalUnit;

    private CreateOrUpdateFunctionalUnitCallBack(FunctionalUnitDto FunctionalUnit) {
      functionalUnit = FunctionalUnit;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(new FunctionalUnitUpdatedEvent(functionalUnit));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new FunctionalUnitCreatedEvent(functionalUnit));
      } else {
        NotificationEvent.Builder builder = NotificationEvent.newBuilder();
        try {
          ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
          GWT.log(errorDto.getStatus());
          GWT.log(errorDto.getArgumentsArray().join());
          builder.error(errorDto.getStatus()).args(errorDto.getArgumentsArray());
        } catch(Exception e) {
          builder.error(response.getText());
        }
        getEventBus().fireEvent(builder.build());
      }

    }
  }

}
