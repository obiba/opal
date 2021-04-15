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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
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

public class TablePropertiesModalView extends ModalPopupViewWithUiHandlers<TablePropertiesModalUiHandlers>
    implements TablePropertiesModalPresenter.Display {

  interface Binder extends UiBinder<Widget, TablePropertiesModalView> {}

  private final Translations translations;

  @UiField
  Modal dialog;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox name;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @UiField
  ControlGroup entityGroup;

  @UiField
  TextBox entityType;

  @Inject
  public TablePropertiesModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.addTableTitle());
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave(getName().getText(), getEntityType().getText());
  }

  @Override
  public void renderProperties(TableDto table) {
    name.setText(table.getName());
    entityType.setText(table.getEntityType());
    entityType.setEnabled(false);
    dialog.setTitle(translations.editProperties());
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
    } else if(group.equals(FormField.NAME)) dialog.addAlert(msg, AlertType.ERROR, nameGroup);
    else dialog.addAlert(msg, AlertType.ERROR, entityGroup);
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasText getEntityType() {
    return entityType;
  }

}
