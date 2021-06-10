/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.packages;

import com.google.common.collect.Sets;
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
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.*;
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
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

import java.util.List;
import java.util.Set;

import static com.google.gwt.http.client.Response.*;

public class DataShieldPackagesPresenter
    extends PresenterWidget<DataShieldPackagesPresenter.Display>
    implements DataShieldPackagesUiHandlers {

  private final ModalProvider<DataShieldPackageInstallModalPresenter> packageInstallModalPresenterProvider;

  private final ModalProvider<DataShieldPackageModalPresenter> packageModalPresenterProvider;

  private final TranslationMessages translationMessages;

  private Runnable removePackagesConfirmation;

  private Runnable removePackageConfirmation;

  private Runnable publishPackageConfirmation;

  private Runnable publishPackagesConfirmation;

  private RServerClusterDto cluster;

  private List<RPackageDto> packages;

  @Inject
  public DataShieldPackagesPresenter(Display display, EventBus eventBus,
                                     ModalProvider<DataShieldPackageInstallModalPresenter> packageInstallModalPresenterProvider,
                                     ModalProvider<DataShieldPackageModalPresenter> packageModalPresenterProvider,
                                     TranslationMessages translationMessages) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.translationMessages = translationMessages;
    this.packageInstallModalPresenterProvider = packageInstallModalPresenterProvider.setContainer(this);
    this.packageModalPresenterProvider = packageModalPresenterProvider.setContainer(this);
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
        DataShieldPackageInstallModalPresenter packageInstallModalPresenter = packageInstallModalPresenterProvider.get();
        packageInstallModalPresenter.setCluster(cluster);
        packageInstallModalPresenter.addNewPackage();
      }
    }));
    addRegisteredHandler(DataShieldMethodCreatedEvent.getType(),
        new DataShieldMethodCreatedEvent.DataShieldMethodCreatedHandler() {

          @Override
          public void onDataShieldMethodCreated(DataShieldMethodCreatedEvent event) {
            if (cluster.getName().equals(event.getProfile()))
              updateDataShieldPackages();
          }
        });
    addRegisteredHandler(DataShieldPackageCreatedEvent.getType(),
        new DataShieldPackageCreatedEvent.DataShieldPackageCreatedHandler() {

          @Override
          public void onDataShieldPackageCreated(DataShieldPackageCreatedEvent event) {
            if (cluster.getName().equals(event.getCluster()))
              updateDataShieldPackages();
          }
        });
    addRegisteredHandler(DataShieldProfileResetEvent.getType(), new DataShieldProfileResetEvent.DataShieldProfileResetHandler() {
      @Override
      public void onDataShieldProfileReset(final DataShieldProfileResetEvent event) {
        if (event.getProfile().getCluster().equals(cluster.getName()) && packages != null && !packages.isEmpty()) {
          publishPackagesConfirmation = new Runnable() {
            @Override
            public void run() {
              UriBuilder builder = UriBuilders.DATASHIELD_PACKAGES_PUBLISH.create();
              Set<String> pkgNames = Sets.newHashSet();
              for (final RPackageDto pkg : packages)
                pkgNames.add(pkg.getName());
              for (String name : pkgNames)
                builder.query("name", name);
              builder.query("profile", event.getProfile().getName());

              ResourceRequestBuilderFactory.<DataShieldPackageMethodsDto>newBuilder()
                  .forResource(builder.build())
                  .put()
                  .withCallback(new ResponseCodeCallback() {
                    @Override
                    public void onResponseCode(Request request, Response response) {
                      fireEvent(NotificationEvent.newBuilder().info("DataShieldProfileReset").args(event.getProfile().getName()).build());
                      fireEvent(new DataShieldPackageUpdatedEvent(event.getProfile().getName(), null));
                      fireEvent(ConfirmationTerminatedEvent.create());
                    }
                  }, SC_OK, SC_NOT_FOUND, SC_BAD_REQUEST, SC_BAD_GATEWAY, SC_INTERNAL_SERVER_ERROR).send();
            }
          };
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(publishPackagesConfirmation, translationMessages.publishAllDataShieldSettings(),
                  translationMessages.confirmPublishAllDataShieldSettings(cluster.getName())));
        }
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
  protected void onUnbind() {
    super.onUnbind();
  }

  @Override
  public void deleteAllPackages(List<RPackageDto> packages) {
    removePackagesConfirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(UriBuilders.DATASHIELD_PACKAGES.create().query("profile", cluster.getName()).build())
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                updateDataShieldPackages();
                fireEvent(new DataShieldPackageRemovedEvent(cluster.getName(), null));
              }
            }, SC_OK, Response.SC_NO_CONTENT, Response.SC_FORBIDDEN, Response.SC_INTERNAL_SERVER_ERROR)
            .delete()
            .send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(removePackagesConfirmation, translationMessages.removeAllDataShieldPackages(),
            translationMessages.confirmDeleteAllDataShieldPackages()));
  }

  // @Override
  public void authorize(HasAuthorization authorizer) {
    authorizePackagesR(CascadingAuthorizer.newBuilder()
        .or(AclRequest.newResourceAuthorizationRequestBuilder())
        .authorize(authorizer).build());
  }

  private void authorize() {
    // view methods
    authorizePackagesR(new CompositeAuthorizer(getView().getPackagesAuthorizer(), new PackagesUpdate()));
    // create method
    authorizeAddPackageR(getView().getAddPackageAuthorizer());
  }

  private String packageR(String packageR) {
    return UriBuilders.DATASHIELD_PACKAGE.create()
        .query("profile", cluster.getName())
        .build(packageR);
  }

  private String packageRMethods(String packageR) {
    return UriBuilders.DATASHIELD_PACKAGE_METHODS.create()
        .query("profile", cluster.getName())
        .build(packageR);
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
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(packageR(dto.getName()))
        .delete()
        .authorize(authorizer).send();
  }

  private void authorizePublishMethods(RPackageDto dto, HasAuthorization authorizer) {
    // Check access to delete/put a method
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(packageRMethods(dto.getName())).get()
        .authorize(authorizer).send();
  }

  private void updateDataShieldPackages() {
    ResourceRequestBuilderFactory.<JsArray<RPackageDto>>newBuilder()
        .forResource(UriBuilders.DATASHIELD_PACKAGES.create().query("profile", cluster.getName()).build())
        .get()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setAddPackageButtonEnabled(false);
            getView().renderDataShieldPackages(null);
            packages = null;
            getEventBus()
                .fireEvent(NotificationEvent.newBuilder().error("RConnectionFailed").build());
          }
        }, SC_NOT_FOUND, SC_INTERNAL_SERVER_ERROR, SC_SERVICE_UNAVAILABLE)
        .withCallback(new ResourceCallback<JsArray<RPackageDto>>() {
          @Override
          public void onResource(Response response, JsArray<RPackageDto> resource) {
            if (response.getStatusCode() == SC_OK) {
              getView().setAddPackageButtonEnabled(true);
              packages = JsArrays.toList(resource);
              getView().renderDataShieldPackages(packages);
            }
          }
        }).send();
  }

  protected void doDataShieldPackageActionImpl(final RPackageDto dto, String actionName) {
    if (actionName.equals(ActionsPackageRColumn.PUBLISH_ACTION)) {
      authorizePublishMethods(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          publishPackageConfirmation = new PublishMethodsRunnable(dto);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(publishPackageConfirmation, translationMessages.publishDataShieldSettings(),
                  translationMessages.confirmPublishDataShieldSettings(dto.getName(), cluster.getName())));
        }
      });
    } else if (actionName.equals(ActionsPackageRColumn.UNPUBLISH_ACTION)) {
      authorizePublishMethods(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          publishPackageConfirmation = new UnPublishMethodsRunnable(dto);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(publishPackageConfirmation, translationMessages.unPublishDataShieldSettings(),
                  translationMessages.confirmUnPublishDataShieldSettings(dto.getName(), cluster.getName())));
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

  public void setCluster(RServerClusterDto cluster) {
    this.cluster = cluster;
    updateDataShieldPackages();
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
      if (event.getSource().equals(removePackageConfirmation) &&
          event.isConfirmed()) {
        removePackageConfirmation.run();
        removePackageConfirmation = null;
      } else if (event.getSource().equals(publishPackageConfirmation) &&
          event.isConfirmed()) {
        publishPackageConfirmation.run();
        publishPackageConfirmation = null;
      } else if (event.getSource().equals(removePackagesConfirmation) &&
          event.isConfirmed()) {
        removePackagesConfirmation.run();
        removePackagesConfirmation = null;
      } else if (event.getSource().equals(publishPackagesConfirmation) && event.isConfirmed()) {
        publishPackagesConfirmation.run();
        publishPackagesConfirmation = null;
      }
    }
  }

  private class PackageNameFieldUpdater implements FieldUpdater<RPackageDto, String> {
    @Override
    public void update(int index, final RPackageDto dto, String value) {
      authorizeViewMethod(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          DataShieldPackageModalPresenter dataShieldPackagePresenter = packageModalPresenterProvider.get();
          dataShieldPackagePresenter.displayPackage(dto);
        }
      });
    }
  }

  public interface Display extends View, HasUiHandlers<DataShieldPackagesUiHandlers> {

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
          if (response.getStatusCode() == SC_OK) {
            updateDataShieldPackages();
            fireEvent(new DataShieldPackageRemovedEvent(cluster.getName(), dto));
          } else {
            fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }

      };

      ResourceRequestBuilderFactory.newBuilder().forResource(packageR(dto.getName())).delete() //
          .withCallback(SC_OK, callbackHandler) //
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
          .forResource(packageRMethods(dto.getName()))
          .put()
          .withCallback(new ResourceCallback<DataShieldPackageMethodsDto>() {

            @Override
            public void onResource(Response response, DataShieldPackageMethodsDto resource) {
              fireEvent(ConfirmationTerminatedEvent.create());
              if (response.getStatusCode() == SC_OK) {
                fireEvent(new DataShieldPackageUpdatedEvent(cluster.getName(), dto));
              } else {
                fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }
          }).send();
    }

  }

  private class UnPublishMethodsRunnable implements Runnable {

    private final RPackageDto dto;

    public UnPublishMethodsRunnable(RPackageDto dto) {
      this.dto = dto;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(packageRMethods(dto.getName()))
          .delete()
          .withCallback(new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(ConfirmationTerminatedEvent.create());
              if (response.getStatusCode() == Response.SC_NO_CONTENT) {
                fireEvent(new DataShieldPackageUpdatedEvent(cluster.getName(), dto));
              } else {
                fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }

          }, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_NOT_FOUND, Response.SC_BAD_REQUEST).send();
    }

  }
}
