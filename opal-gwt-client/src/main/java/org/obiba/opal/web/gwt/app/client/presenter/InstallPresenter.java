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
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class InstallPresenter extends Presenter<InstallPresenter.Display, InstallPresenter.Proxy> implements InstallUiHandlers {

  public interface Display extends View, HasUiHandlers<InstallUiHandlers> {

    void setUsername(String username);
  }

  @ProxyStandard
  @NameToken(Places.INSTALL)
  public interface Proxy extends ProxyPlace<InstallPresenter> {}

  public enum Slot {
    IDENTIFIERS, DATA
  }
  private final RequestCredentials credentials;
  private final IdentifiersDatabasePresenter identifiersDatabasePresenter;

  private final DataDatabasesPresenter dataDatabasesPresenter;

  @Inject
  public InstallPresenter(Display display, EventBus eventBus, Proxy proxy, RequestCredentials credentials,
      IdentifiersDatabasePresenter identifiersDatabasePresenter, DataDatabasesPresenter dataDatabasesPresenter) {
    super(eventBus, display, proxy, RevealType.Root);
    this.credentials = credentials;
    this.identifiersDatabasePresenter = identifiersDatabasePresenter;
    this.dataDatabasesPresenter = dataDatabasesPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    setInSlot(Slot.IDENTIFIERS, identifiersDatabasePresenter);
    setInSlot(Slot.DATA, dataDatabasesPresenter);
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
    getEventBus().fireEvent(new SessionEndedEvent());
  }
}
