/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.r;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.administration.r.event.RPackageInstalledEvent;
import org.obiba.opal.web.gwt.app.client.administration.r.event.RServerStoppedEvent;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RSessionsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RWorkspacesPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.resources.ResourceProvidersService;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;
import org.obiba.opal.web.model.client.opal.r.RSessionDto;

import java.util.List;

import static com.google.gwt.http.client.Response.*;

public class RAdministrationPresenter
    extends ItemAdministrationPresenter<RAdministrationPresenter.Display, RAdministrationPresenter.Proxy>
    implements RAdministrationUiHandlers {

  private static final String DEFAULT_CULSTER = "default";

  private final RSessionsPresenter rSessionsPresenter;

  private final RWorkspacesPresenter rWorkspacesPresenter;

  private final ModalProvider<RPackageInstallModalPresenter> rPackageInstallModalPresenterModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final ResourceProvidersService resourceProvidersService;

  private Runnable confirmation;

  @Inject
  public RAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
                                  RSessionsPresenter rSessionsPresenter, RWorkspacesPresenter rWorkspacesPresenter,
                                  ModalProvider<RPackageInstallModalPresenter> rPackageInstallModalPresenterModalProvider,
                                  Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                  DefaultBreadcrumbsBuilder breadcrumbsHelper,
                                  ResourceProvidersService resourceProvidersService) {
    super(eventBus, display, proxy);
    this.rSessionsPresenter = rSessionsPresenter;
    this.rWorkspacesPresenter = rWorkspacesPresenter;
    this.rPackageInstallModalPresenterModalProvider = rPackageInstallModalPresenterModalProvider.setContainer(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.resourceProvidersService = resourceProvidersService;
    getView().setUiHandlers(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    authorize(event.getHasAuthorization());
  }

  @Override
  public String getName() {
    return "R";
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageRConfigTitle();
  }

  @Override
  protected void onBind() {
    setInSlot(Display.Slots.RSessions, rSessionsPresenter);
    setInSlot(Display.Slots.RWorkspaces, rWorkspacesPresenter);
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
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/r/sessions").post().authorize(getView().getTestAuthorizer())
        .send();

    refreshCluster();
    refreshPackages();
  }

  private void refreshCluster() {
    ResourceRequestBuilderFactory.<RServerClusterDto>newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER.create().build(DEFAULT_CULSTER)) //
        .withCallback(new ResourceCallback<RServerClusterDto>() {
          @Override
          public void onResource(Response response, RServerClusterDto resource) {
            if (response.getStatusCode() == SC_OK) {
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
        .forResource(UriBuilders.SERVICE_R_CLUSTER_PACKAGES.create().build(DEFAULT_CULSTER)) //
        .withCallback(new ResourceCallback<JsArray<RPackageDto>>() {
          @Override
          public void onResource(Response response, JsArray<RPackageDto> resource) {
            getView().renderPackages(JsArrays.toList(resource));
          }
        }) //
        .get().send();
  }


  @Override
  public void start() {
    // Start service
    getView().setServiceStatus(Display.Status.Pending);
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER.create().build(DEFAULT_CULSTER)).put()
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
  public void stop() {
    // Stop service
    getView().setServiceStatus(Display.Status.Pending);
    getView().renderPackages(null);
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER.create().build(DEFAULT_CULSTER)).delete()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshCluster();
            fireEvent(new RServerStoppedEvent(DEFAULT_CULSTER, null));
          }
        }, SC_OK).send();
  }

  @Override
  public void start(String server) {
    // Start service
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER_SERVER.create().build(DEFAULT_CULSTER, server)).put()
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
  public void stop(final String server) {
    // Stop service
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTER_SERVER.create().build(DEFAULT_CULSTER, server)).delete()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshCluster();
            fireEvent(new RServerStoppedEvent(DEFAULT_CULSTER, server));
          }
        }, SC_OK).send();
  }

  @Override
  public void test() {
    ResourceRequestBuilderFactory.<RSessionDto>newBuilder().forResource("/r/sessions").post()//
        .withCallback(new RSessionCreatedCallback())//
        .withCallback(SC_INTERNAL_SERVER_ERROR, new RConnectionFailedCallback()).send();
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
            .forResource(UriBuilders.SERVICE_R_CLUSTER_PACKAGE.create().build(DEFAULT_CULSTER, rPackage.getName()))
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
    rPackageInstallModalPresenterModalProvider.get();
  }

  @Override
  public void onUpdatePackages() {
    confirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(UriBuilders.SERVICE_R_CLUSTER_PACKAGES.create().build(DEFAULT_CULSTER))
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                refreshPackages();
              }
            }, SC_OK, SC_INTERNAL_SERVER_ERROR)
            .put().send();
      }
    };
    String title = translations.updateRPackages();
    String message = translationMessages.confirmUpdateRPackages();
    fireEvent(ConfirmationRequiredEvent.createWithMessages(confirmation, title, message));
  }

  @Override
  public void onDownloadRserveLog() {
    fireEvent(new FileDownloadRequestEvent("/service/r/log/Rserve.log"));
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // test r
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/r/sessions").post().authorize(authorizer)
        .send();
  }

  private final class RSessionCreatedCallback implements ResourceCallback<RSessionDto> {

    @Override
    public void onResource(Response response, RSessionDto resource) {
      fireEvent(NotificationEvent.newBuilder().info("RIsAlive").build());
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource("/r/session/" + resource.getId()) //
          .withCallback(ResponseCodeCallback.NO_OP, SC_OK, SC_INTERNAL_SERVER_ERROR) //
          .delete().send();
    }
  }

  private final class RConnectionFailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("RConnectionFailed").build());
    }
  }

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(Display.Slots.Permissions);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter
          .initialize(ResourcePermissionType.R, ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_R);

      setInSlot(Display.Slots.Permissions, resourcePermissionsPresenter);
    }
  }

  @ProxyStandard
  @NameToken(Places.R)
  public interface Proxy extends ProxyPlace<RAdministrationPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<RAdministrationUiHandlers> {

    enum Slots {
      RSessions,
      RWorkspaces,
      Permissions
    }

    enum Status {
      Startable, Stoppable, Pending
    }

    void setServiceStatus(Status status);

    HasAuthorization getPermissionsAuthorizer();

    HasAuthorization getTestAuthorizer();

    void renderCluster(RServerClusterDto cluster);

    void renderPackages(List<RPackageDto> packages);

  }

}
