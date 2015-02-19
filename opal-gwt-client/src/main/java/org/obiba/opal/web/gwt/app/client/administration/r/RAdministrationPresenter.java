package org.obiba.opal.web.gwt.app.client.administration.r;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RSessionsPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ServiceDto;
import org.obiba.opal.web.model.client.opal.ServiceStatus;
import org.obiba.opal.web.model.client.opal.r.RSessionDto;

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

import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_OK;

public class RAdministrationPresenter
    extends ItemAdministrationPresenter<RAdministrationPresenter.Display, RAdministrationPresenter.Proxy>
    implements RAdministrationUiHandlers {

  private final RSessionsPresenter rSessionsPresenter;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public RAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      RSessionsPresenter rSessionsPresenter,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.rSessionsPresenter = rSessionsPresenter;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.breadcrumbsHelper = breadcrumbsHelper;
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
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();

    refreshStatus();
  }

  private void refreshStatus() {
    // stop start R service
    ResourceRequestBuilderFactory.<ServiceDto>newBuilder().forResource("/service/r") //
        .withCallback(new ResourceCallback<ServiceDto>() {
          @Override
          public void onResource(Response response, ServiceDto resource) {
            if(response.getStatusCode() == SC_OK) {
              getView().setServiceStatus(resource.getStatus().isServiceStatus(ServiceStatus.RUNNING)
                  ? Display.Status.Stoppable
                  : Display.Status.Startable);
            }
          }
        }) //
        .get().send();
  }

  @Override
  public void start() {
    // Start service
    getView().setServiceStatus(Display.Status.Pending);
    ResourceRequestBuilderFactory.newBuilder().forResource("/service/r").put().withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == SC_OK) {
          refreshStatus();
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
    ResourceRequestBuilderFactory.newBuilder().forResource("/service/r").delete()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setServiceStatus(
                response.getStatusCode() == SC_OK ? Display.Status.Startable : Display.Status.Stoppable);
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
  public interface Proxy extends ProxyPlace<RAdministrationPresenter> {}

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<RAdministrationUiHandlers> {

    enum Slots {
      RSessions,
      Permissions
    }

    enum Status {
      Startable, Stoppable, Pending
    }

    void setServiceStatus(Status status);

    HasAuthorization getPermissionsAuthorizer();

  }

}
