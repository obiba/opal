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
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.PropertiesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.PropertiesEditorModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class PropertiesEditorModalView extends ModalPopupViewWithUiHandlers<PropertiesEditorModalUiHandlers>
    implements PropertiesEditorModalPresenter.Display {

  private static final int MIN_WIDTH = 480;

  private static final int MIN_HEIGHT = 400;

  private final Widget widget;

  private final Translations translations = GWT.create(Translations.class);

  interface PropertiesEditorModalUiBinder extends UiBinder<Widget, PropertiesEditorModalView> {}

  private static final PropertiesEditorModalUiBinder uiBinder = GWT.create(PropertiesEditorModalUiBinder.class);

  @UiField
  Modal dialog;

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
  public PropertiesEditorModalView(EventBus eventBus) {
    super(eventBus);

    widget = uiBinder.createAndBindUi(this);
    dialog.setTitle(translations.editProperties());
    dialog.setResizable(true);
    dialog.setMinWidth(MIN_WIDTH);
    dialog.setMinHeight(MIN_HEIGHT);
  }

  @Override
  public Widget asWidget() {
    return widget;
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
  public void renderProperties(VariableDto variable) {
    valueType.setSelectedValue(variable.getValueType());
    repeatable.setValue(variable.getIsRepeatable());
    unit.setText(variable.getUnit());
    refEntityType.setText(variable.getReferencedEntityType());
    mimeType.setText(variable.getMimeType());
    occurenceGroup.setText(variable.getOccurrenceGroup());

//    mimeType.setText(variable.hasMimeType() ? variable.getMimeType() : "");
//    unit.setText(variable.hasUnit() ? variable.getUnit() : "");
//    repeatable.setText(variable.getIsRepeatable() ? translations.yesLabel() : translations.noLabel());
//    occurrenceGroup.setText(variable.getIsRepeatable() ? variable.getOccurrenceGroup() : "");

    // if has data do not allow to change value
    valueType.setEnabled(false);
    repeatable.setEnabled(false);
    occurenceGroup.setEnabled(variable.getIsRepeatable());
  }

  @Override
  public JsArray<CategoryDto> getCategories() {
    JsArray<CategoryDto> list = JsArrays.create();

    return list;
  }

  @Override
  public void showError(String message, @Nullable ControlGroup group) {
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }

  @Override
  public void setDialogTitle(String title) {
    dialog.setTitle(title);
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
  public String getOccurenceGroup() {
    return occurenceGroup.getText();
  }
}
