/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import org.obiba.opal.web.gwt.app.client.administration.users.changePassword.ChangePasswordModalPresenter;
import org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Current subject page.
 */
public class SubjectProfilePresenter extends Presenter<SubjectProfilePresenter.Display, SubjectProfilePresenter.Proxy>
    implements SubjectProfileUiHandlers {

  @ProxyStandard
  @NameToken(Places.PROFILE)
  public interface Proxy extends ProxyPlace<SubjectProfilePresenter> {}

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> BOOKMARKS = new GwtEvent.Type<RevealContentHandler<?>>();

  private SubjectProfileDto profile;

  private final ModalProvider<ChangePasswordModalPresenter> changePasswordModalProvider;

  private final BookmarkListPresenter bookmarkListPresenter;

  @Inject
  public SubjectProfilePresenter(EventBus eventBus, Display display, Proxy proxy,
      ModalProvider<ChangePasswordModalPresenter> changePasswordProvider, BookmarkListPresenter bookmarkListPresenter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    changePasswordModalProvider = changePasswordProvider.setContainer(this);
    this.bookmarkListPresenter = bookmarkListPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<SubjectProfileDto>newBuilder() //
        .forResource(UriBuilders.PROFILE.create().build("_current")) //
        .withCallback(new ResourceCallback<SubjectProfileDto>() {
          @Override
          public void onResource(Response response, SubjectProfileDto resource) {
            if(response.getStatusCode() == Response.SC_OK) {
              profile = resource;
              getView().enableChangePassword("opal-user-realm".equals(resource.getRealm()), resource.getRealm(), resource.getAccountUrl());
            } else {
              getView().enableChangePassword(false, "?", null);
            }
          }
        }) //
        .get().send();
    setInSlot(BOOKMARKS, bookmarkListPresenter);
  }

  @Override
  public void onChangePassword() {
    changePasswordModalProvider.get().setPrincipal(profile.getPrincipal());
  }

  public interface Display extends View, HasUiHandlers<SubjectProfileUiHandlers> {

    void enableChangePassword(boolean enabled, String realm, String accountUrl);

  }
}
