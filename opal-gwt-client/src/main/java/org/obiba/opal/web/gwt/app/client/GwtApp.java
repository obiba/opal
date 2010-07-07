package org.obiba.opal.web.gwt.app.client;

import org.obiba.opal.web.gwt.app.client.event.SessionExpiredEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.inject.client.OpalGinjector;
import org.obiba.opal.web.gwt.rest.client.DefaultResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtApp implements EntryPoint {

  private final OpalGinjector opalGinjector = GWT.create(OpalGinjector.class);

  @Override
  public void onModuleLoad() {
    // TODO: is there a better way to provide the dependencies to instances created with GWT.create()?
    DefaultResourceRequestBuilder.setup(opalGinjector);

    initFileDownloadPresenter();
    initApplicationPresenter();
    initLoginPresenter();
    initTableSelectorPresenter();

    registerHandlers();
  }

  private void initApplicationPresenter() {
    ApplicationPresenter presenter = opalGinjector.getApplicationPresenter();
    presenter.bind();
    presenter.revealDisplay();
    RootLayoutPanel.get().add(presenter.getDisplay().asWidget());
  }

  private void initLoginPresenter() {
    LoginPresenter loginPresenter = opalGinjector.getLoginPresenter();
    loginPresenter.bind();
    // Only display login if we don't currently have any credentials.
    if(opalGinjector.getRequestCredentials().hasCredentials() == false) {

      loginPresenter.revealDisplay();
    }
  }

  private void initFileDownloadPresenter() {
    FileDownloadPresenter fileDownloadPresenter = opalGinjector.getFileDownloadPresenter();
    fileDownloadPresenter.bind();
    RootLayoutPanel.get().add(fileDownloadPresenter.getDisplay().asWidget());
  }

  private void initTableSelectorPresenter() {
    TableSelectorPresenter tableSelectorPresenter = opalGinjector.getTableSelectorPresenter();
    tableSelectorPresenter.bind();
  }

  private void registerHandlers() {
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
    opalGinjector.getEventBus().addHandler(SessionExpiredEvent.getType(), new SessionExpiredEvent.Handler() {
      @Override
      public void onSessionExpired(SessionExpiredEvent event) {
        GWT.log("Session expired");
        opalGinjector.getLoginPresenter().revealDisplay();
      }
    });
  }
}
