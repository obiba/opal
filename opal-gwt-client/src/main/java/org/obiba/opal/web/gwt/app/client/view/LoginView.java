package org.obiba.opal.web.gwt.app.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginView extends Composite {
  @UiTemplate("LoginView.ui.xml")
  interface LoginViewUiBinder extends UiBinder<Widget, LoginView> {
  }

  private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);

  @UiField
  Label applicationName;

  @UiField
  Label errorMessage;

  @UiField
  TextBox userName;

  @UiField
  PasswordTextBox password;

  @UiField
  Button login;

  public LoginView() {
    initWidget(uiBinder.createAndBindUi(this));
    errorMessage.setVisible(false);
  }

  @UiHandler("login")
  void handleClick(ClickEvent e) {
    Window.alert("Login not implement. Click outside Sign In popup to start.");
  }
}
