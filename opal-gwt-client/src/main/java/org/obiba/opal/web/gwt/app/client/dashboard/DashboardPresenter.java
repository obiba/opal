/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.dashboard;

import org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

import static org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter.Mode.VIEW_ONLY;

public class DashboardPresenter extends Presenter<DashboardPresenter.Display, DashboardPresenter.Proxy>
    implements HasPageTitle {

  @ProxyStandard
  @NameToken(Places.DASHBOARD)
  public interface Proxy extends ProxyPlace<DashboardPresenter> {}

  private final BookmarkListPresenter bookmarkListPresenter;

  private final Translations translations;

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> BOOKMARKS = new GwtEvent.Type<RevealContentHandler<?>>();

  @Inject
  public DashboardPresenter(Display display, EventBus eventBus, Proxy proxy, Translations translations,
      BookmarkListPresenter bookmarkListPresenter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.bookmarkListPresenter = bookmarkListPresenter.setMode(VIEW_ONLY);
  }

  @Override
  public String getTitle() {
    return translations.pageDashboardTitle();
  }

  @Override
  protected void onReveal() {
    authorize();
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/identifiers/tables").get()
        .authorize(getView().getIdentifiersAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").get()
        .authorize(getView().getReportsAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SHELL_COMMANDS.create().build())
        .get().authorize(getView().getTasksAuthorizer()).send();
    setInSlot(BOOKMARKS, bookmarkListPresenter);
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    //
    // Authorization
    //

    HasAuthorization getIdentifiersAuthorizer();

    HasAuthorization getReportsAuthorizer();

    HasAuthorization getTasksAuthorizer();

  }

}
