/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.presenter.EncryptDownloadModalPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.EncryptDownloadModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class EncryptDownloadModalView extends ModalPopupViewWithUiHandlers<EncryptDownloadModalUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, EncryptDownloadModalView> {}

  @UiField
  Modal dialog;

  @UiField
  Button cancelButton;

  @UiField
  PasswordTextBox password;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  Button viewPasswordButton;

  @UiField
  Button generatePasswordButton;

  @UiField
  CheckBox encrypt;

  @Inject
  public EncryptDownloadModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.downloadFileModalTitle());
    password.setStyleName("password-vertical-align");
  }

  @Override
  public void onShow() {
    password.setFocus(true);
    password.setText("");
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("encrypt")
  public void onEncrypt(ClickEvent event) {
    boolean shouldEncrypt = shouldEncrypt();
    password.setEnabled(shouldEncrypt);
    viewPasswordButton.setEnabled(shouldEncrypt);
    generatePasswordButton.setEnabled(shouldEncrypt);
    if (!shouldEncrypt) {
      password.setText("");
    }
  }

  @UiHandler("download")
  public void onDownload(ClickEvent event) {
    getUiHandlers().onDownload();
  }

  @UiHandler("viewPasswordButton")
  public void onViewPassword(ClickEvent event) {
    password.getElement().setAttribute(
        "type",
        password.getElement().getAttribute("type").equalsIgnoreCase("text") ? "password" : "text"
    );
  }

  @UiHandler("generatePasswordButton")
  public void onGeneratePassword(ClickEvent event) {
    password.setText(getUiHandlers().onGeneratePassword());
  }

  @Override
  public boolean shouldEncrypt() {
    return encrypt.getValue();
  }

  @Override
  public HasText getPassword() {
    return password;
  }

  @Override
  public void clearErrors() {
    dialog.clearAlert();
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case PASSWORD:
          group = passwordGroup;
          break;
      }
    }
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }
}
