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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

public class FunctionalUnitUpdateDialogPresenter extends PresenterWidget<FunctionalUnitUpdateDialogPresenter.Display> {

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public interface Display extends PopupView {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getUpdateFunctionalUnitButton();

    HasClickHandlers getCancelButton();

    HasCloseHandlers<DialogBox> getDialog();

    void setName(String name);

    void setSelect(String select);

    HasText getName();

    HasText getSelect();

    void setEnabledFunctionalUnitName(boolean enabled);

    void clear();

  }

  @Inject
  public FunctionalUnitUpdateDialogPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
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

  private void addEventHandlers() {
    super.registerHandler(
        getView().getUpdateFunctionalUnitButton().addClickHandler(new CreateOrUpdateFunctionalUnitClickHandler()));
    super.registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  private void updateFunctionalUnit() {
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

  private void createFunctionalUnit() {
    if(validFunctionalUnit()) {
      CreateFunctionalUnitCallBack createFunctionalUnitCallback = new CreateFunctionalUnitCallBack();
      AlreadyExistFunctionalUnitCallBack alreadyExistFunctionalUnitCallback = new AlreadyExistFunctionalUnitCallBack();
      UriBuilder ub = UriBuilder.create().segment("functional-unit", getView().getName().getText());
      ResourceRequestBuilderFactory.<FunctionalUnitDto>newBuilder().forResource(ub.build()).get().withCallback
          (alreadyExistFunctionalUnitCallback).withCallback(Response.SC_NOT_FOUND, createFunctionalUnitCallback).send();
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
    if(getView().getSelect().getText().trim().length() > 0) {
      functionalUnit.setKeyVariableName(getView().getSelect().getText());
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
      CreateOrUpdateFunctionalUnitCallBack callbackHandler = new CreateOrUpdateFunctionalUnitCallBack(functionalUnit);
      ResourceRequestBuilderFactory.newBuilder().forResource("/functional-units/").post()
          .withResourceBody(FunctionalUnitDto.stringify(functionalUnit))
          .withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler)
          .send();
    }
  }

  public class CreateOrUpdateFunctionalUnitClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.CREATE) {
        createFunctionalUnit();
      } else if(dialogMode == Mode.UPDATE) {
        updateFunctionalUnit();
      }
    }

  }

  private class CreateOrUpdateFunctionalUnitCallBack implements ResponseCodeCallback {

    FunctionalUnitDto functionalUnit;

    public CreateOrUpdateFunctionalUnitCallBack(FunctionalUnitDto FunctionalUnit) {
      this.functionalUnit = FunctionalUnit;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(new FunctionalUnitUpdatedEvent(functionalUnit));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new FunctionalUnitCreatedEvent(functionalUnit));
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }

    }
  }

}
