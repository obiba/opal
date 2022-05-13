/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.gwtplatform.dispatch.rest.client.core.RequestBuilderFactory;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import org.obiba.opal.spi.resource.Resource;
import org.obiba.opal.web.gwt.app.client.administration.users.changePassword.ChangePasswordModalPresenter;
import org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.*;
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

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final ModalProvider<ChangePasswordModalPresenter> changePasswordModalProvider;

  private final ModalProvider<AddSubjectTokenModalPresenter> addTokenModalProvider;

  private final BookmarkListPresenter bookmarkListPresenter;

  private final List<String> tokenNames = Lists.newArrayList();

  private Runnable confirmation;

  @Inject
  public SubjectProfilePresenter(EventBus eventBus, Display display, Proxy proxy,
                                 Translations translations, TranslationMessages translationMessages,
                                 ModalProvider<ChangePasswordModalPresenter> changePasswordProvider,
                                 ModalProvider<AddSubjectTokenModalPresenter> addTokenModalProvider,
                                 BookmarkListPresenter bookmarkListPresenter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.changePasswordModalProvider = changePasswordProvider.setContainer(this);
    this.addTokenModalProvider = addTokenModalProvider.setContainer(this);
    this.bookmarkListPresenter = bookmarkListPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (event.getSource().equals(confirmation) && event.isConfirmed()) {
          confirmation.run();
          confirmation = null;
        }
      }
    });
    addRegisteredHandler(SubjectTokensRefreshEvent.getType(), new SubjectTokensRefreshEvent.SubjectTokensRefreshHandler() {
      @Override
      public void onSubjectTokensRefresh(SubjectTokensRefreshEvent event) {
        String token = event.getToken().getToken();
        fireEvent(NotificationEvent.newBuilder().info("TokenToCopyReminder").build());
        fireEvent(NotificationEvent.newBuilder().success(token).build());
        refreshTokens();
      }
    });
  }

  @Override
  protected void onReveal() {
    updateProfile(null);
    setInSlot(BOOKMARKS, bookmarkListPresenter);
    refreshTokens();
  }

  private void updateProfile(final String imageUri) {
    ResourceRequestBuilderFactory.<SubjectProfileDto>newBuilder() //
        .forResource(UriBuilders.SUBJECT_PROFILE.create().build("_current")) //
        .withCallback(new ResourceCallback<SubjectProfileDto>() {
          @Override
          public void onResource(Response response, SubjectProfileDto resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              profile = resource;
              List<String> realms = Splitter.on(",").splitToList(resource.getRealm());
              getView().enableChangePassword(realms.contains("opal-user-realm"), resource.getRealm(), resource.getAccountUrl());
              getView().showOtpSwitch(realms.contains("opal-user-realm") || realms.contains("opal-ini-realm"));
              getView().setOtpSwitchState(profile.getOtpEnabled());
              if (!Strings.isNullOrEmpty(imageUri))
                getView().showQrCode(imageUri);
            } else {
              getView().enableChangePassword(false, "?", null);
              getView().showOtpSwitch(false);
            }
            getView().renderGroups(JsArrays.toList(resource.getGroupsArray()));
          }
        }) //
        .get().send();
  }

  @Override
  public void onChangePassword() {
    changePasswordModalProvider.get().setPrincipal(profile.getPrincipal());
  }

  @Override
  public void onOtpSwitch() {
    ResourceRequestBuilder builder = ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SUBJECT_PROFILE_OTP.create().build("_current"));
    if (profile.getOtpEnabled()) {
      builder.withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              updateProfile(null);
            }
          }, Response.SC_OK, Response.SC_NOT_FOUND, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_GATEWAY)
          .delete().send();
    } else {
      builder.accept("text/plain")
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                updateProfile(response.getText());
              } else {
                updateProfile(null);
              }
            }
          }, Response.SC_OK, Response.SC_NOT_FOUND, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_GATEWAY)
          .put().send();
    }
  }

  @Override
  public void onAddToken() {
    AddSubjectTokenModalPresenter presenter = addTokenModalProvider.get();
    presenter.setTokenNames(tokenNames);
  }

  @Override
  public void onAddDataSHIELDToken() {
    AddSubjectTokenModalPresenter presenter = addTokenModalProvider.get();
    presenter.setTokenNames(tokenNames);
    presenter.initDataSHIELD(generateName("datashield"));
  }

  @Override
  public void onAddRToken() {
    AddSubjectTokenModalPresenter presenter = addTokenModalProvider.get();
    presenter.setTokenNames(tokenNames);
    presenter.initR(generateName("r"));
  }

  @Override
  public void onAddSQLToken() {
    AddSubjectTokenModalPresenter presenter = addTokenModalProvider.get();
    presenter.setTokenNames(tokenNames);
    presenter.initSQL(generateName("sql"));
  }

  private String generateName(String prefix) {
    int i = 1;
    String name = prefix + "-" + i;
    while (tokenNames.contains(name)) {
      i++;
      name = prefix + "-" + i;
    }
    return name;
  }

  @Override
  public void onRemoveToken(final SubjectTokenDto token) {
    confirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(UriBuilders.CURRENT_SUBJECT_TOKEN.create().build(token.getName()))
            .withCallback(Response.SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                refreshTokens();
              }
            })
            .delete().send();
      }
    };
    String title = translations.removeTokenModalTitle();
    String message = translationMessages.confirmRemoveToken(token.getName());
    fireEvent(ConfirmationRequiredEvent.createWithMessages(confirmation, title, message));
  }

  @Override
  public void onRenewToken(SubjectTokenDto token) {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.CURRENT_SUBJECT_TOKEN_RENEW.create().build(token.getName()))
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshTokens();
          }
        })
        .put().send();
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

    String RENEW_ACTION = "Renew";

    void enableChangePassword(boolean enabled, String realm, String accountUrl);

    void showOtpSwitch(boolean visible);

    void setOtpSwitchState(boolean otpEnabled);

    void showQrCode(String imageUri);

    void renderTokens(List<SubjectTokenDto> tokens);

    void renderGroups(List<String> groups);

  }
}
