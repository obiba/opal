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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserPresenter;
import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.GroupSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.SuggestListBox;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class UserView extends ModalPopupViewWithUiHandlers<UserUiHandlers> implements UserPresenter.Display {

  public static final int COMMA_KEY = 188;

  @UiTemplate("UserView.ui.xml")
  interface Binder extends UiBinder<Widget, UserView> {}

  @UiField
  Modal modal;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField(provided = true)
  SuggestListBox groups;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox name;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  PasswordTextBox password;

  @UiField
  PasswordTextBox confirmPassword;

  private final Translations translations;

  @Inject
  public UserView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;

    groups = new SuggestListBox(new GroupSuggestOracle(eventBus));

    initWidget(uiBinder.createAndBindUi(this));

    groups.getSuggestBox().addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
      @Override
      public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
        addGroup(((GroupSuggestOracle.GroupSuggestion) event.getSelectedItem()).getGroup());
        groups.getSuggestBox().setText("");
      }
    });

    modal.setTitle(translations.addUserLabel());

    groups.getSuggestBox().getValueBox().addKeyUpHandler(new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        // Keycode for comma
        if(event.getNativeEvent().getKeyCode() == COMMA_KEY) {
          addGroup(groups.getSuggestBox().getText().replace(",", "").trim());
          groups.getSuggestBox().setText("");
        }
      }
    });

    // used to support ConstraintViolation exceptions
    ConstrainedModal constrainedModal = new ConstrainedModal(modal);
    constrainedModal.registerWidget("name", translations.nameLabel(), nameGroup);
    constrainedModal.registerWidget("password", translations.urlLabel(), passwordGroup);

  }

  //
  @Override
  public void setNamedEnabled(boolean enabled) {
    name.setEnabled(enabled);
    modal.setTitle(enabled ? translations.addUserLabel() : translations.editUserLabel());
  }

  @Override
  public HasText getName() {
    return name;
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
  public TakesValue<List<String>> getGroups() {
    return new TakesValue<List<String>>() {
      @Override
      public void setValue(List<String> value) {
        if(value != null) {
          for(String group : value) {
            addGroup(group);
          }
        }
      }

      @Override
      public List<String> getValue() {
        List<String> selected = new ArrayList<String>();
        for(String group : groups.getSelectedItemsTexts()) {
          selected.add(group);
        }

        // add the group from the textbox (if the user has not entered ',')
        if(!groups.getSuggestBox().getText().isEmpty()) {
          selected.add(groups.getSuggestBox().getText());
        }

        return selected;
      }
    };

  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  private void addGroup(String group) {
    groups.addItem(group);
  }

  @Override
  public void showError(@Nullable UserPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case USERNAME:
          group = nameGroup;
          break;
        case PASSWORD:
          group = passwordGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @Override
  public void clearErrors() {
    modal.clearAlert();
  }
}
