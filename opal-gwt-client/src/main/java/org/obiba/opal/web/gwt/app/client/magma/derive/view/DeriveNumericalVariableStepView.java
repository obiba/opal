/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DerivationUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class DeriveNumericalVariableStepView extends ViewWithUiHandlers<DerivationUiHandlers>
    implements DeriveNumericalVariableStepPresenter.Display {

  @UiTemplate("DeriveNumericalVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveNumericalVariableStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardStep methodStep;

  @UiField
  WizardStep mapStep;

  @UiField
  ValueMapGrid valuesMapGrid;

  @UiField
  RadioButton rangeRadio;

  @UiField
  RadioButton discreteRadio;

  @UiField
  RadioButton manualRadio;

  @UiField
  NumericTextBox fromBox;

  @UiField
  NumericTextBox toBox;

  @UiField
  RadioButton lengthRadio;

  @UiField
  NumericTextBox lengthBox;

  @UiField
  RadioButton countRadio;

  @UiField
  NumericTextBox countBox;

  @UiField
  CollapsiblePanel addPanel;

  @UiField
  RadioButton addRangeRadio;

  @UiField
  RadioButton addDiscreteRadio;

  @UiField
  NumericTextBox addFromBox;

  @UiField
  NumericTextBox addToBox;

  @UiField
  NumericTextBox valueBox;

  @UiField
  TextBox newValueBox;

  @UiField
  Button addButton;

  @UiField
  Panel summary;

  @UiField
  FluidRow rangesRow;

  //
  // Constructors
  //

  public DeriveNumericalVariableStepView() {
    widget = uiBinder.createAndBindUi(this);

    initializeValueMapEntryForm();

    lengthRadio.setValue(true, true);
    rangeRadio.setValue(true, true);
    addRangeRadio.setValue(true, true);

  }

  private void initializeValueMapEntryForm() {
    valuesMapGrid.enableRowDeletion(true);
    addPanel.setText(translations.addValueMapping());
  }

  private void setAddRangeEnabled(boolean enabled) {
    addFromBox.setEnabled(enabled);
    addToBox.setEnabled(enabled);
    valueBox.setEnabled(!enabled);
  }

  private void setRangeEnabled(boolean enabled) {
    rangesRow.setVisible(enabled);
    lengthBox.setEnabled(enabled && lengthRadio.getValue());
    countBox.setEnabled(enabled && countRadio.getValue());
  }

  private void setLengthEnabled(boolean enabled) {
    lengthBox.setEnabled(enabled);
    countBox.setEnabled(!enabled);
  }

  @Override
  public DefaultWizardStepController.Builder getMethodStepBuilder() {
    return DefaultWizardStepController.Builder.create(methodStep).title(translations.recodeNumericalMethodStepTitle());
  }

  @Override
  public DefaultWizardStepController.Builder getMapStepBuilder() {
    return DefaultWizardStepController.Builder.create(mapStep).title(translations.recodeNumericalMapStepTitle());
  }

  @UiHandler({ "rangeRadio", "discreteRadio", "manualRadio", "lengthRadio", "countRadio" })
  void onMethodChanged(ClickEvent event) {
    getUiHandlers().onMethodChange();
  }

  @UiHandler({ "lengthBox", "countBox", "fromBox", "toBox" })
  void onMethodChanged(KeyUpEvent event) {
    getUiHandlers().onMethodChange();
  }

  @UiHandler({ "rangeRadio", "discreteRadio", "manualRadio" })
  void onRangeRadio(ClickEvent event) {
    setRangeEnabled(rangeRadio.getValue());
  }

  @UiHandler({ "lengthRadio", "countRadio" })
  void onLengthRadio(ClickEvent event) {
    setLengthEnabled(lengthRadio.getValue());
  }

  @UiHandler({ "addRangeRadio", "addDiscreteRadio" })
  void onAddRadio(ClickEvent event) {
    setAddRangeEnabled(addRangeRadio.getValue());
  }

  //
  // Widget Display methods
  //

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setSummaryTabWidget(View widget) {
    summary.clear();
    summary.add(widget.asWidget());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valuesMap, List<String> derivedCategories) {
    // clear Add form
    addFromBox.setText("");
    addToBox.setText("");
    valueBox.setText("");
    newValueBox.setText("");
    addRangeRadio.setValue(true, true);
    addPanel.setOpen(false);
    // populates
    valuesMapGrid.populate(valuesMap, derivedCategories);
  }

  @Override
  public HandlerRegistration addValueMapEntryHandler(ClickHandler handler) {
    return addButton.addClickHandler(handler);
  }

  @Override
  public String getNewValue() {
    return newValueBox.getText();
  }

  @Override
  public boolean addRangeSelected() {
    return addRangeRadio.getValue();
  }

  @Override
  public Number getDiscreteValue() {
    return valueBox.getNumberValue();
  }

  @Override
  public Number getLowerValue() {
    return addFromBox.getNumberValue();
  }

  @Override
  public Number getUpperValue() {
    return addToBox.getNumberValue();
  }

  @Override
  public void refreshValuesMapDisplay() {
    valuesMapGrid.refreshValuesMap();
  }

  @Override
  public void setNumberType(String valueType) {
    fromBox.setNumberType(valueType);
    toBox.setNumberType(valueType);
    lengthBox.setNumberType(valueType);
    addFromBox.setNumberType(valueType);
    addToBox.setNumberType(valueType);
    valueBox.setNumberType(valueType);
  }

  @Override
  public void setValueLimits(Number from, Number to) {
    fromBox.setValue(from.toString());
    toBox.setValue(to.toString());
  }

  @Override
  public Number getLowerLimit() {
    return fromBox.getNumberValue();
  }

  @Override
  public Number getUpperLimit() {
    return toBox.getNumberValue();
  }

  @Override
  public boolean rangeLengthSelected() {
    return lengthRadio.getValue();
  }

  @Override
  public Number getRangeLength() {
    return lengthBox.getNumberValue();
  }

  @Override
  public Long getRangeCount() {
    return countBox.getNumberValue();
  }

  @Override
  public boolean rangeSelected() {
    return rangeRadio.getValue();
  }

  @Override
  public boolean discreteSelected() {
    return discreteRadio.getValue();
  }

  @Override
  public boolean manualSelected() {
    return manualRadio.getValue();
  }

  @Override
  public void enableFrequency(boolean enable) {
    valuesMapGrid.enableFrequencyColumn(enable);
  }

  @Override
  public void setRangeCountError(boolean error) {
    setUIObjectError(countBox, error);
  }

  @Override
  public void setRangeLengthError(boolean error) {
    setUIObjectError(lengthBox, error);
  }

  @Override
  public void setUpperLimitError(boolean error) {
    setUIObjectError(toBox, error);
  }

  @Override
  public void setLowerLimitError(boolean error) {
    setUIObjectError(fromBox, error);
  }

  private void setUIObjectError(UIObject obj, boolean error) {
    if(error) {
      obj.addStyleName("error");
    } else {
      obj.removeStyleName("error");
    }
  }
}
