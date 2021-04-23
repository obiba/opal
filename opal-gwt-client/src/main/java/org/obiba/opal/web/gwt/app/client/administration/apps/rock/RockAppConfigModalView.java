/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.apps.rock;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.RockAppConfigDto;

import javax.annotation.Nullable;

public class RockAppConfigModalView extends ModalPopupViewWithUiHandlers<RockAppConfigModalUiHandlers> implements RockAppConfigModalPresenter.Display {

  interface Binder extends UiBinder<Widget, RockAppConfigModalView> {
  }

  private static final String CREDENTIALS_DEFAULT = "default";
  private static final String CREDENTIALS_ADMINISTRATOR = "administrator";
  private static final String CREDENTIALS_MANAGER_USER = "manager_user";

  private final Translations translations;
      
  @UiField
  Modal dialog;

  @UiField
  ControlGroup urlGroup;

  @UiField
  Chooser credentialsType;

  @UiField
  HelpBlock credentialsHelp;

  @UiField
  TextBox urlText;

  @UiField
  Panel administratorGroup;

  @UiField
  ControlGroup administratorUsernameGroup;

  @UiField
  TextBox administratorUsername;

  @UiField
  ControlGroup administratorPasswordGroup;

  @UiField
  PasswordTextBox administratorPassword;

  @UiField
  Panel managerUserGroup;

  @UiField
  ControlGroup managerUsernameGroup;

  @UiField
  TextBox managerUsername;

  @UiField
  ControlGroup managerPasswordGroup;

  @UiField
  PasswordTextBox managerPassword;

  @UiField
  ControlGroup userUsernameGroup;

  @UiField
  TextBox userUsername;

  @UiField
  ControlGroup userPasswordGroup;

  @UiField
  PasswordTextBox userPassword;

  @Inject
  protected RockAppConfigModalView(EventBus eventBus, Binder binder, final Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.addServiceDiscoveryTitle());
    credentialsType.addItem("Default", CREDENTIALS_DEFAULT);
    credentialsType.addItem("Administrator", CREDENTIALS_ADMINISTRATOR);
    credentialsType.addItem("Manager and User", CREDENTIALS_MANAGER_USER);
    credentialsType.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent chosenChangeEvent) {
        String selected = credentialsType.getSelectedValue();
        showCredentialsHelp(selected);
        administratorGroup.setVisible(CREDENTIALS_ADMINISTRATOR.equals(selected));
        managerUserGroup.setVisible(CREDENTIALS_MANAGER_USER.equals(selected));
      }

    });
    administratorGroup.setVisible(false);
    managerUserGroup.setVisible(false);
    this.translations = translations;
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    dialog.clearAlert();
    String selected = credentialsType.getSelectedValue();
    if (CREDENTIALS_DEFAULT.equals(selected))
      getUiHandlers().onSave(urlText.getText().trim());
    else if (CREDENTIALS_ADMINISTRATOR.equals(selected))
      getUiHandlers().onSave(urlText.getText().trim(), administratorUsername.getText(), administratorPassword.getText());
    else if (CREDENTIALS_MANAGER_USER.equals(selected))
      getUiHandlers().onSave(urlText.getText().trim(), managerUsername.getText(), managerPassword.getText(), userUsername.getText(), userPassword.getText());
  }

  @Override
  public void renderConfig(RockAppConfigDto rockConfig) {
    urlText.setText(rockConfig.getHost());
    urlText.setEnabled(false);
    administratorGroup.setVisible(rockConfig.hasAdministratorCredentials());
    managerUserGroup.setVisible(rockConfig.hasManagerCredentials() || rockConfig.hasUserCredentials());
    if (rockConfig.hasAdministratorCredentials()) {
      credentialsType.setSelectedValue(CREDENTIALS_ADMINISTRATOR);
      showCredentialsHelp(CREDENTIALS_ADMINISTRATOR);
      administratorUsername.setText(rockConfig.getAdministratorCredentials().getName());
      administratorPassword.setText(rockConfig.getAdministratorCredentials().getPassword());
    } else if (rockConfig.hasManagerCredentials() || rockConfig.hasUserCredentials()) {
      credentialsType.setSelectedValue(CREDENTIALS_MANAGER_USER);
      showCredentialsHelp(CREDENTIALS_MANAGER_USER);
      if (rockConfig.hasManagerCredentials()) {
        managerUsername.setText(rockConfig.getManagerCredentials().getName());
        managerPassword.setText(rockConfig.getManagerCredentials().getPassword());
      }
      if (rockConfig.hasUserCredentials()) {
        userUsername.setText(rockConfig.getUserCredentials().getName());
        userPassword.setText(rockConfig.getUserCredentials().getPassword());
      }
    } else {
      credentialsType.setSelectedValue(CREDENTIALS_DEFAULT);
      showCredentialsHelp(CREDENTIALS_DEFAULT);
    }
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if (formField != null) {
      switch (formField) {
        case HOST:
          group = urlGroup;
          break;
        case ADMINISTRATOR_NAME:
          group = administratorUsernameGroup;
          break;
        case ADMINISTRATOR_PASSWORD:
          group = administratorPasswordGroup;
          break;
        case MANAGER_NAME:
          group = managerUsernameGroup;
          break;
        case MANAGER_PASSWORD:
          group = managerPasswordGroup;
          break;
        case USER_NAME:
          group = userUsernameGroup;
          break;
        case USER_PASSWORD:
          group = userPasswordGroup;
          break;
      }
    }
    if (group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }

  private void showCredentialsHelp(String selected) {
    switch (selected) {
      case CREDENTIALS_DEFAULT:
        credentialsHelp.setHTML(translations.rockDefaultCredentialsHelp());
        break;
      case CREDENTIALS_ADMINISTRATOR:
        credentialsHelp.setHTML(translations.rockAdministratorCredentialsHelp());
        break;
      case CREDENTIALS_MANAGER_USER:
        credentialsHelp.setHTML(translations.rockManagerUserCredentialsHelp());
        break;
    }
  }
}
