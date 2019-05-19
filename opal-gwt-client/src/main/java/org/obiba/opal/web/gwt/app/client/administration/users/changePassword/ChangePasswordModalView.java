/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.changePassword;

import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.administration.users.changePassword.ChangePasswordModalPresenter.Display;

public class ChangePasswordModalView extends ModalPopupViewWithUiHandlers<ChangePasswordModalUiHandlers>
    implements Display {

  interface Binder extends UiBinder<Widget, ChangePasswordModalView> {}

  @UiField
  TextBox confirmPassword;

  @UiField
  TextBox newPassword;

  @UiField
  TextBox oldPassword;

  @UiField
  ControlGroup oldPasswordGroup;

  @UiField
  Modal modal;

  @UiField
  ControlGroup newPasswordGroup;

  private final Translations translations;

  @Inject
  public ChangePasswordModalView(EventBus eventBus, Binder uiBinder, Translations trans) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    translations = trans;
    modal.setTitle(translations.changePasswordModalTitle());
  }

  @Override
  public void close() {
    modal.hide();
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  @Override
  public void showError(String messageKey, List<String> args) {
    modal.addAlert(TranslationsUtils.replaceArguments(translations.userMessageMap().get(messageKey), args),
        AlertType.ERROR);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case OLD_PASSWORD:
          group = oldPasswordGroup;
          break;
        case NEW_PASSWORD:
          group = newPasswordGroup;
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
  public HasText getOldPassword() {
    return oldPassword;
  }

  @Override
  public HasText getNewPassword() {
    return newPassword;
  }

  @Override
  public HasText getConfirmPassword() {
    return confirmPassword;
  }

  @UiHandler("cancelButton")
  public void onCancelButtonClicked(ClickEvent event) {
    close();
  }

  @UiHandler("saveButton")
  public void onSaveButtonClicked(ClickEvent event) {
    getUiHandlers().save();
  }
}
