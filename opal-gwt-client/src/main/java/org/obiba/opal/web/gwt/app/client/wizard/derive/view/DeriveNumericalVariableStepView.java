/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import java.util.List;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DeriveNumericalVariableStepView extends Composite implements DeriveNumericalVariableStepPresenter.Display {

  @UiTemplate("DeriveNumericalVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveNumericalVariableStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

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
  DisclosurePanel addPanel;

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

  //
  // Constructors
  //

  public DeriveNumericalVariableStepView() {
    initWidget(uiBinder.createAndBindUi(this));

    initializeMethodForm();
    initializeValueMapEntryForm();
  }

  private void initializeValueMapEntryForm() {
    addRangeRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setAddRangeEnabled(true);
      }
    });

    addDiscreteRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setAddRangeEnabled(false);
      }
    });

    addRangeRadio.setValue(true, true);
    valuesMapGrid.enableRowDeletion(true);
  }

  private void initializeMethodForm() {
    rangeRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setRangeEnabled(true);
      }
    });

    ValueChangeHandler<Boolean> rangeDisable = new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setRangeEnabled(false);
      }
    };

    discreteRadio.addValueChangeHandler(rangeDisable);
    manualRadio.addValueChangeHandler(rangeDisable);

    lengthRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setLengthEnabled(true);
      }
    });

    countRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setLengthEnabled(false);
      }
    });

    rangeRadio.setValue(true, true);
    lengthRadio.setValue(true, true);

  }

  private void setAddRangeEnabled(boolean enabled) {
    addFromBox.setEnabled(enabled);
    addToBox.setEnabled(enabled);
    valueBox.setEnabled(!enabled);
  }

  private void setRangeEnabled(boolean enabled) {
    fromBox.setEnabled(enabled);
    toBox.setEnabled(enabled);
    lengthRadio.setEnabled(enabled);
    countRadio.setEnabled(enabled);
    lengthBox.setEnabled(enabled && lengthRadio.getValue());
    countBox.setEnabled(enabled && countRadio.getValue());
  }

  private void setLengthEnabled(boolean enabled) {
    lengthBox.setEnabled(enabled);
    countBox.setEnabled(!enabled);
  }

  @Override
  public DefaultWizardStepController.Builder getMethodStepController() {
    return DefaultWizardStepController.Builder.create(methodStep).title(translations.recodeNumericalMethodStepTitle());
  }

  @Override
  public DefaultWizardStepController.Builder getMapStepController() {
    return DefaultWizardStepController.Builder.create(mapStep).title(translations.recodeNumericalMapStepTitle());
  }

  //
  // Widget Display methods
  //

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
  public void setSummaryTabWidget(WidgetDisplay widget) {
    summary.clear();
    summary.add(widget.asWidget());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valuesMap) {
    // clear Add form
    addFromBox.setText("");
    addToBox.setText("");
    valueBox.setText("");
    newValueBox.setText("");
    addRangeRadio.setValue(true, true);
    addPanel.setOpen(false);
    // populates
    valuesMapGrid.populate(valuesMap);
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

  @Override
  public void setMaxFrequency(double maxFreq) {
    valuesMapGrid.setMaxFrequency(maxFreq);
  }
}
