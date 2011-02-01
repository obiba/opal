package org.obiba.opal.web.gwt.app.client.administration.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DataShieldAdministrationPresenter extends WidgetPresenter<DataShieldAdministrationPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String EDIT_ACTION = "Edit";

  //
  // Instance Variables
  //

  private Runnable removeMethodConfirmation;

  //
  // Constructors
  //

  @Inject
  public DataShieldAdministrationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  private void addEventHandlers() {
    getDisplay().getDataShieldMethodActionsColumn().setActionHandler(new ActionHandler<DataShieldMethodDto>() {
      public void doAction(DataShieldMethodDto dto, String actionName) {
        if(actionName != null) {
          doDataShieldMethodActionImpl(dto, actionName);
        }
      }
    });
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
    updateDataShield();
  }

  //
  //
  //

  private void updateDataShield() {
    ResourceRequestBuilderFactory.<JsArray<DataShieldMethodDto>> newBuilder().forResource("/datashield/methods").get()//
    .withCallback(new ResourceCallback<JsArray<DataShieldMethodDto>>() {

      @Override
      public void onResource(Response response, JsArray<DataShieldMethodDto> resource) {
        getDisplay().renderDataShieldMethodsRows(JsArrays.toSafeArray(resource));
      }
    }).send();
  }

  protected void doDataShieldMethodActionImpl(final DataShieldMethodDto dto, String actionName) {
    if(actionName.equals(EDIT_ACTION)) {

    } else if(actionName.equals(DELETE_ACTION)) {
      removeMethodConfirmation = new Runnable() {
        public void run() {
          deleteDataShieldMethod(dto);
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(removeMethodConfirmation, "deleteDataShieldMethod", "confirmDeleteDataShieldMethod"));
    }
  }

  private void deleteDataShieldMethod(final DataShieldMethodDto dto) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NOT_FOUND) {
          updateDataShield();
        } else {
          ClientErrorDto error = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, error.getStatus(), null));
        }
      }

    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/datashield/method/" + dto.getName()).delete() //
    .withCallback(Response.SC_OK, callbackHandler) //
    .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
    .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    //
    // DataShield
    //

    void renderDataShieldMethodsRows(JsArray<DataShieldMethodDto> rows);

    HasActionHandler<DataShieldMethodDto> getDataShieldMethodActionsColumn();

  }

}
