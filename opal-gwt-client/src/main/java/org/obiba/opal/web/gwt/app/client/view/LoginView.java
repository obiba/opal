package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginView extends PopupPanel implements LoginPresenter.Display {
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
    // Clicking outside of the popupPanel will dismiss the panel, panel is modal
    super(false, true);
    add(uiBinder.createAndBindUi(this));
    errorMessage.setVisible(false);
    setGlassEnabled(true);
    userName.setFocus(true);
  }

  @Override
  public HasClickHandlers getSignIn() {
    return login;
  }

  @Override
  public Widget asWidget() {
    return null;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
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
  public void showPopup() {
    center();
    userName.setFocus(true);
    show();
  }

  @Override
  public void showPopupWithGlassPanel() {
    center();
    show();
  }

  @Override
  public void showErrorMessage() {
    errorMessage.setVisible(true);
    new Animation() {

      @Override
      protected void onUpdate(double progress) {
        errorMessage.getElement().setAttribute("style", "opacity:" + progress);
      }

    }.run(200);
  }

  @Override
  public void hidePopup() {
    errorMessage.setVisible(false);
    getPassword().setValue("");
    hide();
  }

  @Override
  public HasKeyUpHandlers getPasswordTextBox() {
    return password;
  }

  @Override
  public HasKeyUpHandlers getUserNameTextBox() {
    return userName;
  }

}
