package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

@Singleton
public class LoggedInGatekeeper implements Gatekeeper {

  private final RequestCredentials credentials;

  @Inject
  public LoggedInGatekeeper(RequestCredentials credentials) {
    this.credentials = credentials;
  }

  @Override
  public boolean canReveal() {
    return credentials.hasCredentials();
  }
}