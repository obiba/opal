package org.obiba.opal.web.gwt.app.client.view;

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
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.model.client.opal.AuthClientDto;

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
  VerticalPanel authClientsPanel;

  private final Translations translations;

  @Inject
  public LoginView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    userName.setFocus(true);
    password.addKeyPressHandler(new CapsLockTestKeyPressesHandler());
    authClientsPanel.setBorderWidth(5);
    authClientsPanel.setVisible(true);
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
    clear();
    final Alert alert = new Alert(translations.authFailed(), AlertType.ERROR);
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
    public void renderAuthClients(JsArray<AuthClientDto> clients) {

        List<Widget> widgets = new ArrayList<>();
        for (int i=0;i<clients.length(); i++) {
            AuthClientDto client = clients.get(i);
            String url = client.getRedirectUrl();
            if (url == null) {
                continue; //not interested in clients without redirect url
            }

            String key = client.getName();
            String title = translations.authClientsTitleMap().get(key);
            if (title == null) {
                title = key; //fallback
            }
            String icon = translations.authClientsImageMap().get(key);
            Anchor anchor = null;

            if (icon != null) {
                anchor = new Anchor(null, false, url);
                Image img = new Image();
                img.setUrl(icon);
                img.setAltText(title);
                anchor.getElement().appendChild(img.getElement());
            } else {
                anchor = new Anchor(title, false, url);
            }
            widgets.add(anchor);
        }

        if (widgets.size() > 0) {
            for (Widget w: widgets) {
                authClientsPanel.add(w);
            }
        }
        authClientsPanel.setBorderWidth(0);
        authClientsPanel.setVisible(widgets.size() > 0);
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
