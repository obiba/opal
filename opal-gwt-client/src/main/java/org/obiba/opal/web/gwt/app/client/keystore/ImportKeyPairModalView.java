/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.keystore.support.KeystoreType;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.keystore.ImportKeyPairModalPresenter.Display;
import static org.obiba.opal.web.gwt.app.client.keystore.ImportKeyPairModalPresenter.ImportType;

public class ImportKeyPairModalView extends ModalPopupViewWithUiHandlers<KeyPairModalUiHandlers>
    implements Display {

  interface Binder extends UiBinder<Widget, ImportKeyPairModalView> {}

  @UiField
  FlowPanel privateKeyPanel;

  @UiField
  ControlGroup privateKeyGroup;

  @UiField
  TextArea privateKey;

  @UiField
  ControlGroup publicKeyGroup;

  @UiField
  TextArea publicKey;

  @UiField
  Modal modal;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox name;

  private final Translations translations;


  @Inject
  public ImportKeyPairModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public HasText getPublicKey() {
    return publicKey;
  }

  @Override
  public HasText getPrivateKey() {
    return privateKey;
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public void setType(KeystoreType kType, ImportType type) {
    nameGroup.setVisible(kType == KeystoreType.PROJECT);

    switch(type) {
      case KEY_PAIR:
        modal.setTitle(translations.importKeyPairTitle());
        privateKeyPanel.setVisible(true);
        break;
      case CERTIFICATE:
        modal.setTitle(translations.importCertificateTitle());
        privateKeyPanel.setVisible(false);
        break;
    }
  }

  @Override
  public void close() {
    modal.hide();
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  @UiHandler("cancelButton")
  public void onCancelButtonClicked(ClickEvent event) {
    close();
  }

  @UiHandler("saveButton")
  public void onSaveButtonClicked(ClickEvent event) {
    getUiHandlers().save();
  }

  @Override
  public void showError(@Nullable Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = nameGroup;
          break;
        case PUBLIC_KEY:
          group = publicKeyGroup;
          break;
        case PRIVATE_KEY:
          group = privateKeyGroup;
          break;
      }
    }

    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }
}
