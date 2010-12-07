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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

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
import com.google.inject.Inject;

public class FunctionalUnitUpdateDialogPresenter extends WidgetPresenter<FunctionalUnitUpdateDialogPresenter.Display> {

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public interface Display extends WidgetDisplay {
    void showDialog();

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
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    addValidators();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getDisplay().getName(), "FunctionalUnitNameIsRequired"));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    validators.clear();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getUpdateFunctionalUnitButton().addClickHandler(new CreateOrUpdateFunctionalUnitClickHandler()));

    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    super.registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    }));

  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getDisplay().setDialogMode(dialogMode);
  }

  private void updateFunctionalUnit() {
    if(validFunctionalUnit()) {
      FunctionalUnitDto FunctionalUnit = getFunctionalUnitDto();
      CreateOrUpdateFunctionalUnitCallBack callbackHandler = new CreateOrUpdateFunctionalUnitCallBack(FunctionalUnit);
      ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + getDisplay().getName().getText()).put().withResourceBody(FunctionalUnitDto.stringify(FunctionalUnit)).withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private void createFunctionalUnit() {
    if(validFunctionalUnit()) {
      CreateFunctionalUnitCallBack createFunctionalUnitCallback = new CreateFunctionalUnitCallBack();
      AlreadyExistFunctionalUnitCallBack alreadyExistFunctionalUnitCallback = new AlreadyExistFunctionalUnitCallBack();
      ResourceRequestBuilderFactory.<FunctionalUnitDto> newBuilder().forResource("/functional-unit/" + getDisplay().getName().getText()).get().withCallback(alreadyExistFunctionalUnitCallback).withCallback(Response.SC_NOT_FOUND, createFunctionalUnitCallback).send();
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
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, messages, null));
      return false;
    } else {
      return true;
    }
  }

  private void createUnitFolder(final String unit) {
    final String unitFolderPath = "/units/" + unit;

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 201) {
          eventBus.fireEvent(new FolderCreationEvent(unitFolderPath));
        } else {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/files" + unitFolderPath).put().withCallback(201, callbackHandler).withCallback(403, callbackHandler).withCallback(500, callbackHandler).send();
  }

  private FunctionalUnitDto getFunctionalUnitDto() {
    FunctionalUnitDto functionalUnit = FunctionalUnitDto.create();
    functionalUnit.setName(getDisplay().getName().getText());
    functionalUnit.setKeyVariableName(getDisplay().getName().getText());
    if(getDisplay().getSelect().getText().trim().length() > 0) {
      functionalUnit.setKeyVariableName(getDisplay().getSelect().getText());
    }
    return functionalUnit;
  }

  private class AlreadyExistFunctionalUnitCallBack implements ResourceCallback<FunctionalUnitDto> {

    @Override
    public void onResource(Response response, FunctionalUnitDto resource) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "FunctionalUnitAlreadyExistWithTheSpecifiedName", null));
    }

  }

  private class CreateFunctionalUnitCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      FunctionalUnitDto functionalUnit = getFunctionalUnitDto();
      CreateOrUpdateFunctionalUnitCallBack callbackHandler = new CreateOrUpdateFunctionalUnitCallBack(functionalUnit);
      ResourceRequestBuilderFactory.newBuilder().forResource("/functional-units/").post().withResourceBody(FunctionalUnitDto.stringify(functionalUnit)).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
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
      getDisplay().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        eventBus.fireEvent(new FunctionalUnitUpdatedEvent(functionalUnit));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new FunctionalUnitCreatedEvent(functionalUnit));
        createUnitFolder(functionalUnit.getName());
      } else {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
      }

    }
  }

}
