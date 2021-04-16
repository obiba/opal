/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.opal.AuthProviderDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

public class LoginPresenter extends Presenter<LoginPresenter.Display, LoginPresenter.Proxy> {

  public interface Display extends View {

    void focusOnUserName();

    void clear();

    void showErrorMessageAndClearPassword();

    void showErrorMessageAndClearPassword(String message);

    HasValue<String> getUserName();

    HasValue<String> getPassword();

    HasClickHandlers getSignIn();

    HasKeyUpHandlers getUserNameTextBox();

    HasKeyUpHandlers getPasswordTextBox();

    void setApplicationName(String text);

    void setBusy(boolean value);

    void renderAuthProviders(JsArray<AuthProviderDto> providers);
  }

  @ProxyStandard
  @NameToken(Places.LOGIN)
  @NoGatekeeper
  public interface Proxy extends ProxyPlace<LoginPresenter> {
  }

  private final RequestCredentials credentials;

  private final ResourceAuthorizationCache authorizationCache;

  private final Translations translations;

  private HandlerRegistration unhandledExceptionHandler;

  @Inject
  public LoginPresenter(Display display, EventBus eventBus, Proxy proxy, RequestCredentials credentials,
                        ResourceAuthorizationCache authorizationCache, Translations translations) {
    super(eventBus, display, proxy, RevealType.Root);
    this.credentials = credentials;
    this.authorizationCache = authorizationCache;
    this.translations = translations;
  }

  @Override
  protected void onBind() {
    getView().getSignIn().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        createSecurityResource();
      }
    });
    getView().getUserNameTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          createSecurityResource();
        }
      }
    });
    getView().getPasswordTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          createSecurityResource();
        }
      }
    });

    addRegisteredHandler(GeneralConfigSavedEvent.getType(), new GeneralConfigSavedEvent.GeneralConfigSavedHandler() {
      @Override
      public void onGeneralConfigSaved(GeneralConfigSavedEvent event) {
        refreshApplicationName();
      }
    });
  }

  @Override
  protected void onHide() {
    super.onHide();
    getView().setBusy(false);
  }

  @Override
  protected void onReveal() {
    refreshApplicationName();
    refreshAuthProviders();
  }

  private void refreshApplicationName() {
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(UriBuilders.SYSTEM_NAME.create().build()) //
        .accept("text/plain")
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setApplicationName(response.getText());
          }
        }, Response.SC_OK).send();
  }

  private void refreshAuthProviders() {
    // Fetch all auth providers
    ResourceRequestBuilderFactory.<JsArray<AuthProviderDto>>newBuilder() //
        .forResource(UriBuilders.AUTH_PROVIDERS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<AuthProviderDto>>() {
          @Override
          public void onResource(Response response, JsArray<AuthProviderDto> resource) {
            JsArray<AuthProviderDto> providers = JsArrays.toSafeArray(resource);
            getView().renderAuthProviders(providers);
          }
        }).get().send();
  }

  private void createSecurityResource() {
    createSecurityResource(getView().getUserName().getValue(), getView().getPassword().getValue());
  }

  @Override
  public void onReset() {
    // TODO: Temporarily commenting out the "focus on user name" behaviour.
    // This seems to lead to layout issues!
    getView().focusOnUserName();
    getView().clear();
    authorizationCache.clear();
  }

  private void createSecurityResource(final String username, String password) {
    unhandledExceptionHandler = addHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {
      @Override
      public void onUnhandledResponse(UnhandledResponseEvent e) {
        unhandledExceptionHandler.removeHandler();
        getView().setBusy(false);
      }
    });

    getView().setBusy(true);
    ResponseCodeCallback authError = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getView().setBusy(false);
        try {
          ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
          String msg = errorDto.getStatus();
          if (translations.userMessageMap().containsKey(msg) || "BannedUser".equals(msg)) {
            String status = errorDto.getStatus();
            JsArrayString args = errorDto.getArgumentsArray();
            if ("BannedUser".equals(status)) {
              int remainingBanTime = Integer.parseInt(args.get(1));
              String arg = remainingBanTime + "";
              status = "BannedUserSecs";
              if (remainingBanTime > 60) {
                arg = (remainingBanTime / 60) + "";
                status = "BannedUserMin" + ("1".equals(arg) ? "" : "s");
              }
              args = JsArrays.from(arg);
            }
            getView().showErrorMessageAndClearPassword(TranslationsUtils.replaceArguments(translations.userMessageMap().get(status), args));
            return;
          }
        } catch (Exception ignored) {
          GWT.log(ignored.getMessage());
        }
        getView().showErrorMessageAndClearPassword();
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/auth/sessions").post()
        .withCallback(Response.SC_FORBIDDEN, authError).withCallback(Response.SC_UNAUTHORIZED, authError)
        .withCallback(Response.SC_CREATED, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            // When a 201 happens, we should have credentials, but we'll test anyway.
            if (credentials.hasCredentials()) {
              getView().clear();
              credentials.setUsername(username);
              fireEvent(new SessionCreatedEvent(response.getHeader("Location")));
            } else {
              getView().setBusy(false);
              getView().showErrorMessageAndClearPassword();
            }
          }
        }).withFormBody("username", username, "password", password).send();
  }
}
