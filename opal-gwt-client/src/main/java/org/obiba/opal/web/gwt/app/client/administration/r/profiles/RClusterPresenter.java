/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.r.profiles;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.r.RPackageInstallModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.event.RPackageInstalledEvent;
import org.obiba.opal.web.gwt.app.client.administration.r.event.RServerStoppedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.resources.ResourceProvidersService;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;
import org.obiba.opal.web.model.client.opal.r.RSessionDto;

import java.util.List;

import static com.google.gwt.http.client.Response.*;

public class RClusterPresenter extends PresenterWidget<RClusterPresenter.Display> implements RClusterUiHandlers {

  private final TranslationMessages translationMessages;

  private final Translations translations;

  private final ModalProvider<RPackageInstallModalPresenter> rPackageInstallModalPresenterModalProvider;

  private final ResourceProvidersService resourceProvidersService;

  private Runnable confirmation;

  private RServerClusterDto cluster;

  @Inject
  public RClusterPresenter(EventBus eventBus, Display view,
                           ModalProvider<RPackageInstallModalPresenter> rPackageInstallModalPresenterModalProvider,
                           ResourceProvidersService resourceProvidersService,
                           Translations translations,
                           TranslationMessages translationMessages) {
    super(eventBus, view);
    this.rPackageInstallModalPresenterModalProvider = rPackageInstallModalPresenterModalProvider.setContainer(this);
    this.resourceProvidersService = resourceProvidersService;
    this.translationMessages = translationMessages;
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    // Register event handlers
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (confirmation != null && event.getSource().equals(confirmation) && event.isConfirmed()) {
          confirmation.run();
          confirmation = null;
        }
      }
    });
    addRegisteredHandler(RPackageInstalledEvent.getType(), new RPackageInstalledEvent.RPackageInstalledHandler() {
      @Override
      public void onRPackageInstalled(RPackageInstalledEvent event) {
        refreshPackages();
      }
    });
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.R_SESSIONS_TEST.create().query("profile", cluster.getName()).build())
        .put().authorize(getView().getTestAuthorizer())
        .send();
  }

  public void setCluster(RServerClusterDto cluster) {
    this.cluster = cluster;
    getView().renderCluster(cluster);
    refreshPackages();
  }

  @Override
  public void onStart() {
    // Start service
    getView().setServiceStatus(Display.Status.Pending);
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER.create().build(cluster.getName())).put()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if (response.getStatusCode() == SC_OK) {
              refreshCluster();
              refreshPackages();
              resourceProvidersService.reset();
            } else {
              getView().setServiceStatus(Display.Status.Startable);
            }
          }
        }, SC_OK).send();
  }

  @Override
  public void onStop() {
    // Stop service
    getView().setServiceStatus(Display.Status.Pending);
    getView().renderPackages(null);
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER.create().build(cluster.getName())).delete()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshCluster();
            fireEvent(new RServerStoppedEvent(cluster.getName(), null));
          }
        }, SC_OK).send();
  }

  @Override
  public void onStart(String server) {
    // Start service
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER_SERVER.create().build(cluster.getName(), server)).put()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if (response.getStatusCode() == SC_OK) {
              refreshCluster();
              refreshPackages();
              resourceProvidersService.reset();
            } else {
              getView().setServiceStatus(Display.Status.Startable);
            }
          }
        }, SC_OK).send();
  }

  @Override
  public void onStop(final String server) {
    // Stop service
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER_SERVER.create().build(cluster.getName(), server)).delete()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshCluster();
            fireEvent(new RServerStoppedEvent(cluster.getName(), server));
          }
        }, SC_OK).send();
  }

  @Override
  public void onDownloadRserveLog(String server) {
    fireEvent(new FileDownloadRequestEvent(UriBuilders.SERVICE_R_CLUSTER_SERVER_LOG.create().build(cluster.getName(), server)));
  }

  @Override
  public void onTest() {
    ResourceRequestBuilderFactory.<RSessionDto>newBuilder()
        .forResource(UriBuilders.R_SESSIONS_TEST.create().query("profile", cluster.getName()).build())
        .put()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if (response.getStatusCode() == SC_OK) {
              fireEvent(NotificationEvent.newBuilder().info("RIsAlive").build());
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error("RConnectionFailed").build());
            }
          }
        }, SC_OK, SC_INTERNAL_SERVER_ERROR, SC_FORBIDDEN)
        .send();
  }

  @Override
  public void onRefreshCluster() {
    refreshCluster();
  }

  @Override
  public void onRemovePackage(final RPackageDto rPackage) {
    confirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.<RPackageDto>newBuilder()
            .forResource(UriBuilders.SERVICE_R_CLUSTER_PACKAGE.create().build(cluster.getName(), rPackage.getName()))
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                refreshPackages();
              }
            }, SC_OK, SC_NO_CONTENT, SC_INTERNAL_SERVER_ERROR, SC_FORBIDDEN, SC_NOT_FOUND)
            .delete().send();
      }
    };
    String title = translations.removeRPackage();
    String message = translationMessages.confirmRemoveRPackage(rPackage.getName());
    fireEvent(ConfirmationRequiredEvent.createWithMessages(confirmation, title, message));
  }

  @Override
  public void onRefreshPackages() {
    refreshPackages();
  }

  @Override
  public void onInstallPackage() {
    rPackageInstallModalPresenterModalProvider.get().setRServerCluster(cluster);
  }

  @Override
  public void onUpdatePackages() {
    confirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(UriBuilders.SERVICE_R_CLUSTER_PACKAGES_UPDATE.create().build(cluster.getName()))
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                String location = response.getHeader("Location");
                String jobId = location.substring(location.lastIndexOf('/') + 1);
                fireEvent(NotificationEvent.newBuilder().info("RPackagesUpdateTask").args(jobId).build());
              }
            }, SC_CREATED, SC_INTERNAL_SERVER_ERROR)
            .post().send();
      }
    };
    String title = translations.updateRPackages();
    String message = translationMessages.confirmUpdateRPackages();
    fireEvent(ConfirmationRequiredEvent.createWithMessages(confirmation, title, message));
  }

  @Override
  public void onDownloadRserveLog() {
    fireEvent(new FileDownloadRequestEvent(UriBuilders.SERVICE_R_CLUSTER_LOG.create().build(cluster.getName())));
  }

  //
  // Private methods
  //

  private void refreshCluster() {
    ResourceRequestBuilderFactory.<RServerClusterDto>newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER.create().build(cluster.getName())) //
        .withCallback(new ResourceCallback<RServerClusterDto>() {
          @Override
          public void onResource(Response response, RServerClusterDto resource) {
            if (response.getStatusCode() == SC_OK) {
              cluster = resource;
              getView().renderCluster(resource);
            } else {
              getView().renderCluster(null);
            }
          }
        })
        .get().send();
  }

  private void refreshPackages() {
    // Fetch all packages
    ResourceRequestBuilderFactory.<JsArray<RPackageDto>>newBuilder() //
        .forResource(UriBuilders.SERVICE_R_CLUSTER_PACKAGES.create().build(cluster.getName())) //
        .withCallback(new ResourceCallback<JsArray<RPackageDto>>() {
          @Override
          public void onResource(Response response, JsArray<RPackageDto> resource) {
            getView().renderPackages(JsArrays.toList(resource));
          }
        }) //
        .get().send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View, HasUiHandlers<RClusterUiHandlers> {

    enum Status {
      Startable, Stoppable, Pending
    }

    void setServiceStatus(Status status);

    void renderCluster(RServerClusterDto cluster);

    void renderPackages(List<RPackageDto> packages);

    HasAuthorization getTestAuthorizer();
  }
}
