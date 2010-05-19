package org.obiba.opal.web.gwt.app.client;

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.view.LoginView;
import org.obiba.opal.web.gwt.inject.client.OpalGinjector;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

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
    displayLogin();

    opalGinjector.getEventBus().addHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {

      @Override
      public void onUnhandledResponse(UnhandledResponseEvent e) {
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

  private void displayLogin() {
    final PopupPanel loginPopup = new PopupPanel(true, false);
    loginPopup.add(new LoginView());
    loginPopup.show();
    loginPopup.setPopupPositionAndShow(new PositionCallback() {

      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = (Window.getClientWidth() - offsetWidth) / 2;
        int top = (Window.getClientHeight() - offsetHeight) / 2;
        loginPopup.setPopupPosition(left, top);
      }
    });
  }

}
