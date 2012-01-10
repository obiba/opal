/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootLayoutContentEvent;

/**
 *
 */
public class ApplicationPresenter extends Presenter<ApplicationPresenter.Display, ApplicationPresenter.Proxy> {

  public interface Display extends View {

    HasClickHandlers getQuit();

    HasClickHandlers getHelp();

    HasClickHandlers getAdministration();

    MenuItem getDatasourcesItem();

    void updateWorkbench(Widget workbench);

    HasAuthorization getAdministrationAuthorizer();

    MenuItem getListJobsItem();

    MenuItem getFileExplorerItem();

    MenuItem getDashboardItem();

    MenuItem getReportsItem();

    MenuItem getUnitsItem();

    void setCurrentSelection(MenuItem selection);

    void clearSelection();

    void setUsername(String username);

    void setVersion(String version);

  }

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> WORKBENCH = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<ApplicationPresenter> {
  }

  private final RequestCredentials credentials;

  private final NotificationPresenter messageDialog;

  // TODO: decouple these presenters. This should be moved to FileSelectionPresenter when it becomes a PresenterWidget
  private final Provider<FileSelectorPresenter> fileSelectorPresenter;

  @Inject
  public ApplicationPresenter(final Display display, final Proxy proxy, final EventBus eventBus, RequestCredentials credentials, NotificationPresenter messageDialog, Provider<FileSelectorPresenter> fileSelectorPresenter) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.fileSelectorPresenter = fileSelectorPresenter;
  }

  @Override
  protected void revealInParent() {
    RevealRootLayoutContentEvent.fire(this, this);
  }

  @Override
  protected void onBind() {

    super.registerHandler(getEventBus().addHandler(FileSelectionRequiredEvent.getType(), new FileSelectionRequiredEvent.Handler() {

      @Override
      public void onFileSelectionRequired(FileSelectionRequiredEvent event) {
        FileSelectorPresenter fsp = fileSelectorPresenter.get();
        fsp.handle(event);
        addToPopupSlot(fsp);
      }
    }));

    getView().getDashboardItem().setCommand(new Command() {

      @Override
      public void execute() {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.dashboardPlace));
      }
    });

    getView().getReportsItem().setCommand(new Command() {

      @Override
      public void execute() {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.reportTemplatesPlace));
      }
    });

    getView().getUnitsItem().setCommand(new Command() {

      @Override
      public void execute() {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.unitsPlace));
      }
    });

    getView().getDatasourcesItem().setCommand(new Command() {

      @Override
      public void execute() {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.navigatorPlace));
      }
    });

    getView().getListJobsItem().setCommand(new Command() {

      @Override
      public void execute() {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.jobsPlace));
      }
    });

    getView().getFileExplorerItem().setCommand(new Command() {

      @Override
      public void execute() {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.filesPlace));
      }
    });

    getView().getQuit().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new SessionEndedEvent());
      }
    });

    getView().getHelp().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        HelpUtil.openPage();
      }
    });

    getView().getAdministration().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.administrationPlace));
      }
    });

    registerUserMessageEventHandler();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  protected void onReveal() {
    getView().setUsername(credentials.getUsername());
    getView().setVersion(ResourceRequestBuilderFactory.newBuilder().getVersion());
    authorize();
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").get().authorize(new UIObjectAuthorizer(getView().getDatasourcesItem())).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units").get().authorize(new UIObjectAuthorizer(getView().getUnitsItem())).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").get().authorize(new UIObjectAuthorizer(getView().getReportsItem())).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files/meta").get().authorize(new UIObjectAuthorizer(getView().getFileExplorerItem())).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/commands").get().authorize(new UIObjectAuthorizer(getView().getListJobsItem())).send();

    getEventBus().fireEvent(new RequestAdministrationPermissionEvent(new HasAuthorization() {

      @Override
      public void unauthorized() {
      }

      @Override
      public void beforeAuthorization() {
        getView().getAdministrationAuthorizer().beforeAuthorization();
      }

      @Override
      public void authorized() {
        getView().getAdministrationAuthorizer().authorized();
      }
    }));
  }

  private void registerUserMessageEventHandler() {
    super.registerHandler(getEventBus().addHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        messageDialog.setNotification(event);
        // false : don't center the dialog
        addToPopupSlot(messageDialog, false);
      }
    }));
  }
}
