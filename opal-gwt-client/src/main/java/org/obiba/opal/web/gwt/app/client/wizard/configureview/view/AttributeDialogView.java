/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AttributeDialogPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AttributeDialogView extends Composite implements AttributeDialogPresenter.Display {

  @UiTemplate("AttributeDialogView.ui.xml")
  interface MyUiBinder extends UiBinder<DialogBox, AttributeDialogView> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  RadioButton nameDropdownRadioChoice;

  @UiField
  RadioButton nameFieldRadioChoice;

  @UiField
  ListBox labels;

  @UiField
  TextBox attributeName;

  @UiField
  ScrollPanel scrollPanel;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  public AttributeDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void showDialog() {
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public Button getCancelButton() {
    return cancelButton;
  }

  @Override
  public Button getSaveButton() {
    return saveButton;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

  @Override
  public HandlerRegistration addNameDropdownRadioChoiceHandler(ClickHandler handler) {
    return nameDropdownRadioChoice.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addNameFieldRadioChoiceHandler(ClickHandler handler) {
    return nameFieldRadioChoice.addClickHandler(handler);
  }

  @Override
  public void setLabelsEnabled(boolean enabled) {
    labels.setEnabled(enabled);
  }

  @Override
  public void setAttributeNameEnabled(boolean enabled) {
    attributeName.setEnabled(enabled);
  }

  @Override
  public void selectNameDropdownRadioChoice() {
    nameDropdownRadioChoice.setValue(true, true);
    nameFieldRadioChoice.setValue(false, true);
  }

  @Override
  public HasText getAttributeName() {
    return attributeName;
  }

  @Override
  public void addLabelListPresenter(Widget widget) {
    scrollPanel.clear();
    scrollPanel.add(widget);
  }

  @Override
  public void removeLabelListPresenter(Widget widget) {
    scrollPanel.remove(widget);
  }
}
