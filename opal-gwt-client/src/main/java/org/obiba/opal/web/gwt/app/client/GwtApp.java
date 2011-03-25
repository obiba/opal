package org.obiba.opal.web.gwt.app.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.resources.OpalResources;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardManager;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.inject.client.OpalGinjector;
import org.obiba.opal.web.gwt.rest.client.DefaultResourceAuthorizationRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.DefaultResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationCache;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.event.RequestCredentialsExpiredEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestEventBus;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtApp implements EntryPoint {

  private final OpalGinjector opalGinjector = GWT.create(OpalGinjector.class);

  private Widget rootWidget;

  @Override
  public void onModuleLoad() {

    final EventBus bus = opalGinjector.getEventBus();
    ResourceAuthorizationCache authorizationCache = opalGinjector.getResourceAuthorizationCache();
    // TODO: is there a better way to provide the dependencies to instances created with GWT.create()?
    DefaultResourceRequestBuilder.setup(new RequestEventBus() {

      @Override
      public void fireEvent(GwtEvent<?> event) {
        bus.fireEvent(event);
      }
    }, opalGinjector.getRequestCredentials(), authorizationCache);

    DefaultResourceAuthorizationRequestBuilder.setup(authorizationCache);

    OpalResources.INSTANCE.css().ensureInjected();
    OpalResources.INSTANCE.cssCustom().ensureInjected();
    initFileDownloadPresenter();
    initFileSelectorPresenter();
    initApplicationPresenter();
    initLoginPresenter();
    initTableSelectorPresenter();
    initConfirmationPresenter();
    initViewWizards();
    initWizardManager();

    updateRootLayout();

    registerHandlers();
  }

  private void updateRootLayout() {
    // Only display login if we don't currently have any credentials.
    if(opalGinjector.getRequestCredentials().hasCredentials() == false) {
      revealDisplay(opalGinjector.getLoginPresenter());
    } else {
      revealDisplay(opalGinjector.getApplicationPresenter());
    }
  }

  private void revealDisplay(WidgetPresenter<?> presenter) {
    WidgetDisplay newRoot = presenter.getDisplay();
    if(rootWidget != null) {
      RootLayoutPanel.get().remove(rootWidget);
    }
    rootWidget = newRoot.asWidget();
    RootLayoutPanel.get().add(rootWidget);
    presenter.revealDisplay();
  }

  private void initApplicationPresenter() {
    ApplicationPresenter presenter = opalGinjector.getApplicationPresenter();
    presenter.bind();
  }

  private void initLoginPresenter() {
    LoginPresenter loginPresenter = opalGinjector.getLoginPresenter();
    loginPresenter.bind();
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

  private void initViewWizards() {
    ConfigureViewStepPresenter configureViewStepPresenter = opalGinjector.getConfigureViewStepPresenter();
    configureViewStepPresenter.bind();
  }

  private void initWizardManager() {
    WizardManager wizardManager = opalGinjector.getWizardManager();
    wizardManager.bind();
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
    opalGinjector.getEventBus().addHandler(RequestCredentialsExpiredEvent.getType(), new RequestCredentialsExpiredEvent.Handler() {
      @Override
      public void onCredentialsExpired(RequestCredentialsExpiredEvent e) {
        revealDisplay(opalGinjector.getLoginPresenter());
      }
    });
    opalGinjector.getEventBus().addHandler(SessionCreatedEvent.getType(), new SessionCreatedEvent.Handler() {
      @Override
      public void onSessionCreated(SessionCreatedEvent event) {
        GWT.log("Session created");
        updateRootLayout();
      }
    });

    opalGinjector.getEventBus().addHandler(SessionEndedEvent.getType(), new SessionEndedEvent.Handler() {

      @Override
      public void onSessionEnded(SessionEndedEvent event) {
        GWT.log("Session ended");
        // swap the interface before the credentials are gone
        revealDisplay(opalGinjector.getLoginPresenter());
        RequestCredentials credentials = opalGinjector.getRequestCredentials();
        if(credentials != null && credentials.hasCredentials()) {
          // calling this makes the session expired event to be fired in return
          ResourceRequestBuilderFactory.newBuilder().forResource("/auth/session/" + credentials.extractCredentials()).delete().send();

          credentials.invalidate();
        }

      }
    });

    // Kills the session it the browser is closed or when navigating to another page.
    Window.addWindowClosingHandler(new ClosingHandler() {

      @Override
      public void onWindowClosing(ClosingEvent arg0) {
        opalGinjector.getEventBus().fireEvent(new SessionEndedEvent());
      }

    });

  }

}
