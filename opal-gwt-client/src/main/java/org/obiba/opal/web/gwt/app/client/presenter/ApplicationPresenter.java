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
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.ui.HasUrl;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueMapPopupPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.ui.MenuItem;
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

    HasUrl getDownloder();

    NavLink getAdministrationItem();

    NavLink getDatasourcesItem();

    HasAuthorization getAdministrationAuthorizer();

    NavLink getDashboardItem();

    void setCurrentSelection(MenuItem selection);

    void clearSelection();

    void setUsername(String username);

    void setVersion(String version);

  }

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> WORKBENCH = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<ApplicationPresenter> {}

  private final RequestCredentials credentials;

  private final NotificationPresenter messageDialog;

  private final Provider<FileSelectorPresenter> fileSelectorPresenter;

  private final Provider<ValueMapPopupPresenter> valueMapPopupPresenter;

  private final RequestUrlBuilder urlBuilder;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public ApplicationPresenter(Display display, Proxy proxy, EventBus eventBus, RequestCredentials credentials,
      NotificationPresenter messageDialog, Provider<FileSelectorPresenter> fileSelectorPresenter,
      Provider<ValueMapPopupPresenter> valueMapPopupPresenter, RequestUrlBuilder urlBuilder) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.fileSelectorPresenter = fileSelectorPresenter;
    this.valueMapPopupPresenter = valueMapPopupPresenter;
    this.urlBuilder = urlBuilder;
  }

  @Override
  protected void revealInParent() {
    RevealRootLayoutContentEvent.fire(this, this);
  }

  @Override
  protected void onBind() {

    registerHandler(
        getEventBus().addHandler(FileSelectionRequiredEvent.getType(), new FileSelectionRequiredEvent.Handler() {

          @Override
          public void onFileSelectionRequired(FileSelectionRequiredEvent event) {
            FileSelectorPresenter fsp = fileSelectorPresenter.get();
            fsp.handle(event);
            addToPopupSlot(fsp);
          }
        }));
    registerHandler(getEventBus().addHandler(GeoValueDisplayEvent.getType(), new GeoValueDisplayEvent.Handler() {

      @Override
      public void onGeoValueDisplay(GeoValueDisplayEvent event) {
        ValueMapPopupPresenter vmp = valueMapPopupPresenter.get();
        vmp.handle(event);
        addToPopupSlot(vmp);
      }
    }));
    registerHandler(getEventBus().addHandler(FileDownloadEvent.getType(), new FileDownloadEvent.Handler() {

      @Override
      public void onFileDownload(FileDownloadEvent event) {
        getView().getDownloder().setUrl(urlBuilder.buildAbsoluteUrl(event.getUrl()));
      }
    }));

    getView().getDashboardItem().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.dashboardPlace));
      }
    });

    getView().getAdministrationItem().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.administrationPlace));
      }
    });

    getView().getDatasourcesItem().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new PlaceChangeEvent(Places.navigatorPlace));
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

    registerUserMessageEventHandler();
  }

  @Override
  protected void onReveal() {
    getView().setUsername(credentials.getUsername());
    getView().setVersion(ResourceRequestBuilderFactory.newBuilder().getVersion());
    authorize();
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").get()
        .authorize(new UIObjectAuthorizer(getView().getDatasourcesItem())).send();

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
    registerHandler(getEventBus().addHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        messageDialog.setNotification(event);
        // false : don't center the dialog
        addToPopupSlot(messageDialog, false);
      }
    }));
  }
}
