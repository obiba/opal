/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.edit;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.GroupSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.SuggestListBox;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class SubjectCredentialsView extends ModalPopupViewWithUiHandlers<SubjectCredentialsUiHandlers>
    implements SubjectCredentialsPresenter.Display {

  public static final int COMMA_KEY = 188;

  @UiTemplate("SubjectCredentialsView.ui.xml")
  interface Binder extends UiBinder<Widget, SubjectCredentialsView> {}

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

  @UiField
  ControlGroup certificateGroup;

  @UiField
  TextArea certificate;

  @Inject
  public SubjectCredentialsView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);

    groups = new SuggestListBox(new GroupSuggestOracle());

    initWidget(uiBinder.createAndBindUi(this));

    certificate.setPlaceholder(translations.pasteCertificate());

    initGroupSuggestBox();
    initConstrainedModal(translations);
  }

  private void initGroupSuggestBox() {
    groups.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion event) {
        addGroup(event.getDisplayString());
        return "";
      }
    });
    groups.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.getNativeEvent().getKeyCode() == COMMA_KEY) {         // Keycode for comma
          addGroup(groups.getTextBox().getText().replace(",", "").trim());
          groups.getTextBox().setText("");
        }
      }
    });
  }

  /**
   * Used to support ConstraintViolation exceptions
   */
  private void initConstrainedModal(Translations translations) {
    ConstrainedModal constrainedModal = new ConstrainedModal(modal);
    constrainedModal.registerWidget("name", translations.nameLabel(), nameGroup);
    constrainedModal.registerWidget("password", translations.passwordLabel(), passwordGroup);
    constrainedModal.registerWidget("certificate", translations.certificateLabel(), certificateGroup);
  }

  @Override
  public void setNamedEnabled(boolean enabled) {
    name.setEnabled(enabled);
  }

  @Override
  public void setTitle(String title) {
    modal.setTitle(title);
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasVisibility getPasswordGroupVisibility() {
    return passwordGroup;
  }

  @Override
  public HasText getPassword() {
    return password;
  }

  @Override
  public HasText getConfirmPassword() {
    return confirmPassword;
  }

  @Override
  public HasVisibility getCertificateGroupVisibility() {
    return certificateGroup;
  }

  @Override
  public HasText getCertificate() {
    return certificate;
  }

  @Override
  public TakesValue<List<String>> getGroups() {
    return new TakesValue<List<String>>() {
      @Override
      public void setValue(List<String> value) {
        groups.clear();
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
        if(!groups.getTextBox().getText().isEmpty()) {
          selected.add(groups.getTextBox().getText());
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
  public void showError(@Nullable SubjectCredentialsPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = nameGroup;
          break;
        case PASSWORD:
          group = passwordGroup;
          break;
        case CERTIFICATE:
          group = certificateGroup;
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
