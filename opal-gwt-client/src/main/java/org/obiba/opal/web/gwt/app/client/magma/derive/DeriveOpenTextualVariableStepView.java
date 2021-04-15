/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.OpenTextualVariableDerivationHelper.Method;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.RadioGroup;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController.Builder;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DeriveOpenTextualVariableStepView extends ViewImpl
    implements DeriveOpenTextualVariableStepPresenter.Display {

  @UiTemplate("DeriveOpenTextualVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveOpenTextualVariableStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  // --- Method fields ---
  @UiField
  WizardStep methodStep;

  @UiField
  WizardStep mapStep;

  @UiField
  RadioButton auto;

  @UiField
  RadioButton manual;

  final RadioGroup<Method> radioGroup;

  // --- Map fields ---

  @UiField
  CollapsiblePanel addPanel;

  @UiField(provided = true)
  SuggestBox value;

  @UiField
  TextBox newValue;

  @UiField
  Button addButton;

  @UiField
  ValueMapGrid valuesMapGrid;

  final MultiWordSuggestOracleWithDisplay valueOracle;

  public DeriveOpenTextualVariableStepView() {
    value = new SuggestBox(valueOracle = new MultiWordSuggestOracleWithDisplay());
    widget = uiBinder.createAndBindUi(this);
    radioGroup = new RadioGroup<Method>();
    radioGroup.addButton(auto, Method.AUTOMATICALLY);
    radioGroup.addButton(manual, Method.MANUAL);
    auto.setValue(true);
    addButton.setText("Add");
    addPanel.setText(translations.addValueMapping());
  }

  @Override
  public Method getMethod() {
    return radioGroup.getValue();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public DefaultWizardStepController.Builder getMethodStepController() {
    return DefaultWizardStepController.Builder.create(methodStep)
        .title(translations.recodeOpenTextualMethodStepTitle());
  }

  @Override
  public Builder getMapStepController() {
    return DefaultWizardStepController.Builder.create(mapStep).title(translations.recodeOpenTextualMapStepTitle());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valueMapEntries, @Nullable List<String> derivedCategories) {
    value.setText("");
    newValue.setText("");
    addPanel.setOpen(false);
    valuesMapGrid.populate(valueMapEntries, derivedCategories);
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
  public void addValueSuggestion(String value, String frequency) {
    valueOracle.add(value, frequency);
  }

  private static class MultiWordSuggestOracleWithDisplay extends MultiWordSuggestOracle {

    final Map<String, String> map = new HashMap<String, String>();

    public void add(String value, String frequency) {
      add(value);
      map.put(value, frequency);
    }

    @Override
    protected MultiWordSuggestion createSuggestion(String replacementString, String displayString) {
      return super.createSuggestion(replacementString, displayString + " (" + map.get(replacementString) + ")");
    }

  }

}
