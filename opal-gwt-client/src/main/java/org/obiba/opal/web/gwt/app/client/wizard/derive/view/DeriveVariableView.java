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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.DropdownSuggestBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DeriveVariableView extends Composite implements DeriveVariablePresenter.Display {

  @UiTemplate("DeriveVariableView.ui.xml")
  interface ViewUiBinder extends UiBinder<WizardDialogBox, DeriveVariableView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  FlowPanel stepsFlow;

  @UiField
  WizardStep scriptEvaluationStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  CheckBox openEditor;

  @UiField
  TextBox derivedNameBox;

  @UiField
  FlowPanel derivedNameInput;

  @UiField
  ListBox datasourceNameBox;

  @UiField(provided = true)
  DropdownSuggestBox viewNameBox;

  @UiField
  FlowPanel viewNameInput;

  private StepInHandler summaryHandler;

  private final WizardDialogBox dialog;

  private WizardStepChain stepChain;

  private WizardStepChain.Builder stepChainBuilder;

  private Map<String, List<String>> viewSuggestions;

  public DeriveVariableView() {
    this.viewNameBox = new DropdownSuggestBox();
    this.dialog = uiBinder.createAndBindUi(this);

    datasourceNameBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        viewNameBox.getSuggestOracle().clear();
        for(String viewName : viewSuggestions.get(getDatasourceName())) {
          viewNameBox.getSuggestOracle().add(viewName);
        }
        // viewNameBox.setText("");
      }
    });
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Override
  public void appendWizardSteps(List<DefaultWizardStepController> stepCtrls) {
    for(DefaultWizardStepController stepCtrl : stepCtrls) {
      appendWizardStep(stepCtrl);
    }
  }

  private void appendWizardStep(DefaultWizardStepController stepCtrl) {
    if(stepChainBuilder == null) {
      stepChainBuilder = WizardStepChain.Builder.create(dialog);
      stepsFlow.clear();
    }
    stepsFlow.add(stepCtrl.getStep());
    stepChainBuilder.append(stepCtrl);
  }

  private void initWizardDialog() {

    stepChain = stepChainBuilder//

    .append(scriptEvaluationStep)//
    .title(translations.derivedVariableEvaluation())//
    .onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        summaryHandler.onStepIn();
      }
    })//

    .append(conclusionStep)//
    .title(translations.saveDerivedVariable())//

    .onNext().onPrevious().build();

    stepsFlow.add(scriptEvaluationStep);
    stepsFlow.add(conclusionStep);

    // reset
    stepChainBuilder = null;
  }

  @Override
  public void showDialog() {
    if(stepChainBuilder != null) {
      initWizardDialog();
    }

    stepChain.reset();
    clear();
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void clear() {
    scriptEvaluationStep.setVisible(false);
    conclusionStep.setVisible(false);
  }

  @Override
  public void setScriptEvaluationWidget(WidgetDisplay widget) {
    scriptEvaluationStep.add(widget.asWidget());
  }

  @Override
  public void setScriptEvaluationStepInHandler(StepInHandler handler) {
    summaryHandler = handler;
  }

  @Override
  public void setDefaultDerivedName(String name) {
    derivedNameBox.setText(name);
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
  public void addViewSuggestion(DatasourceDto ds, String viewName) {
    viewSuggestions.get(ds.getName()).add(viewName);

    if(ds.getName().equals(getDatasourceName())) {
      viewNameBox.getSuggestOracle().add(viewName);
    }
  }

  @Override
  public void populateDatasources(JsArray<DatasourceDto> datasources) {
    viewSuggestions = new HashMap<String, List<String>>();

    datasourceNameBox.clear();
    for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
      viewSuggestions.put(ds.getName(), new ArrayList<String>());
      datasourceNameBox.addItem(ds.getName());
    }
    datasourceNameBox.setSelectedIndex(0);

    viewNameBox.getSuggestOracle().clear();
  }

  @Override
  public String getDerivedName() {
    return derivedNameBox.getText();
  }

  @Override
  public String getDatasourceName() {
    return datasourceNameBox.getItemText(datasourceNameBox.getSelectedIndex());
  }

  @Override
  public String getViewName() {
    return viewNameBox.getText();
  }

  @Override
  public boolean isOpenEditorSelected() {
    return openEditor.getValue();
  }

  @Override
  public void setDerivedNameError(boolean error) {
    if(error) {
      derivedNameInput.addStyleName("error");
    } else {
      derivedNameInput.removeStyleName("error");
    }
  }

  @Override
  public void setViewNameError(boolean error) {
    if(error) {
      viewNameInput.addStyleName("error");
    } else {
      viewNameInput.removeStyleName("error");
    }
  }

}
