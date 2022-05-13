/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.view;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginUiHandlers;
import org.obiba.opal.web.model.client.opal.AuthProviderDto;

import java.util.List;

public class LoginView extends ViewWithUiHandlers<LoginUiHandlers> implements LoginPresenter.Display {

  interface Binder extends UiBinder<Widget, LoginView> {
  }

  @UiField
  Panel alertPanel;

  @UiField
  Well credentialsWell;

  @UiField
  TextBox userName;

  @UiField
  PasswordTextBox password;

  @UiField
  Well otpWell;

  @UiField
  TextBox otp;

  @UiField
  Button login;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  HelpBlock passwordHelp;

  @UiField
  Brand applicationName;

  @UiField
  Image loginProgress;

  @UiField
  Panel authPanel;

  @UiField
  FlowPanel authClientsPanel;

  private String otpHeader;

  private final Translations translations;

  @Inject
  public LoginView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    userName.setFocus(true);
    password.addKeyPressHandler(new CapsLockTestKeyPressesHandler());
    KeyUpHandler enterKeyHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          getUiHandlers().onSignIn(userName.getText(), password.getText());
        }
      }
    };
    userName.addKeyUpHandler(enterKeyHandler);
    password.addKeyUpHandler(enterKeyHandler);
    otp.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          getUiHandlers().onSignIn(userName.getText(), password.getText(), otp.getText(), otpHeader);
        }
      }
    });
  }

  @UiHandler("login")
  public void onSignIn(ClickEvent event) {
    if (Strings.isNullOrEmpty(userName.getText()) || Strings.isNullOrEmpty(password.getText()))
      return;
    getUiHandlers().onSignIn(userName.getText(), password.getText());
  }

  @UiHandler("validate")
  public void onValidate(ClickEvent event) {
    if (Strings.isNullOrEmpty(otp.getText()))
      return;
    getUiHandlers().onSignIn(userName.getText(), password.getText(), otp.getText(), otpHeader);
  }

  @UiHandler("cancel")
  public void onCancel(ClickEvent event) {
    clearPassword();
    otpWell.setVisible(false);
    credentialsWell.setVisible(true);
  }

  @Override
  public void focusOnUserName() {
    userName.setFocus(true);
  }

  @Override
  public void showTotp(String otpHeader) {
    this.otpHeader = otpHeader;
    credentialsWell.setVisible(false);
    otpWell.setVisible(true);
    otp.setFocus(true);
  }

  @Override
  public void showErrorMessageAndClearPassword() {
    showErrorMessageAndClearPassword(translations.authFailed());
  }

  @Override
  public void showErrorMessageAndClearPassword(String message) {
    clear();
    final Alert alert = new Alert(message, AlertType.ERROR);
    alert.addClosedHandler(new ClosedHandler() {
      @Override
      public void onClosed(ClosedEvent closedEvent) {
        alert.removeFromParent();
      }
    });
    alertPanel.add(alert);

    Timer nonStickyTimer = new Timer() {
      @Override
      public void run() {
        alert.close();
      }
    };
    nonStickyTimer.schedule(3000);
  }

  @Override
  public void clear() {
    alertPanel.clear();
    clearPassword();
  }

  @Override
  public void setApplicationName(String text) {
    applicationName.setText(text);
    if (Document.get() != null) {
      Document.get().setTitle(text);
    }
  }

  @Override
  public void setBusy(boolean value) {
    userName.setEnabled(!value);
    password.setEnabled(!value);
    login.setEnabled(!value);
    loginProgress.setVisible(value);
    RootPanel.get().getBodyElement().getStyle().setCursor(value ? Style.Cursor.WAIT : Style.Cursor.DEFAULT);
  }

  @Override
  public void renderAuthProviders(JsArray<AuthProviderDto> providers) {
    authClientsPanel.clear();
    List<Widget> widgets = Lists.newArrayList();
    for (int i = 0; i < providers.length(); i++) {
      AuthProviderDto provider = providers.get(i);

      String key = provider.getName();
      String title = translations.signInWith() + " " + (provider.hasLabel() ? provider.getLabel() : provider.getName());
      Anchor anchor = new Anchor(title, false, "../auth/login/" + key);
      anchor.addStyleName("btn btn-primary");
      if (i > 0) {
        anchor.addStyleName("top-margin");
      }
      widgets.add(anchor);
    }

    if (widgets.size() > 0) {
      for (Widget w : widgets) {
        w.setWidth("100%");
        authClientsPanel.add(w);
      }
    }
    authPanel.setVisible(widgets.size() > 0);
  }

  private void clearPassword() {
    password.setValue("");
    otp.setValue("");
    otpWell.setVisible(false);
    credentialsWell.setVisible(true);
  }

  private final class CapsLockTestKeyPressesHandler implements KeyPressHandler {

    @Override
    public void onKeyPress(KeyPressEvent event) {
      int code = event.getUnicodeCharCode();
      if ((!event.isShiftKeyDown() && (code >= 65 && code <= 90)) ||
          (event.isShiftKeyDown() && (code >= 97 && code <= 122))) {
        passwordGroup.setType(ControlGroupType.WARNING);
        passwordHelp.setVisible(true);
      } else {
        passwordGroup.setType(ControlGroupType.NONE);
        passwordHelp.setVisible(false);
      }
    }
  }

}
