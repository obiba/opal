/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageRemovedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsPackageRColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldPackageMethodsDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import java.util.List;

public class DataShieldPackageAdministrationPresenter
    extends PresenterWidget<DataShieldPackageAdministrationPresenter.Display>
    implements DataShieldPackageAdministrationUiHandlers {

  private Runnable removePackageConfirmation;

  private Runnable publishMethodsConfirmation;

  private final ModalProvider<DataShieldPackageCreatePresenter> dataShieldPackageCreateModalProvider;

  private final ModalProvider<DataShieldPackagePresenter> dataShieldPackageModalProvider;

  private TranslationMessages translationMessages;

  private Runnable removePackagesConfirmation;

  @Inject
  public DataShieldPackageAdministrationPresenter(Display display, EventBus eventBus,
                                                  ModalProvider<DataShieldPackageCreatePresenter> dataShieldPackageCreateModalProvider,
                                                  ModalProvider<DataShieldPackagePresenter> dataShieldPackageModalProvider,
                                                  TranslationMessages translationMessages) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.translationMessages = translationMessages;
    this.dataShieldPackageCreateModalProvider = dataShieldPackageCreateModalProvider.setContainer(this);
    this.dataShieldPackageModalProvider = dataShieldPackageModalProvider.setContainer(this);
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
        if (actionName != null) {
          doDataShieldPackageActionImpl(dto, actionName);
        }
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
    registerHandler(getView().addPackageHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        DataShieldPackageCreatePresenter dataShieldPackageCreatePresenter = dataShieldPackageCreateModalProvider.get();
        dataShieldPackageCreatePresenter.addNewPackage();
      }
    }));
    addRegisteredHandler(DataShieldMethodCreatedEvent.getType(),
        new DataShieldMethodCreatedEvent.DataShieldMethodCreatedHandler() {

          @Override
          public void onDataShieldMethodCreated(DataShieldMethodCreatedEvent event) {
            updateDataShieldPackages();
          }
        });
    addRegisteredHandler(DataShieldPackageCreatedEvent.getType(),
        new DataShieldPackageCreatedEvent.DataShieldPackageCreatedHandler() {

          @Override
          public void onDataShieldPackageCreated(DataShieldPackageCreatedEvent event) {
            updateDataShieldPackages();
          }
        });

    FieldUpdater<RPackageDto, String> updater = new PackageNameFieldUpdater();
    getView().setPackageNameFieldUpdater(updater);
  }

  @Override
  protected void onReveal() {
    authorize();
  }

  @Override
  public void onRefresh() {
    authorize();
  }

  @Override
  public void deleteAllPackages(List<RPackageDto> packages) {
    removePackagesConfirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASHIELD_PACKAGES.create().build())
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                updateDataShieldPackages();
                fireEvent(new DataShieldPackageRemovedEvent(null));
              }
            }, Response.SC_OK, Response.SC_NO_CONTENT, Response.SC_FORBIDDEN, Response.SC_INTERNAL_SERVER_ERROR)
            .delete().send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(removePackagesConfirmation, translationMessages.removeAllDataShieldPackages(),
            translationMessages.confirmDeleteAllDataShieldPackages()));
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

  private String packageR(String packageR) {
    return UriBuilder.create().segment("datashield", "package", "{package}").build(packageR);
  }

  private String packageRMethods(String packageR) {
    return UriBuilder.create().segment("datashield", "package", "{package}", "methods").build(packageR);
  }

  private void authorizePackagesR(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASHIELD_PACKAGES.create().build()).get().authorize(authorizer).send();
  }

  private void authorizeAddPackageR(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASHIELD_PACKAGES.create().build()).post().authorize(authorizer)
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

  private void authorizePublishMethods(RPackageDto dto, HasAuthorization authorizer) {
    // Check access to delete/put a method
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(packageRMethods(dto.getName())).get()
        .authorize(authorizer).send();
  }

  private void updateDataShieldPackages() {
    ResourceRequestBuilderFactory.<JsArray<RPackageDto>>newBuilder().forResource(UriBuilders.DATASHIELD_PACKAGES.create().build()).get()//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setAddPackageButtonEnabled(false);
            getView().renderDataShieldPackages(null);
            getEventBus()
                .fireEvent(NotificationEvent.newBuilder().error("RConnectionFailed").build());
          }
        }, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_SERVICE_UNAVAILABLE)//
        .withCallback(new ResourceCallback<JsArray<RPackageDto>>() {
          @Override
          public void onResource(Response response, JsArray<RPackageDto> resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              getView().setAddPackageButtonEnabled(true);
              getView().renderDataShieldPackages(JsArrays.toList(resource));
            }
          }
        }).send();
  }

  protected void doDataShieldPackageActionImpl(final RPackageDto dto, String actionName) {
    if (actionName.equals(ActionsPackageRColumn.PUBLISH_ACTION)) {
      authorizePublishMethods(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          publishMethodsConfirmation = new PublishMethodsRunnable(dto);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(publishMethodsConfirmation, translationMessages.publishDataShieldMethods(),
                  translationMessages.confirmPublishDataShieldMethods()));
        }
      });

    } else if (actionName.equals(ActionsPackageRColumn.REMOVE_ACTION)) {
      authorizeDeleteMethod(dto, new Authorizer(getEventBus()) {
        @Override
        public void authorized() {
          removePackageConfirmation = new RemovePackageRunnable(dto);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(removePackageConfirmation, translationMessages.removeDataShieldPackage(),
                  translationMessages.confirmDeleteDataShieldPackage()));
        }
      });
    }
  }

  //
  // Inner Classes / Interfaces
  //

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
      if (removePackageConfirmation != null && event.getSource().equals(removePackageConfirmation) &&
          event.isConfirmed()) {
        removePackageConfirmation.run();
        removePackageConfirmation = null;
      } else if (publishMethodsConfirmation != null && event.getSource().equals(publishMethodsConfirmation) &&
          event.isConfirmed()) {
        publishMethodsConfirmation.run();
        publishMethodsConfirmation = null;
      } else if (removePackagesConfirmation != null && event.getSource().equals(removePackagesConfirmation) &&
          event.isConfirmed()) {
        removePackagesConfirmation.run();
        removePackagesConfirmation = null;
      }
    }
  }

  private class PackageNameFieldUpdater implements FieldUpdater<RPackageDto, String> {
    @Override
    public void update(int index, final RPackageDto dto, String value) {
      authorizeViewMethod(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          DataShieldPackagePresenter dataShieldPackagePresenter = dataShieldPackageModalProvider.get();
          dataShieldPackagePresenter.displayPackage(dto);
        }
      });
    }
  }

  public interface Display extends View, HasUiHandlers<DataShieldPackageAdministrationUiHandlers> {

    void renderDataShieldPackages(List<RPackageDto> rows);

    HasActionHandler<RPackageDto> getDataShieldPackageActionsColumn();

    HandlerRegistration addPackageHandler(ClickHandler handler);

    void setAddPackageButtonEnabled(boolean b);

    HasAuthorization getAddPackageAuthorizer();

    HasAuthorization getPackagesAuthorizer();

    void setPackageNameFieldUpdater(FieldUpdater<RPackageDto, String> updater);

  }

  private class RemovePackageRunnable implements Runnable {

    private final RPackageDto dto;

    public RemovePackageRunnable(RPackageDto dto) {
      this.dto = dto;
    }

    @Override
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          fireEvent(ConfirmationTerminatedEvent.create());
          if (response.getStatusCode() == Response.SC_OK) {
            updateDataShieldPackages();
            fireEvent(new DataShieldPackageRemovedEvent(dto));
          } else {
            fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }

      };

      ResourceRequestBuilderFactory.newBuilder().forResource(packageR(dto.getName())).delete() //
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
          .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
    }
  }

  private class PublishMethodsRunnable implements Runnable {

    private final RPackageDto dto;

    public PublishMethodsRunnable(RPackageDto dto) {
      this.dto = dto;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.<DataShieldPackageMethodsDto>newBuilder()
          .forResource(packageRMethods(dto.getName())).put()//
          .withCallback(new ResourceCallback<DataShieldPackageMethodsDto>() {

            @Override
            public void onResource(Response response, DataShieldPackageMethodsDto resource) {
              fireEvent(ConfirmationTerminatedEvent.create());
              if (response.getStatusCode() == Response.SC_OK) {
                fireEvent(new DataShieldPackageUpdatedEvent(dto));
              } else {
                fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }
          }).send();
    }

  }
}
