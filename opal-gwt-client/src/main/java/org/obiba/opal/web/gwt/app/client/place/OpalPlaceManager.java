package org.obiba.opal.web.gwt.app.client.place;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

@Singleton
public class OpalPlaceManager extends PlaceManagerImpl {

  private final PlaceRequest defaultPlaceRequest;

  @Inject
  public OpalPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter, @DefaultPlace String defaultNameToken) {
    super(eventBus, tokenFormatter);
    defaultPlaceRequest = new PlaceRequest.Builder().nameToken(defaultNameToken).build();
  }

  @Override
  public void revealDefaultPlace() {
    revealPlace(defaultPlaceRequest, true);
  }

  @Override
  public void revealErrorPlace(String invalidHistoryToken) {
    GWT.log("invalid token " + invalidHistoryToken);
    revealPlace(defaultPlaceRequest, true);
  }

  @Override
  public void revealUnauthorizedPlace(String unauthorizedHistoryToken) {
    revealPlace(new PlaceRequest.Builder().nameToken(Places.LOGIN).build(), false);
  }
}