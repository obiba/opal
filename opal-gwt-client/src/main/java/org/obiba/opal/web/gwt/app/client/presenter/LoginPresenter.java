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

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
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
import org.obiba.opal.web.model.client.opal.AuthProviderDto;
import org.obiba.opal.web.model.client.opal.Subject;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

;

public class LoginPresenter extends Presenter<LoginPresenter.Display, LoginPresenter.Proxy>
    implements LoginUiHandlers {

  public interface Display extends View, HasUiHandlers<LoginUiHandlers> {

    void focusOnUserName();

    void clear();

    void showTotp(String otpHeader);

    void showErrorMessageAndClearPassword();

    void showErrorMessageAndClearPassword(String message);

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

  @Inject
  public LoginPresenter(Display display, EventBus eventBus, Proxy proxy, RequestCredentials credentials,
                        ResourceAuthorizationCache authorizationCache, Translations translations) {
    super(eventBus, display, proxy, RevealType.Root);
    this.credentials = credentials;
    this.authorizationCache = authorizationCache;
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
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

  @Override
  public void onSignIn(String username, String password) {
    getView().setBusy(true);

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.AUTH_SESSIONS.create().build())
        .withFormBody("username", username, "password", password)
        .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            showLoginError(response);
          }
        })
        .withCallback(Response.SC_UNAUTHORIZED, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            String authHeader = response.getHeader("WWW-Authenticate");
            if (!Strings.isNullOrEmpty(authHeader)) {
              getView().setBusy(false);
              getView().showTotp(authHeader);
            } else
              showLoginError(response);
          }
        })
        .withCallback(Response.SC_CREATED, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            String location = response.getHeader("Location");
            initUsername(location);
          }
        })
        .post().send();
  }

  @Override
  public void onSignIn(String username, String password, String code, String otpHeader) {
    getView().setBusy(true);

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.AUTH_SESSIONS.create().build())
        .header(otpHeader, code)
        .withFormBody("username", username, "password", password)
        .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            showLoginError(response);
          }
        })
        .withCallback(Response.SC_UNAUTHORIZED, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            showLoginError(response);
          }
        })
        .withCallback(Response.SC_CREATED, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            String location = response.getHeader("Location");
            initUsername(location);
          }
        })
        .post().send();
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

  @Override
  public void onReset() {
    // TODO: Temporarily commenting out the "focus on user name" behaviour.
    // This seems to lead to layout issues!
    getView().focusOnUserName();
    getView().clear();
    authorizationCache.clear();
  }

  private void showLoginError(Response response) {
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

  private void initUsername(final String location) {
    // try to get the username
    ResourceRequestBuilderFactory.<Subject>newBuilder()
        .forResource(UriBuilders.AUTH_SESSION_CURRENT_USERNAME.create().build())
        .withCallback(new ResourceCallback<Subject>() {
          @Override
          public void onResource(Response response, Subject resource) {
            if (response.getStatusCode() == Response.SC_OK) {
              credentials.setUsername(resource.getPrincipal());
              fireEvent(new SessionCreatedEvent(location));
            } else {
              getView().setBusy(false);
              getView().showErrorMessageAndClearPassword();
            }
          }
        })
        .get().send();
  }
}
