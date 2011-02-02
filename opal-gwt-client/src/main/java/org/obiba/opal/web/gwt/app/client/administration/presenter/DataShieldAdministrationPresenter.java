package org.obiba.opal.web.gwt.app.client.administration.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 * DataShield related administration.
 */
public class DataShieldAdministrationPresenter extends ItemAdministrationPresenter<DataShieldAdministrationPresenter.Display> {

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

  @Override
  public String getName() {
    return "DataShield";
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
    registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    registerHandler(getDisplay().addMethodHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        // TODO Auto-generated method stub

      }
    }));
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
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "Failed removing aggregating method.", null));
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

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeMethodConfirmation != null && event.getSource().equals(removeMethodConfirmation) && event.isConfirmed()) {
        removeMethodConfirmation.run();
        removeMethodConfirmation = null;
      }
    }

  }

  public interface Display extends WidgetDisplay {

    void renderDataShieldMethodsRows(JsArray<DataShieldMethodDto> rows);

    HasActionHandler<DataShieldMethodDto> getDataShieldMethodActionsColumn();

    HandlerRegistration addMethodHandler(ClickHandler handler);

  }

}
