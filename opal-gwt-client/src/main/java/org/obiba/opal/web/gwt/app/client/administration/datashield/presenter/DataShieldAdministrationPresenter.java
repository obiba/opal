package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.EDIT_ACTION;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DataShieldAdministrationPresenter extends WidgetPresenter<DataShieldAdministrationPresenter.Display> {

  private String env;

  private Runnable removeMethodConfirmation;

  private DataShieldMethodPresenter dataShieldMethodPresenter;

  @Inject
  public DataShieldAdministrationPresenter(final Display display, final EventBus eventBus, DataShieldMethodPresenter dataShieldMethodPresenter) {
    super(display, eventBus);
    this.dataShieldMethodPresenter = dataShieldMethodPresenter;
  }

  void setEnvironment(String env) {
    this.env = env;
    this.dataShieldMethodPresenter.setEnvironement(env);
  }

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
        dataShieldMethodPresenter.bind();
        dataShieldMethodPresenter.revealDisplay();
      }
    }));
    registerHandler(eventBus.addHandler(DataShieldMethodCreatedEvent.getType(), new DataShieldMethodCreatedEvent.Handler() {

      @Override
      public void onDataShieldMethodCreated(DataShieldMethodCreatedEvent event) {
        updateDataShieldMethods();
      }
    }));
    registerHandler(eventBus.addHandler(DataShieldMethodUpdatedEvent.getType(), new DataShieldMethodUpdatedEvent.Handler() {

      @Override
      public void onDataShieldMethodUpdated(DataShieldMethodUpdatedEvent event) {
        updateDataShieldMethods();
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
    authorize();
  }

  @Override
  public void revealDisplay() {
    authorize();
  }

  // @Override
  public void authorize(HasAuthorization authorizer) {
    authorizeMethods(CascadingAuthorizer.newBuilder()//
    .or(AclRequest.newResourceAuthorizationRequestBuilder())//
    .authorize(authorizer).build());
  }

  private void authorize() {
    // view methods
    authorizeMethods(new CompositeAuthorizer(getDisplay().getMethodsAuthorizer(), new MethodsUpdate()));
    // create method
    authorizeAddMethod(getDisplay().getAddMethodAuthorizer());
  }

  private String environment() {
    return "/datashield/env/" + this.env;
  }

  private String methods() {
    return environment() + "/methods";
  }

  private String method(String method) {
    return environment() + "/method/" + method;
  }

  private void authorizeMethods(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(methods()).get().authorize(authorizer).send();
  }

  private void authorizeAddMethod(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(methods()).post().authorize(authorizer).send();
  }

  private void authorizeEditMethod(DataShieldMethodDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(method(dto.getName())).put().authorize(authorizer).send();
  }

  private void authorizeDeleteMethod(DataShieldMethodDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(method(dto.getName())).delete().authorize(authorizer).send();
  }

  private void updateDataShieldMethods() {
    ResourceRequestBuilderFactory.<JsArray<DataShieldMethodDto>> newBuilder().forResource(methods()).get()//
    .withCallback(new ResourceCallback<JsArray<DataShieldMethodDto>>() {

      @Override
      public void onResource(Response response, JsArray<DataShieldMethodDto> resource) {
        getDisplay().renderDataShieldMethodsRows(JsArrays.toSafeArray(resource));
      }
    }).send();
  }

  protected void doDataShieldMethodActionImpl(final DataShieldMethodDto dto, String actionName) {
    if(actionName.equals(EDIT_ACTION)) {
      authorizeEditMethod(dto, new Authorizer(eventBus) {

        @Override
        public void authorized() {
          dataShieldMethodPresenter.bind();
          dataShieldMethodPresenter.updateMethod(dto);
          dataShieldMethodPresenter.revealDisplay();
        }
      });

    } else if(actionName.equals(DELETE_ACTION)) {
      authorizeDeleteMethod(dto, new Authorizer(eventBus) {
        @Override
        public void authorized() {
          removeMethodConfirmation = new Runnable() {
            public void run() {
              deleteDataShieldMethod(dto);
            }
          };
          eventBus.fireEvent(new ConfirmationRequiredEvent(removeMethodConfirmation, "deleteDataShieldMethod", "confirmDeleteDataShieldMethod"));
        }
      });

    }
  }

  private void deleteDataShieldMethod(final DataShieldMethodDto dto) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NOT_FOUND) {
          updateDataShieldMethods();
        } else {
          eventBus.fireEvent(NotificationEvent.newBuilder().error("Failed removing aggregating method.").build());
        }
      }

    };

    ResourceRequestBuilderFactory.newBuilder().forResource(method(dto.getName())).delete() //
    .withCallback(Response.SC_OK, callbackHandler) //
    .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
    .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  //
  // Inner Classes / Interfaces
  //

  /**
   *
   */
  private final class MethodsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      updateDataShieldMethods();
    }
  }

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

    HasAuthorization getAddMethodAuthorizer();

    HasAuthorization getMethodsAuthorizer();

  }

}
