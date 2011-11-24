/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

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
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class CodingViewDialogPresenter extends WidgetPresenter<CodingViewDialogPresenter.Display> {

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Mode dialogMode;

  public enum Mode {
    CREATE
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

    void setEnabledCodingViewName(boolean enabled);

    void clear();

  }

  @Inject
  public CodingViewDialogPresenter(Display display, EventBus eventBus) {
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
    validators.add(new RequiredTextValidator(getDisplay().getName(), "CodingViewNameIsRequired"));
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
    // super.registerHandler(getDisplay().getUpdateFunctionalUnitButton().addClickHandler(new
    // CreateOrUpdateFunctionalUnitClickHandler()));
    //
    // super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
    // public void onClick(ClickEvent event) {
    // getDisplay().hideDialog();
    // }
    // }));
    //
    // super.registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
    // @Override
    // public void onClose(CloseEvent<DialogBox> event) {
    // unbind();
    // }
    // }));

  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getDisplay().setDialogMode(dialogMode);
  }

  private void createCodingView() {
    if(validCodingView()) {
      CreateCodingViewCallBack createCodingViewCallback = new CreateCodingViewCallBack();
      AlreadyExistViewCallBack alreadyExistCodingViewCallback = new AlreadyExistViewCallBack();
      ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource("/views/" + getDisplay().getName().getText()).get().withCallback(alreadyExistCodingViewCallback).withCallback(Response.SC_NOT_FOUND, createCodingViewCallback).send();
    }
  }

  private boolean validCodingView() {
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
      }
    }

    if(messages.size() > 0) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(messages).build());
      return false;
    } else {
      return true;
    }
  }

  private ViewDto getViewDto() {
    ViewDto view = ViewDto.create();
    view.setName(getDisplay().getName().getText());
    // view.setKeyVariableName(getDisplay().getName().getText());
    // if(getDisplay().getSelect().getText().trim().length() > 0) {
    // view.setKeyVariableName(getDisplay().getSelect().getText());
    // }
    return view;
  }

  private class AlreadyExistViewCallBack implements ResourceCallback<ViewDto> {

    @Override
    public void onResource(Response response, ViewDto resource) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error("ViewAlreadyExistWithTheSpecifiedName").build());
    }

  }

  private class CreateCodingViewCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      ViewDto codingView = getViewDto();
      CreateOrUpdateCodingViewCallBack callbackHandler = new CreateOrUpdateCodingViewCallBack(codingView);
      ResourceRequestBuilderFactory.newBuilder().forResource("/views/").post().withResourceBody(ViewDto.stringify(codingView)).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  public class CreateOrUpdateFunctionalUnitClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.CREATE) {
        createCodingView();
      }
    }

  }

  private class CreateOrUpdateCodingViewCallBack implements ResponseCodeCallback {

    ViewDto view;

    public CreateOrUpdateCodingViewCallBack(ViewDto view) {
      this.view = view;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        eventBus.fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
      } else {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }

    }
  }

}
