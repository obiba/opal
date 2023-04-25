/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client;

import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.cart.service.CartService;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.fs.service.FileService;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.project.resources.ResourceProvidersService;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.event.RequestCredentialsExpiredEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestEventBus;
import org.obiba.opal.web.model.client.database.DatabasesStatusDto;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.Subject;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class OpalBootstrapperImpl implements Bootstrapper {

  private final PlaceManager placeManager;

  @Inject
  EventBus eventBus;

  @Inject
  ResourceAuthorizationCache authorizationCache;

  @Inject
  RequestCredentials requestCredentials;

  @Inject
  ConfirmationPresenter confirmationPresenter;

  @Inject
  CartService cartService;

  @Inject
  FileService fileService;

  @Inject
  ResourceProvidersService resourceProvidersService;

  private GeneralConf generalConf;

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

    registerHandlers();

    initConfirmationPresenter();

    initUserSession();
  }

  private void initUserSession() {
    initUsernameFromSession();
    refreshGeneralConfig();
  }

  private void initUsernameFromSession() {
    // try to get the username
    ResourceRequestBuilderFactory.<Subject>newBuilder()
        .forResource(UriBuilders.AUTH_SESSION_CURRENT_USERNAME.create().build())
        .withCallback(new SubjectResourceCallback())
        .get().send();
  }

  private void initConfirmationPresenter() {
    confirmationPresenter.bind();
  }

  private void registerHandlers() {
    eventBus.addHandler(RequestErrorEvent.getType(), new RequestErrorEvent.RequestErrorHandler() {
      @Override
      public void onRequestError(RequestErrorEvent e) {
        GWT.log("Request error: ", e.getException());
      }
    });
    eventBus.addHandler(RequestCredentialsExpiredEvent.getType(), new RequestCredentialsExpiredEvent.Handler() {
      @Override
      public void onCredentialsExpired(RequestCredentialsExpiredEvent e) {
        requestCredentials.invalidate();
        placeManager.revealUnauthorizedPlace(Places.LOGIN);
      }
    });
    eventBus.addHandler(SessionCreatedEvent.getType(), new SessionCreatedEvent.Handler() {
      @Override
      public void onSessionCreated(SessionCreatedEvent event) {
        cartService.clear();
        fileService.clear();
        resourceProvidersService.reset();
        revealCurrentPlace();
      }
    });

    eventBus.addHandler(SessionEndedEvent.getType(), new SessionEndedEvent.Handler() {

      @Override
      public void onSessionEnded(SessionEndedEvent event) {
        if (generalConf != null && generalConf.hasLogoutURL())
          Window.Location.replace(generalConf.getLogoutURL());
        else
          Window.Location.replace("..");
      }
    });

    eventBus.addHandler(GeneralConfigSavedEvent.getType(), new GeneralConfigSavedEvent.GeneralConfigSavedHandler() {
      @Override
      public void onGeneralConfigSaved(GeneralConfigSavedEvent event) {
        refreshGeneralConfig();
      }
    });

  }

  private void refreshGeneralConfig() {
    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()//
        .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
        .withCallback(new ResourceCallback<GeneralConf>() {

          @Override
          public void onResource(Response response, GeneralConf resource) {
            generalConf = resource;
          }
        }).get().send();
  }

  private void revealCurrentPlace() {
    placeManager.revealCurrentPlace();
  }

  private class SubjectResourceCallback implements ResourceCallback<Subject> {
    @Override
    public void onResource(Response response, Subject subject) {
      if(response.getStatusCode() == Response.SC_OK) {
        requestCredentials.setUsername(subject.getPrincipal());
        revealCurrentPlace();
      } else {
        placeManager.revealUnauthorizedPlace(Places.LOGIN);
      }
    }
  }
}
