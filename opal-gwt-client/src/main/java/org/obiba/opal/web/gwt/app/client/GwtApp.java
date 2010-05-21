package org.obiba.opal.web.gwt.app.client;

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.inject.client.OpalGinjector;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtApp implements EntryPoint {

  @Override
  public void onModuleLoad() {
    final OpalGinjector opalGinjector = GWT.create(OpalGinjector.class);

    ApplicationPresenter presenter = opalGinjector.getApplicationPresenter();
    presenter.bind();
    presenter.revealDisplay();

    RootLayoutPanel.get().add(presenter.getDisplay().asWidget());

    LoginPresenter loginPresenter = opalGinjector.getLoginPresenter();
    loginPresenter.bind();
    loginPresenter.revealDisplay();

    final UnhandledResponseNotificationPresenter unhandledResponseNotificationPresenter = opalGinjector.getUnhandledResponseNotificationPresenter();
    unhandledResponseNotificationPresenter.bind();

    opalGinjector.getEventBus().addHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {

      @Override
      public void onUnhandledResponse(UnhandledResponseEvent e) {
        unhandledResponseNotificationPresenter.revealDisplay();
        GWT.log("Unhandled request response: " + e.getRequest().toString());
      }
    });
    opalGinjector.getEventBus().addHandler(RequestErrorEvent.getType(), new RequestErrorEvent.Handler() {
      @Override
      public void onRequestError(RequestErrorEvent e) {
        GWT.log("Request error: ", e.getException());
      }
    });
  }

}
