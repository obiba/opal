/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.view;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.*;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Brand;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.obiba.opal.web.model.client.opal.AuthProviderDto;

import java.util.ArrayList;
import java.util.List;

public class LoginView extends ViewImpl implements LoginPresenter.Display {

  interface Binder extends UiBinder<Widget, LoginView> {}

  @UiField
  Panel alertPanel;

  @UiField
  TextBox userName;

  @UiField
  PasswordTextBox password;

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
  VerticalPanel authClientsPanel;

  private final Translations translations;

  @Inject
  public LoginView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    userName.setFocus(true);
    password.addKeyPressHandler(new CapsLockTestKeyPressesHandler());
  }

  @Override
  public HasClickHandlers getSignIn() {
    return login;
  }

  @Override
  public HasValue<String> getPassword() {
    return password;
  }

  @Override
  public HasValue<String> getUserName() {
    return userName;
  }

  @Override
  public void focusOnUserName() {
    userName.setFocus(true);
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
  public HasKeyUpHandlers getPasswordTextBox() {
    return password;
  }

  @Override
  public HasKeyUpHandlers getUserNameTextBox() {
    return userName;
  }

  @Override
  public void setApplicationName(String text) {
    applicationName.setText(text);
    if (Document.get() != null) {
      Document.get().setTitle (text);
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
    List<Widget> widgets = Lists.newArrayList();
    for (int i=0;i<providers.length(); i++) {
      AuthProviderDto provider = providers.get(i);

      FlowPanel panel = new FlowPanel();
      if (i > 0) {
        panel.addStyleName("top-margin");
      }
      String key = provider.getName();
      String title = translations.signInWith() + " " + (provider.hasLabel() ? provider.getLabel() : provider.getName());
      Anchor anchor = new Anchor(title, false, "../auth/login/" + key);
      anchor.addStyleName("btn btn-inverse");
      panel.add(anchor);
      if (!Strings.isNullOrEmpty(provider.getProviderUrl())) {
        Anchor accountAnchor = new Anchor(translations.userAccountLabel(), false, provider.getProviderUrl());
        accountAnchor.addStyleName("small-indent");
        accountAnchor.setTarget("_blank");
        panel.add(accountAnchor);
      }
      widgets.add(panel);
    }

    if (widgets.size() > 0) {
      for (Widget w: widgets) {
        authClientsPanel.add(w);
      }
    }
    authClientsPanel.setBorderWidth(0);
    authPanel.setVisible(widgets.size() > 0);
  }

  private void clearPassword() {
    getPassword().setValue("");
  }

  private final class CapsLockTestKeyPressesHandler implements KeyPressHandler {

    @Override
    public void onKeyPress(KeyPressEvent event) {
      int code = event.getUnicodeCharCode();
      if((!event.isShiftKeyDown() && (code >= 65 && code <= 90)) ||
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
