/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.apps;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

public class AppsTokenModalView extends ModalPopupViewWithUiHandlers<AppsTokenModalUiHandlers> implements AppsTokenModalPresenter.Display {

  interface Binder extends UiBinder<Widget, AppsTokenModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  ControlGroup tokenGroup;

  @UiField
  Button cancelButton;

  @UiField
  TextBox tokenText;

  @Inject
  public AppsTokenModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.updateTokenModalTitle());
  }

  @UiHandler("generateTokenButton")
  public void onGenerateToken(ClickEvent event) {
    tokenText.setText(getUiHandlers().onGenerateToken());
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().onUpdateToken(tokenText.getText());
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void showError(String message) {
    dialog.closeAlerts();
    dialog.addAlert(message, AlertType.ERROR, tokenGroup);
  }

  @Override
  public void setToken(String token) {
    tokenText.setText(token);
  }
}
