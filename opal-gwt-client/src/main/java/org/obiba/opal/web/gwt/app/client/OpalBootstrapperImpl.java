package org.obiba.opal.web.gwt.app.client;

import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class OpalBootstrapperImpl implements Bootstrapper {
  private final PlaceManager placeManager;

  @Inject
  public OpalBootstrapperImpl(PlaceManager placeManager) {
    this.placeManager = placeManager;
  }

  @Override
  public void onBootstrap() {
    initialize();
    placeManager.revealCurrentPlace();
  }

  private void initialize() {
    // TODO place
  }
}
