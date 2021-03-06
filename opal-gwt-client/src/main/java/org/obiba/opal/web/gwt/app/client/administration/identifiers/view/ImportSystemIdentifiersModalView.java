/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.identifiers.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.ImportSystemIdentifiersModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.ImportSystemIdentifiersModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ImportSystemIdentifiersModalView extends ModalPopupViewWithUiHandlers<ImportSystemIdentifiersModalUiHandlers>
    implements ImportSystemIdentifiersModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ImportSystemIdentifiersModalView> {}

  private final Translations translations;

  @UiField
  Modal dialog;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @UiField
  ControlGroup idsGroup;

  @UiField
  TextArea identifiers;

  @Inject
  public ImportSystemIdentifiersModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.importSystemIdentifiersTitle());
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    getUiHandlers().onSubmit(getIdentifiers().getText());
  }

  @Override
  public void showError(String message, @Nullable FormField group) {
    if(Strings.isNullOrEmpty(message)) return;

    String msg = message;
    try {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(message);
      msg = errorDto.getStatus();
      if(translations.userMessageMap().containsKey(msg)) msg = translations.userMessageMap().get(errorDto.getStatus());
    } catch(Exception ignored) {
    }

    if(group == null) {
      dialog.addAlert(msg, AlertType.ERROR);
    } else dialog.addAlert(msg, AlertType.ERROR, idsGroup);
  }

  @Override
  public void setBusy(boolean busy) {
    if(busy) {
      dialog.setBusy(busy);
      dialog.setCloseVisible(false);
      saveButton.setEnabled(false);
      closeButton.setEnabled(false);
    } else {
      dialog.setBusy(busy);
      dialog.setCloseVisible(true);
      saveButton.setEnabled(true);
      closeButton.setEnabled(true);
    }
  }

  @Override
  public HasText getIdentifiers() {
    return identifiers;
  }

}
