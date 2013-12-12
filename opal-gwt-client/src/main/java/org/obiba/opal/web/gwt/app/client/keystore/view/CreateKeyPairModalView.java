/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.CreateKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.CreateKeyPairModalUiHandlers;
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

public class CreateKeyPairModalView extends ModalPopupViewWithUiHandlers<CreateKeyPairModalUiHandlers>
implements CreateKeyPairModalPresenter.Display {

  @UiField
  Modal modal;

  @UiField
  ControlGroup algorithmGroup;

  @UiField
  TextBox algorithm;

  @UiField
  ControlGroup sizeGroup;

  @UiField
  TextBox size;

  @UiField
  ControlGroup firstLastNameGroup;

  @UiField
  TextBox firstLastName;

  @UiField
  ControlGroup organizationalUnitGroup;

  @UiField
  TextBox organizationalUnit;

  interface Binder extends UiBinder<Widget, CreateKeyPairModalView> {}

  @Inject
  public CreateKeyPairModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.addResourcePermissionsModalTile());
  }

  @Override
  public void showError(@Nullable CreateKeyPairModalPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case ALGORITHM:
          group = algorithmGroup;
          break;
        case SIZE:
          group = sizeGroup;
          break;
        case FIRST_LAST_NAME:
          group = firstLastNameGroup;
          break;
        case ORGANIZATIONAL_UNIT:
          group = organizationalUnitGroup;
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
  public HasText getAlgorithm() {
    return algorithm;
  }

  @Override
  public HasText getSize() {
    return size;
  }

  @Override
  public HasText getFirstLastName() {
    return firstLastName;
  }

  @Override
  public HasText getOrganizationalUnit() {
    return organizationalUnit;
  }

  @Override
  public void close() {
    modal.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButtonClicked(ClickEvent event) {
    close();
  }

  @UiHandler("saveButton")
  public void onCreateButtonClicked(ClickEvent event) {
    getUiHandlers().save();
  }

}

