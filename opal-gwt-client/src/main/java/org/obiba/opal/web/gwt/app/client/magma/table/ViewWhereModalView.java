/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.table;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.ace.client.AceEditor;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ViewWhereModalView extends ModalPopupViewWithUiHandlers<ViewWhereModalUiHandlers>
    implements ViewWhereModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ViewWhereModalView> {}

  @UiField
  Modal dialog;

  @UiField
  ControlGroup whereGroup;

  @UiField
  AceEditor scriptArea;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @Inject
  public ViewWhereModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.editEntitiesFilter());
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave(scriptArea.getText());
  }

  @Override
  public void renderProperties(ViewDto view) {
    scriptArea.setText(view.getWhere());
  }

  @Override
  public void showError(String message, @Nullable FormField group) {
    if(Strings.isNullOrEmpty(message)) return;

    dialog.closeAlerts();
    String msg = message;
    try {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(message);
      msg = errorDto.getStatus();
    } catch(Exception ignored) {
    }

    if(group == null) {
      dialog.addAlert(msg, AlertType.ERROR);
    } else dialog.addAlert(msg, AlertType.ERROR, whereGroup);
  }

}
