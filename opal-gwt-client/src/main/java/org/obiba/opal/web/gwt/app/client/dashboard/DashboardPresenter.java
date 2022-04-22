/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.dashboard;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.NewsDto;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter.Mode.VIEW_ONLY;

public class DashboardPresenter extends Presenter<DashboardPresenter.Display, DashboardPresenter.Proxy>
    implements HasPageTitle {

  @ProxyStandard
  @NameToken(Places.DASHBOARD)
  public interface Proxy extends ProxyPlace<DashboardPresenter> {
  }

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
  protected void onBind() {
    initReleaseNotes();
  }

  @Override
  protected void onReveal() {
    authorize();
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datashield/profiles").post()
        .authorize(getView().getDataShieldAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/identifiers/tables").get()
        .authorize(getView().getIdentifiersAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").get()
        .authorize(getView().getReportsAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SHELL_COMMANDS.create().build())
        .get().authorize(getView().getTasksAuthorizer()).send();
    setInSlot(BOOKMARKS, bookmarkListPresenter);
    ResourceRequestBuilderFactory.<SubjectProfileDto>newBuilder() //
        .forResource(UriBuilders.SUBJECT_PROFILE.create().build("_current")) //
        .withCallback(new ResourceCallback<SubjectProfileDto>() {
          @Override
          public void onResource(Response response, SubjectProfileDto resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              List<String> realms = Splitter.on(",").splitToList(resource.getRealm());
              boolean realmCandidate = realms.contains("opal-user-realm") || realms.contains("opal-ini-realm") || realms.contains("obiba-realm");
              getView().showOtpAlert(!resource.getOtpEnabled() && realmCandidate);
            } else {
              getView().showOtpAlert(false);
            }
          }
        }) //
        .get().send();
  }

  private void initReleaseNotes() {
    ResourceRequestBuilderFactory.<NewsDto>newBuilder().forResource("/system/news")
        .withCallback(new ResourceCallback<NewsDto>() {
          @Override
          public void onResource(Response response, NewsDto resource) {
            getView().showNews(resource);
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // ignore
          }
        }, Response.SC_INTERNAL_SERVER_ERROR)
        .get().send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    //
    // Authorization
    //

    HasAuthorization getDataShieldAuthorizer();

    HasAuthorization getIdentifiersAuthorizer();

    HasAuthorization getReportsAuthorizer();

    HasAuthorization getTasksAuthorizer();

    void showNews(NewsDto notes);

    void showOtpAlert(boolean visible);
  }

}
