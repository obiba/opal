package org.obiba.opal.web.gwt.app.client.administration.r.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.AclAction;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class RAdministrationPresenter
    extends ItemAdministrationPresenter<RAdministrationPresenter.Display, RAdministrationPresenter.Proxy> {

  public static final Object PermissionSlot = new Object();

  private final AuthorizationPresenter authorizationPresenter;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public RAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      AuthorizationPresenter authorizationPresenter, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.authorizationPresenter = authorizationPresenter;
    this.breadcrumbsHelper = breadcrumbsHelper;
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
  protected void revealInParent() {
    RevealContentEvent.fire(this, PageContainerPresenter.CONTENT, this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageRConfigTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
    authorizationPresenter.setAclRequest("r", new AclRequest(AclAction.R_SESSION_ALL, "/r/session"));
    addEventHandlers();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  private void addEventHandlers() {
    registerHandler(getView().addTestRServerHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResourceRequestBuilderFactory.newBuilder().forResource("/r/sessions").post()//
            .withCallback(Response.SC_CREATED, new RSessionCreatedCallback())//
            .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new RConnectionFailedCallback()).send();
      }
    }));
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // test r
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/r/sessions").post().authorize(authorizer)
        .send();
  }

  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(PermissionSlot);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      setInSlot(PermissionSlot, authorizationPresenter);
    }
  }

  private final class RSessionCreatedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().info("RIsAlive").nonSticky().build());
      // clean up
      ResponseCodeCallback ignore = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          // ignore
        }
      };
      ResourceRequestBuilderFactory.newBuilder().forResource("/r/session/current").delete()//
          .withCallback(Response.SC_OK, ignore).withCallback(Response.SC_INTERNAL_SERVER_ERROR, ignore).send();
    }
  }

  private final class RConnectionFailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("RConnectionFailed", response.getText()).build());
    }
  }

  @ProxyStandard
  @NameToken(Places.r)
  public interface Proxy extends ProxyPlace<RAdministrationPresenter> {}

  public interface Display extends View, HasBreadcrumbs {

    HandlerRegistration addTestRServerHandler(ClickHandler handler);

    HasAuthorization getPermissionsAuthorizer();

  }

}
