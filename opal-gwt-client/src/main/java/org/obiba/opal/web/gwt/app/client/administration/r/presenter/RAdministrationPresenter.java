package org.obiba.opal.web.gwt.app.client.administration.r.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 * R related administration.
 */
public class RAdministrationPresenter extends ItemAdministrationPresenter<RAdministrationPresenter.Display> {

  //
  // Instance Variables
  //

  private AuthorizationPresenter authorizationPresenter;

  //
  // Constructors
  //

  @Inject
  public RAdministrationPresenter(final Display display, final EventBus eventBus, AuthorizationPresenter authorizationPresenter) {
    super(display, eventBus);
    this.authorizationPresenter = authorizationPresenter;
  }

  @Override
  public String getName() {
    return "R";
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
    authorizationPresenter.bind();
    // getDisplay().setPermissionsDisplay(authorizationPresenter.getDisplay());

    addEventHandlers();
  }

  private void addEventHandlers() {
    registerHandler(getDisplay().addTestRServerHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResourceRequestBuilderFactory.newBuilder().forResource("/r/sessions").post()//
        .withCallback(Response.SC_CREATED, new RSessionCreatedCallback())//
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new RConnectionFailedCallback()).send();
      }
    }));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    authorizationPresenter.unbind();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {

  }

  //
  // methods
  //

  @Override
  public void authorize(HasAuthorization authorizer) {
    // test r
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/r/sessions").post().authorize(authorizer).send();
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder().authorize(new CompositeAuthorizer(getDisplay().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  //
  // Inner Classes / Interfaces
  //

  /**
  *
  */
  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      authorizationPresenter.setAclRequest("r", AclRequest.newBuilder("Use", "/r/session", "*:GET/*"));
      // authorizationPresenter.refreshDisplay();
    }
  }

  private final class RSessionCreatedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(NotificationEvent.newBuilder().info("RIsAlive").nonSticky().build());
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
      eventBus.fireEvent(NotificationEvent.newBuilder().error("RConnectionFailed", response.getText()).build());
    }
  }

  public interface Display extends WidgetDisplay {

    HandlerRegistration addTestRServerHandler(ClickHandler handler);

    HasAuthorization getPermissionsAuthorizer();

    void setPermissionsDisplay(AuthorizationPresenter.Display display);

  }

}
