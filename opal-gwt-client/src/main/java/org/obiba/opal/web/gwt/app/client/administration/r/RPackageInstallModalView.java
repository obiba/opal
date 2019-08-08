/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.r;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

public class RPackageInstallModalView extends ModalPopupViewWithUiHandlers<RPackageInstallModalUiHandlers> implements RPackageInstallModalPresenter.Display {

  interface Binder extends UiBinder<Modal, RPackageInstallModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox name;

  @UiField
  TextBox reference;

  @UiField
  Button installButton;
  @UiField
  Button cancelButton;

  private final Translations translations;

  @Inject
  protected RPackageInstallModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void onShow() {
    dialog.setTitle(translations.installRPackage());
    name.setFocus(true);
  }

  @UiHandler("installButton")
  public void onInstall(ClickEvent event) {
    dialog.closeAlerts();
    if (Strings.isNullOrEmpty(name.getText())) {
      dialog.addAlert(translations.userMessageMap().get("NameIsRequired"), AlertType.ERROR, nameGroup);
    } else
      getUiHandlers().installPackage(name.getText(), reference.getText());
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    hideDialog();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setInProgress(boolean progress) {
    dialog.setBusy(progress);
    installButton.setEnabled(!progress);
    cancelButton.setEnabled(!progress);
  }
}
