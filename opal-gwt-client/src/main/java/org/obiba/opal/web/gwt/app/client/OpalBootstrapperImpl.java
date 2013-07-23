package org.obiba.opal.web.gwt.app.client;

import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.rest.client.DefaultResourceAuthorizationRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.DefaultResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationCache;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.event.RequestCredentialsExpiredEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestEventBus;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.opal.Subject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class OpalBootstrapperImpl implements Bootstrapper {
  private final PlaceManager placeManager;

  @Inject
  EventBus eventBus;

  @Inject
  ResourceAuthorizationCache authorizationCache;

  @Inject
  RequestCredentials requestCredentials;

  @Inject
  VariablesToViewPresenter variablesToViewPresenter;

  @Inject
  ConfigureViewStepPresenter configureViewStepPresenter;

  @Inject
  ConfirmationPresenter confirmationPresenter;

  @Inject
  UnhandledResponseNotificationPresenter unhandledResponseNotificationPresenter;


  @Inject
  public OpalBootstrapperImpl(PlaceManager placeManager) {
    this.placeManager = placeManager;
  }

  @Override
  public void onBootstrap() {
    initialize();
  }

  private void initialize() {
    // TODO: is there a better way to provide the dependencies to instances created with GWT.create()?
    DefaultResourceRequestBuilder.setup(new RequestEventBus() {

      @Override
      public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
      }
    }, requestCredentials, authorizationCache);

    DefaultResourceAuthorizationRequestBuilder.setup(authorizationCache);

    initConfirmationPresenter();
    initViewWizards();
    initCopyVariablesPresenter();

    ResourceRequestBuilderFactory.<Subject>newBuilder()
        .forResource("/auth/session/" + requestCredentials.extractCredentials() + "/username").get()
        .withCallback(new ResourceCallback<Subject>() {
          @Override
          public void onResource(Response response, Subject subject) {
            if(response.getStatusCode() == Response.SC_OK) {
              requestCredentials.setUsername(subject.getPrincipal());
            } else {
              // Force logout/login
              ResourceRequestBuilderFactory.newBuilder()
                  .forResource("/auth/session/" + requestCredentials.extractCredentials()).delete()
                  .send();
              requestCredentials.invalidate();
            }
            placeManager.revealCurrentPlace();
          }
        }).send();

    registerHandlers();
  }

  private void initConfirmationPresenter() {
    confirmationPresenter.bind();
  }

  private void initViewWizards() {
    configureViewStepPresenter.bind();
  }

  private void initCopyVariablesPresenter() {
    variablesToViewPresenter.bind();
  }

  private void registerHandlers() {

    eventBus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {

      @Override
      public void onPlaceChange(PlaceChangeEvent event) {
        Places.Place place = (Places.Place) event.getNewPlace();
        PlaceRequest request = new PlaceRequest.Builder().nameToken(place.getName()).build();
        // add the params if any
        for(String name : place.getParameterNames()) {
          request = request.with(name, place.getParameter(name, ""));
        }

        placeManager.revealPlace(request);
      }
    });

    eventBus.addHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {
      @Override
      public void onUnhandledResponse(UnhandledResponseEvent e) {
        RevealRootPopupContentEvent
            .fire(unhandledResponseNotificationPresenter, unhandledResponseNotificationPresenter.withResponseEvent(e));
      }
    });

    eventBus.addHandler(RequestErrorEvent.getType(), new RequestErrorEvent.Handler() {
      @Override
      public void onRequestError(RequestErrorEvent e) {
        GWT.log("Request error: ", e.getException());
      }
    });
    eventBus
        .addHandler(RequestCredentialsExpiredEvent.getType(), new RequestCredentialsExpiredEvent.Handler() {
          @Override
          public void onCredentialsExpired(RequestCredentialsExpiredEvent e) {
            requestCredentials.invalidate();
            placeManager.revealUnauthorizedPlace(Places.login);
          }
        });
    eventBus.addHandler(SessionCreatedEvent.getType(), new SessionCreatedEvent.Handler() {
      @Override
      public void onSessionCreated(SessionCreatedEvent event) {
        placeManager.revealCurrentPlace();
      }
    });

    eventBus.addHandler(SessionEndedEvent.getType(), new SessionEndedEvent.Handler() {

      @Override
      public void onSessionEnded(SessionEndedEvent event) {
        RequestCredentials credentials = requestCredentials;
        if(credentials != null && credentials.hasCredentials()) {
          // calling this makes the session expired event to be fired in return
          ResourceRequestBuilderFactory.newBuilder().forResource("/auth/session/" + credentials.extractCredentials())
              .delete().send();
          credentials.invalidate();
        }
        // Reload application and reset history
        Window.Location.replace("/");
      }
    });

    // Kills the session it the browser is closed or when navigating to another page.
    Window.addWindowClosingHandler(new ClosingHandler() {

      @Override
      public void onWindowClosing(ClosingEvent arg0) {
        // opalGinjector.getEventBus().fireEvent(new SessionEndedEvent());
      }

    });

  }
}
