package org.obiba.opal.web.gwt.app.client.place;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

public class OpalPlaceManager extends PlaceManagerImpl {

  private final PlaceRequest defaultPlaceRequest;

  @Inject
  public OpalPlaceManager(final EventBus eventBus, final TokenFormatter tokenFormatter,
      @DefaultPlace String defaultNameToken) {
    super(eventBus, tokenFormatter);
    this.defaultPlaceRequest = new PlaceRequest(defaultNameToken);
  }

  @Override
  public void revealDefaultPlace() {
    revealPlace(defaultPlaceRequest, false);
  }

  @Override
  public void revealErrorPlace(String invalidHistoryToken) {
    GWT.log("invalid token " + invalidHistoryToken);
  }

  @Override
  public void revealUnauthorizedPlace(String unauthorizedHistoryToken) {
    revealPlace(new PlaceRequest("login"), false);
  }
}