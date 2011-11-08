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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RadioButton;
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
  NumericTextBox fromBox;

  @UiField
  NumericTextBox toBox;

  @UiField
  RadioButton lengthRadio;

  @UiField
  NumericTextBox lengthBox;

  @UiField
  RadioButton numberRadio;

  @UiField
  NumericTextBox numberBox;

  //
  // Constructors
  //

  public DeriveNumericalVariableStepView() {
    initWidget(uiBinder.createAndBindUi(this));

    setRangeEnabled(true);

    rangeRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setRangeEnabled(true);
      }
    });

    discreteRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setRangeEnabled(false);
      }
    });

    lengthRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setLengthEnabled(true);
      }
    });

    numberRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setLengthEnabled(false);
      }
    });
  }

  private void setRangeEnabled(boolean enabled) {
    fromBox.setEnabled(enabled);
    toBox.setEnabled(enabled);
    lengthRadio.setEnabled(enabled);
    numberRadio.setEnabled(enabled);
    lengthBox.setEnabled(enabled && lengthRadio.getValue());
    numberBox.setEnabled(enabled && numberRadio.getValue());
  }

  private void setLengthEnabled(boolean enabled) {
    lengthBox.setEnabled(enabled);
    numberBox.setEnabled(!enabled);
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
  public void populateValues(List<ValueMapEntry> valuesMap) {
    valuesMapGrid.populate(valuesMap);
  }

}
