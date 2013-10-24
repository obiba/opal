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

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DataDatabasesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.IdentifiersDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.database.DatabasesStatusDto;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class PostInstallPresenter extends Presenter<PostInstallPresenter.Display, PostInstallPresenter.Proxy>
    implements PostInstallUiHandlers {

  public interface Display extends View, HasUiHandlers<PostInstallUiHandlers> {

    void setUsername(String username);
  }

  @ProxyStandard
  @NameToken(Places.INSTALL)
  public interface Proxy extends ProxyPlace<PostInstallPresenter> {}

  public enum Slot {
    IDENTIFIERS, DATA, NOTIFICATION
  }

  private final PlaceManager placeManager;

  private final RequestCredentials credentials;

  private final NotificationPresenter messageDialog;

  private final IdentifiersDatabasePresenter identifiersDatabasePresenter;

  private final DataDatabasesPresenter dataDatabasesPresenter;

  @Inject
  public PostInstallPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      RequestCredentials credentials, NotificationPresenter messageDialog,
      IdentifiersDatabasePresenter identifiersDatabasePresenter, DataDatabasesPresenter dataDatabasesPresenter) {
    super(eventBus, display, proxy, RevealType.Root);
    this.placeManager = placeManager;
    this.credentials = credentials;
    this.messageDialog = messageDialog;
    this.identifiersDatabasePresenter = identifiersDatabasePresenter;
    this.dataDatabasesPresenter = dataDatabasesPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    setInSlot(Slot.IDENTIFIERS, identifiersDatabasePresenter);
    identifiersDatabasePresenter.onAdministrationPermissionRequest(null);
    setInSlot(Slot.DATA, dataDatabasesPresenter);
    dataDatabasesPresenter.onAdministrationPermissionRequest(null);

    addRegisteredHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        if(isVisible()) {
          messageDialog.setNotification(event);
          setInSlot(Slot.NOTIFICATION, messageDialog);
        }
      }
    });
  }

  @Override
  protected void onReveal() {
    getView().setUsername(credentials.getUsername());
  }

  @Override
  public void onHelp() {
    HelpUtil.openPage();
  }

  @Override
  public void onQuit() {
    fireEvent(new SessionEndedEvent());
  }

  @Override
  public void onGoToMain() {
    ResourceRequestBuilderFactory.<DatabasesStatusDto>newBuilder()
        .forResource(UriBuilder.create().segment("system", "status", "databases").build()).get()
        .withCallback(new ResourceCallback<DatabasesStatusDto>() {
          @Override
          public void onResource(Response response, DatabasesStatusDto resource) {
            if(resource.getHasIdentifiers() && resource.getHasStorage()) {
              placeManager.revealDefaultPlace();
            } else {
              fireEvent(NotificationEvent.newBuilder().error("PostInstallNotCompleted.").build());
            }
          }
        }).send();
  }
}
