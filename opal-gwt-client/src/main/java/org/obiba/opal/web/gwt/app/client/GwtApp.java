package org.obiba.opal.web.gwt.app.client;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionExpiredEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
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
    initFileSelectorPresenter();
    initApplicationPresenter();
    initLoginPresenter();
    initTableSelectorPresenter();
    initConfirmationPresenter();

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
      replaceRootLayoutPanelChild(opalGinjector.getApplicationPresenter().getDisplay(), loginPresenter.getDisplay());
      loginPresenter.revealDisplay();
    }
  }

  private void initFileDownloadPresenter() {
    FileDownloadPresenter fileDownloadPresenter = opalGinjector.getFileDownloadPresenter();
    fileDownloadPresenter.bind();
    RootLayoutPanel.get().add(fileDownloadPresenter.getDisplay().asWidget());
  }

  private void initFileSelectorPresenter() {
    FileSelectorPresenter fileSelectorPresenter = opalGinjector.getFileSelectorPresenter();
    fileSelectorPresenter.bind();
  }

  private void initTableSelectorPresenter() {
    TableSelectorPresenter tableSelectorPresenter = opalGinjector.getTableSelectorPresenter();
    tableSelectorPresenter.bind();
  }

  private void initConfirmationPresenter() {
    ConfirmationPresenter confirmationPresenter = opalGinjector.getConfirmationPresenter();
    confirmationPresenter.bind();
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

        LoginPresenter loginPresenter = opalGinjector.getLoginPresenter();
        replaceRootLayoutPanelChild(opalGinjector.getApplicationPresenter().getDisplay(), loginPresenter.getDisplay());
        loginPresenter.revealDisplay();
      }
    });
    opalGinjector.getEventBus().addHandler(SessionCreatedEvent.getType(), new SessionCreatedEvent.Handler() {
      @Override
      public void onSessionCreated(SessionCreatedEvent event) {
        GWT.log("Session created");

        ApplicationPresenter applicationPresenter = opalGinjector.getApplicationPresenter();
        replaceRootLayoutPanelChild(opalGinjector.getLoginPresenter().getDisplay(), applicationPresenter.getDisplay());
        applicationPresenter.revealDisplay();
      }
    });
  }

  private void replaceRootLayoutPanelChild(WidgetDisplay previousChild, WidgetDisplay newChild) {
    RootLayoutPanel.get().remove(previousChild.asWidget());
    RootLayoutPanel.get().add(newChild.asWidget());
  }
}
