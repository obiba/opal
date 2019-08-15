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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
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
import org.obiba.opal.web.gwt.app.client.administration.users.changePassword.ChangePasswordModalPresenter;
import org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.view.NotificationView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;
import org.obiba.opal.web.model.client.opal.SubjectTokenDto;

import java.util.List;

/**
 * Current subject page.
 */
public class SubjectProfilePresenter extends Presenter<SubjectProfilePresenter.Display, SubjectProfilePresenter.Proxy>
    implements SubjectProfileUiHandlers {

  @ProxyStandard
  @NameToken(Places.PROFILE)
  public interface Proxy extends ProxyPlace<SubjectProfilePresenter> {
  }

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> BOOKMARKS = new GwtEvent.Type<RevealContentHandler<?>>();

  private SubjectProfileDto profile;

  private final ModalProvider<ChangePasswordModalPresenter> changePasswordModalProvider;

  private final ModalProvider<AddSubjectTokenModalPresenter> addTokenModalProvider;

  private final BookmarkListPresenter bookmarkListPresenter;

  private final List<String> tokenNames = Lists.newArrayList();

  @Inject
  public SubjectProfilePresenter(EventBus eventBus, Display display, Proxy proxy,
                                 ModalProvider<ChangePasswordModalPresenter> changePasswordProvider,
                                 ModalProvider<AddSubjectTokenModalPresenter> addTokenModalProvider,
                                 BookmarkListPresenter bookmarkListPresenter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    changePasswordModalProvider = changePasswordProvider.setContainer(this);
    this.addTokenModalProvider = addTokenModalProvider.setContainer(this);
    this.bookmarkListPresenter = bookmarkListPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    registerHandler(getEventBus().addHandler(SubjectTokensRefreshEvent.getType(), new SubjectTokensRefreshEvent.SubjectTokensRefreshHandler() {
      @Override
      public void onSubjectTokensRefresh(SubjectTokensRefreshEvent event) {
        String token = event.getToken().getToken();
        fireEvent(NotificationEvent.newBuilder().info("TokenToCopyReminder").build());
        fireEvent(NotificationEvent.newBuilder().success(token).build());
        refreshTokens();
      }
    }));
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<SubjectProfileDto>newBuilder() //
        .forResource(UriBuilders.PROFILE.create().build("_current")) //
        .withCallback(new ResourceCallback<SubjectProfileDto>() {
          @Override
          public void onResource(Response response, SubjectProfileDto resource) {
            List<String> realms = Splitter.on(",").splitToList(resource.getRealm());
            if (response.getStatusCode() == Response.SC_OK) {
              profile = resource;
              getView().enableChangePassword(realms.contains("opal-user-realm"), resource.getRealm(), resource.getAccountUrl());
            } else {
              getView().enableChangePassword(false, "?", null);
            }
            getView().renderGroups(JsArrays.toList(resource.getGroupsArray()));
          }
        }) //
        .get().send();
    setInSlot(BOOKMARKS, bookmarkListPresenter);
    refreshTokens();
  }

  @Override
  public void onChangePassword() {
    changePasswordModalProvider.get().setPrincipal(profile.getPrincipal());
  }

  @Override
  public void onAddToken() {
    AddSubjectTokenModalPresenter presenter = addTokenModalProvider.get();
    presenter.setTokenNames(tokenNames);
  }

  @Override
  public void onRemoveToken(SubjectTokenDto token) {
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.CURRENT_SUBJECT_TOKEN.create().build(token.getName())) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshTokens();
          }
        }) //
        .delete().send();
  }

  private void refreshTokens() {
    // Fetch all providers
    ResourceRequestBuilderFactory.<JsArray<SubjectTokenDto>>newBuilder() //
        .forResource(UriBuilders.CURRENT_SUBJECT_TOKENS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<SubjectTokenDto>>() {
          @Override
          public void onResource(Response response, JsArray<SubjectTokenDto> resource) {
            List<SubjectTokenDto> tokens = JsArrays.toList(resource);
            tokenNames.clear();
            for (SubjectTokenDto tk : tokens) {
              tokenNames.add(tk.getName());
            }
            getView().renderTokens(tokens);
          }
        }) //
        .get().send();
  }

  public interface Display extends View, HasUiHandlers<SubjectProfileUiHandlers> {

    void enableChangePassword(boolean enabled, String realm, String accountUrl);

    void renderTokens(List<SubjectTokenDto> tokens);

    void renderGroups(List<String> groups);
  }
}
