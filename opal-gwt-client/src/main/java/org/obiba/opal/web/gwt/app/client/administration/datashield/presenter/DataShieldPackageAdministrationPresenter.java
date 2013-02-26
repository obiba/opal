package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageCreatedEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsPackageRColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class DataShieldPackageAdministrationPresenter
    extends PresenterWidget<DataShieldPackageAdministrationPresenter.Display> {

  private Runnable removePackageConfirmation;

  private DataShieldPackageCreatePresenter dataShieldPackageCreatePresenter;

  private DataShieldPackagePresenter dataShieldPackagePresenter;

  @Inject
  public DataShieldPackageAdministrationPresenter(final Display display, final EventBus eventBus,
      DataShieldPackageCreatePresenter dataShieldPackagePresenter,
      DataShieldPackagePresenter dataShieldPackagePresenter1) {
    super(eventBus, display);
    dataShieldPackageCreatePresenter = dataShieldPackagePresenter;
    this.dataShieldPackagePresenter = dataShieldPackagePresenter1;
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
  }

  private void addEventHandlers() {

    getView().getDataShieldPackageActionsColumn().setActionHandler(new ActionHandler<RPackageDto>() {
      @Override
      public void doAction(RPackageDto dto, String actionName) {
        if(actionName != null) {
          doDataShieldPackageActionImpl(dto, actionName);
        }
      }
    });
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    registerHandler(getView().addPackageHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dataShieldPackageCreatePresenter.addNewPackage();
        addToPopupSlot(dataShieldPackageCreatePresenter);
      }
    }));
    registerHandler(
        getEventBus().addHandler(DataShieldMethodCreatedEvent.getType(), new DataShieldMethodCreatedEvent.Handler() {

          @Override
          public void onDataShieldMethodCreated(DataShieldMethodCreatedEvent event) {
            updateDataShieldPackages();
          }
        }));
    registerHandler(
        getEventBus().addHandler(DataShieldPackageCreatedEvent.getType(), new DataShieldPackageCreatedEvent.Handler() {

          @Override
          public void onDataShieldPackageCreated(DataShieldPackageCreatedEvent event) {
            updateDataShieldPackages();
          }
        }));

    FieldUpdater<RPackageDto, String> updater = new PackageNameFieldUpdater();
    getView().setPackageNameFieldUpdater(updater);
  }

  @Override
  protected void onReveal() {
    authorize();
  }

  // @Override
  public void authorize(HasAuthorization authorizer) {
    authorizePackagesR(CascadingAuthorizer.newBuilder()//
        .or(AclRequest.newResourceAuthorizationRequestBuilder())//
        .authorize(authorizer).build());
  }

  private void authorize() {
    // view methods
    authorizePackagesR(new CompositeAuthorizer(getView().getPackagesAuthorizer(), new PackagesUpdate()));
    // create method
    authorizeAddPackageR(getView().getAddPackageAuthorizer());
  }

  private String packagesR() {
    return UriBuilder.create().segment("datashield", "packages").build();
  }

  private String packageR(String packageR) {
    return UriBuilder.create().segment("datashield", "package", "{package}").build(packageR);
  }

  private void authorizePackagesR(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(packagesR()).get().authorize(authorizer).send();
  }

  private void authorizeAddPackageR(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(packagesR()).post().authorize(authorizer)
        .send();
  }

  private void authorizeViewMethod(RPackageDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(packageR(dto.getName())).get()
        .authorize(authorizer).send();
  }

  private void authorizeDeleteMethod(RPackageDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(packageR(dto.getName())).delete()
        .authorize(authorizer).send();
  }

  private void updateDataShieldPackages() {
    ResourceRequestBuilderFactory.<JsArray<RPackageDto>>newBuilder().forResource(packagesR()).get()//
        .withCallback(new ResourceCallback<JsArray<RPackageDto>>() {

          @Override
          public void onResource(Response response, JsArray<RPackageDto> resource) {
            getView().renderDataShieldPackagesRows(JsArrays.toSafeArray(resource));
          }
        }).send();
  }

  protected void doDataShieldPackageActionImpl(final RPackageDto dto, String actionName) {
    if(actionName.equals(ActionsPackageRColumn.PUBLISH_ACTION)) {
//      authorizeEditMethod(dto, new Authorizer(getEventBus()) {
//
//        @Override
//        public void authorized() {
//          dataShieldMethodPresenter.showPackage(dto);
//          addToPopupSlot(dataShieldMethodPresenter);
//        }
//      });

    } else if(actionName.equals(ActionsPackageRColumn.REMOVE_ACTION)) {
      authorizeDeleteMethod(dto, new Authorizer(getEventBus()) {
        @Override
        public void authorized() {
          removePackageConfirmation = new Runnable() {
            @Override
            public void run() {
              removeDataShieldPackage(dto);
            }
          };
          getEventBus().fireEvent(ConfirmationRequiredEvent
              .createWithKeys(removePackageConfirmation, "deleteDataShieldPackage", "confirmDeleteDataShieldPackage"));
        }
      });
    }
  }

  private void removeDataShieldPackage(final RPackageDto dto) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          updateDataShieldPackages();
        } else {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
        }
      }

    };

    ResourceRequestBuilderFactory.newBuilder().forResource(packageR(dto.getName())).delete() //
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
  private final class PackagesUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      updateDataShieldPackages();
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removePackageConfirmation != null && event.getSource().equals(removePackageConfirmation) &&
          event.isConfirmed()) {
        removePackageConfirmation.run();
        removePackageConfirmation = null;
      }
    }
  }

  private class PackageNameFieldUpdater implements FieldUpdater<RPackageDto, String> {
    @Override
    public void update(int index, final RPackageDto dto, String value) {
      authorizeViewMethod(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          dataShieldPackagePresenter.displayPackage(dto);
          addToPopupSlot(dataShieldPackagePresenter);
        }
      });
    }
  }

  public interface Display extends View {

    void renderDataShieldPackagesRows(JsArray<RPackageDto> rows);

    HasActionHandler<RPackageDto> getDataShieldPackageActionsColumn();

    HandlerRegistration addPackageHandler(ClickHandler handler);

    HasAuthorization getAddPackageAuthorizer();

    HasAuthorization getPackagesAuthorizer();

    void setPackageNameFieldUpdater(FieldUpdater<RPackageDto, String> updater);

  }

}
