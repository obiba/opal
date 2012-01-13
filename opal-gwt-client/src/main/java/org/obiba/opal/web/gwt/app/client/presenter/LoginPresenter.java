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

import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationCache;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealRootLayoutContentEvent;

public class LoginPresenter extends Presenter<LoginPresenter.Display, LoginPresenter.Proxy> {

  public interface Display extends View {

    public void focusOnUserName();

    public void clear();

    public void showErrorMessageAndClearPassword();

    public HasValue<String> getUserName();

    public HasValue<String> getPassword();

    public HasClickHandlers getSignIn();

    public HasKeyUpHandlers getUserNameTextBox();

    public HasKeyUpHandlers getPasswordTextBox();

  }

  @ProxyStandard
  @NameToken("login")
  @NoGatekeeper
  public interface Proxy extends ProxyPlace<LoginPresenter> {
  }

  private final RequestCredentials credentials;

  private final ResourceAuthorizationCache authorizationCache;

  @Inject
  public LoginPresenter(Display display, EventBus eventBus, Proxy proxy, RequestCredentials credentials, ResourceAuthorizationCache authorizationCache) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.authorizationCache = authorizationCache;
  }

  @Override
  protected void onBind() {
    getView().getSignIn().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        createSecurityResource();
      }
    });
    getView().getUserNameTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          createSecurityResource();
        }
      }
    });
    getView().getPasswordTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          createSecurityResource();
        }
      }
    });
  }

  private void createSecurityResource() {
    createSecurityResource(getView().getUserName().getValue(), getView().getPassword().getValue());
  }

  @Override
  protected void revealInParent() {
    RevealRootLayoutContentEvent.fire(this, this);
  }

  @Override
  public void onReset() {
    // TODO: Temporarily commenting out the "focus on user name" behaviour.
    // This seems to lead to layout issues!
    // getView().focusOnUserName();
    authorizationCache.clear();
  }

  private void createSecurityResource(final String username, String password) {
    ResponseCodeCallback authError = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getView().showErrorMessageAndClearPassword();
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/auth/sessions").post().withCallback(403, authError).withCallback(401, authError).withCallback(201, new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        // When a 201 happens, we should have credentials, but we'll test anyway.
        if(credentials.hasCredentials()) {
          getView().clear();
          credentials.setUsername(username);
          getEventBus().fireEvent(new SessionCreatedEvent(response.getHeader("Location")));
        } else {
          getView().showErrorMessageAndClearPassword();
        }
      }
    }).withFormBody("username", username, "password", password).send();
  }

}
