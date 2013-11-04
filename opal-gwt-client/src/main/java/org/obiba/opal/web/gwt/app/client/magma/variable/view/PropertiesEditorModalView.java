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
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.PropertiesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.PropertiesEditorModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
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

public class PropertiesEditorModalView extends ModalPopupViewWithUiHandlers<PropertiesEditorModalUiHandlers>
    implements PropertiesEditorModalPresenter.Display {

  private static final int MIN_WIDTH = 480;

  private static final int MIN_HEIGHT = 400;

  private final Translations translations;

  interface Binder extends UiBinder<Widget, PropertiesEditorModalView> {}

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

  @Inject
  public PropertiesEditorModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.editProperties());
    dialog.setResizable(true);
    dialog.setMinWidth(MIN_WIDTH);
    dialog.setMinHeight(MIN_HEIGHT);
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
    getUiHandlers().onSave(getName(), getValueType(), getRepeatable(), getUnit(), getMimeType(), getOccurenceGroup(),
        getReferencedEntityType());
  }

  @Override
  public void renderProperties(VariableDto variable, boolean modifyValueType) {
    if(variable != null) {
      variableName.setText(variable.getName());
      valueType.setSelectedValue(variable.getValueType());
      repeatable.setValue(variable.getIsRepeatable());
      unit.setText(variable.getUnit());
      refEntityType.setText(variable.getReferencedEntityType());
      mimeType.setText(variable.getMimeType());
      occurenceGroup.setText(variable.getOccurrenceGroup());
    }

    variableName.setEnabled(modifyValueType);
    valueType.setEnabled(modifyValueType);
    repeatable.setEnabled(modifyValueType);
    occurenceGroup.setEnabled(getRepeatable());
  }

  @Override
  public void showError(String message, @Nullable FormField group) {
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, variableGroup);
    }
  }

  @Override
  public HasText getVariableName() {
    return variableName;
  }

  private String getName() {
    return variableName.getText();
  }

  private String getValueType() {
    return valueType.getValue();
  }

  private boolean getRepeatable() {
    return repeatable.getValue();
  }

  private String getUnit() {
    return unit.getText();
  }

  private String getReferencedEntityType() {
    return refEntityType.getText();
  }

  private String getMimeType() {
    return mimeType.getText();
  }

  private String getOccurenceGroup() {
    return occurenceGroup.getText();
  }
}
