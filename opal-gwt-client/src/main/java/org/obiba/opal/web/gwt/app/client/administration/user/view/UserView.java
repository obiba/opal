/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.user.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserPresenter;
import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.GroupSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.SuggestListBox;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class UserView extends ModalPopupViewWithUiHandlers<UserUiHandlers> implements UserPresenter.Display {

  @UiTemplate("UserView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, UserView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Modal dialog;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField(provided = true)
  SuggestListBox groups;

  @UiField
  ControlGroup usernameGroup;

  @UiField
  TextBox userName;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  PasswordTextBox password;

  @UiField
  PasswordTextBox confirmPassword;

  @Inject
  public UserView(EventBus eventBus) {
    super(eventBus);
    groups = new SuggestListBox(new GroupSuggestOracle(eventBus));

    widget = uiBinder.createAndBindUi(this);
    dialog.setTitle(translations.addUserLabel());

    groups.getSuggestBox().getValueBox().addKeyUpHandler(new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        // Keycode for comma
        if(event.getNativeEvent().getKeyCode() == 188) {
          addSearchItem(groups.getSuggestBox().getText().replace(",", "").trim());
          groups.getSuggestBox().setText("");
        }
      }
    });
  }

  @Override
  public void setUser(String originalUserName, List<String> originalGroups) {
    userName.setText(originalUserName);
  }

  @Override
  public void usernameSetEnabled(boolean b) {
    userName.setEnabled(b);
    dialog.setTitle(b ? translations.addUserLabel() : translations.editUserLabel());
  }

  @Override
  public String getUserName() {
    return userName.getText();
  }

  @Override
  public String getPassword() {
    return password.getText();
  }

  @Override
  public String getConfirmPassword() {
    return confirmPassword.getText();
  }

  @Override
  public JsArrayString getGroups() {
    JsArrayString g = JsArrayString.createArray().cast();
    for(String s : groups.getSelectedItemsTexts()) {
      g.push(s);
    }

    // add the group from the textbox (if the user has not entered ',')
    if(!groups.getSuggestBox().getText().isEmpty()) {
      g.push(groups.getSuggestBox().getText());
    }

    return g;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void addSearchItem(String text) {
    String qText = text;
    groups.addItem(qText);
  }

  @Override
  public HandlerRegistration addSearchSelectionHandler(final SelectionHandler<SuggestOracle.Suggestion> handler) {
    return groups.getSuggestBox().addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
      @Override
      public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
        addSearchItem(((GroupSuggestOracle.GroupSuggestion) event.getSelectedItem()).getGroup());
        groups.getSuggestBox().setText("");
        handler.onSelection(event);
      }
    });
  }

  @Override
  public void setNameError(String message) {
    clearNameAlert();
    usernameGroup.setType(ControlGroupType.ERROR);
    dialog.addAlert(message, AlertType.ERROR, new ClosedHandler() {
      @Override
      public void onClosed(ClosedEvent closedEvent) {
        clearNameAlert();
      }
    });
  }

  @Override
  public void setPasswordError(String message) {
    clearPasswordAlert();
    passwordGroup.setType(ControlGroupType.ERROR);
    dialog.addAlert(message, AlertType.ERROR, new ClosedHandler() {
      @Override
      public void onClosed(ClosedEvent closedEvent) {
        clearPasswordAlert();
      }
    });
  }

  @Override
  public void setError(String message) {
    dialog.addAlert(message, AlertType.ERROR);
  }

  private void clearNameAlert() {
    usernameGroup.setType(ControlGroupType.NONE);
    dialog.clearAlert();
  }

  private void clearPasswordAlert() {
    passwordGroup.setType(ControlGroupType.NONE);
    dialog.clearAlert();
  }
}
