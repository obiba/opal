/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
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

public class VariablePropertiesModalView extends ModalPopupViewWithUiHandlers<VariablePropertiesModalUiHandlers>
    implements VariablePropertiesModalPresenter.Display {

  private final Translations translations;

  interface Binder extends UiBinder<Widget, VariablePropertiesModalView> {}

  @UiField
  Modal dialog;

  @UiField
  ControlGroup variableGroup;

  @UiField
  TextBox variableName;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @UiField
  ControlGroup valueTypeGroup;

  @UiField
  Chooser valueType;

  @UiField
  CheckBox repeatable;

  @UiField
  TextBox unit;

  @UiField
  TextBox refEntityType;

  @UiField
  TextBox mimeType;

  @UiField
  TextBox occurenceGroup;

  @UiField
  NumericTextBox index;

  @Inject
  public VariablePropertiesModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.editProperties());
    dialog.setResizable(true);
    index.setMaxConstrained(false);
    index.setMinConstrained(false);
  }

  @UiHandler("repeatable")
  void onRepeatable(ClickEvent event) {
    occurenceGroup.setEnabled(getRepeatable());
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave();
  }

  @Override
  public void renderProperties(VariableDto variable, boolean modifyName, boolean modifyValueType) {
    if(variable != null) {
      variableName.setText(variable.getName());
      valueType.setSelectedValue(variable.getValueType());
      repeatable.setValue(variable.getIsRepeatable());
      unit.setText(variable.getUnit());
      refEntityType.setText(variable.getReferencedEntityType());
      mimeType.setText(variable.getMimeType());
      occurenceGroup.setText(variable.getOccurrenceGroup());
      index.setValue(variable.getIndex());
    } else {
      dialog.setTitle(translations.addVariable());
    }

    variableName.setEnabled(modifyName);
    valueType.setEnabled(modifyValueType);
    repeatable.setEnabled(modifyValueType);
    occurenceGroup.setEnabled(getRepeatable());
  }

  @Override
  public void showError(String message, @Nullable FormField group) {
    if(Strings.isNullOrEmpty(message)) return;

    String msg = message;
    try {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(message);
      msg = errorDto.getStatus();
    } catch(Exception ignored) {
    }
    if(group == null) {
      dialog.addAlert(msg, AlertType.ERROR);
    } else {
      dialog.addAlert(msg, AlertType.ERROR, variableGroup);
    }
  }

  @Override
  public HasText getVariableName() {
    return variableName;
  }

  @Override
  public String getName() {
    return variableName.getText();
  }

  @Override
  public String getValueType() {
    return valueType.getValue();
  }

  @Override
  public boolean getRepeatable() {
    return repeatable.getValue();
  }

  @Override
  public String getUnit() {
    return unit.getText();
  }

  @Override
  public String getReferencedEntityType() {
    return refEntityType.getText();
  }

  @Override
  public String getMimeType() {
    return mimeType.getText();
  }

  @Override
  public String getOccurrenceGroup() {
    return occurenceGroup.getText();
  }

  @Override
  public Long getIndex() {
    return index.getNumberValue();
  }
}
