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
import org.obiba.opal.web.gwt.app.client.ui.RadioGroup;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController.Builder;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.OpenTextualVariableDerivationHelper.Method;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveOpenTextualVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DeriveOpenTextualVariableStepView extends Composite implements DeriveOpenTextualVariableStepPresenter.Display {

  @UiTemplate("DeriveOpenTextualVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveOpenTextualVariableStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  // --- Method fields ---
  @UiField
  WizardStep methodStep;

  @UiField
  WizardStep mapStep;

  @UiField(provided = true)
  RadioButton auto;

  @UiField
  Label labelAuto;

  @UiField(provided = true)
  RadioButton manual;

  @UiField
  Label labelManual;

  RadioGroup<Method> radioGroup;

  // --- Map fields ---

  @UiField(provided = true)
  SuggestBox value;

  @UiField(provided = true)
  SuggestBox newValue;

  @UiField
  Button addButton;

  @UiField
  ValueMapGrid valuesMapGrid;

  MultiWordSuggestOracleWithDisplay valueOracle;

  MultiWordSuggestOracleWithDisplay newValueOracle;

  public DeriveOpenTextualVariableStepView() {
    value = new SuggestBox(valueOracle = new MultiWordSuggestOracleWithDisplay());
    newValue = new SuggestBox(newValueOracle = new MultiWordSuggestOracleWithDisplay());

    auto = new RadioButton(Method.group);
    manual = new RadioButton(Method.group);

    initWidget(uiBinder.createAndBindUi(this));

    radioGroup = new RadioGroup<Method>();
    radioGroup.addButton(auto, Method.AUTOMATICALLY);
    radioGroup.addButton(manual, Method.MANUAL);

    auto.setText(translations.automatically());
    auto.setValue(true);
    labelAuto.setText("Similar disctinct values will be grouped together");

    manual.setText(translations.manually());
    labelManual.setText("No predefined value mapping");

    addButton.setText("Add");
  }

  @Override
  public Method getMethod() {
    return radioGroup.getValue();
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public DefaultWizardStepController.Builder getMethodStepController() {
    return DefaultWizardStepController.Builder.create(methodStep).title(translations.recodeOpenTextualMethodStepTitle());
  }

  @Override
  public Builder getMapStepController() {
    return DefaultWizardStepController.Builder.create(mapStep).title(translations.recodeOpenTextualMapStepTitle());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valueMapEntries) {
    valuesMapGrid.populate(valueMapEntries);
  }

  @Override
  public HasValue<String> getValue() {
    return value;
  }

  @Override
  public HasValue<String> getNewValue() {
    return newValue;
  }

  @Override
  public Button getAddButton() {
    return addButton;
  }

  @Override
  public void emptyValueFields() {
    value.setValue(null);
    newValue.setValue(null);
  }

  @Override
  public ValueMapGrid getValueMapGrid() {
    return valuesMapGrid;
  }

  @Override
  public void entryAdded() {
    valuesMapGrid.entryAdded();
  }

  @Override
  public void addValueSuggestion(String replacementString, String displayString) {
    valueOracle.add(replacementString, displayString);
  }

}
