package org.obiba.opal.web.gwt.app.client.administration.r.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

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

  //
  // Constructors
  //

  @Inject
  public RAdministrationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
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

  //
  // Inner Classes / Interfaces
  //

  private final class RSessionCreatedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.INFO, "RIsAlive", null).nonSticky());
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
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "RConnectionFailed", null));
    }
  }

  public interface Display extends WidgetDisplay {

    HandlerRegistration addTestRServerHandler(ClickHandler handler);

  }

}
