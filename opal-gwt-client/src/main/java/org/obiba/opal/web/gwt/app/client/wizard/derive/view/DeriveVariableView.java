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
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.ValueDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestBox;
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
  WizardStep summaryStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  Panel summary;

  @UiField
  CellTable<ValueDto> valuesTable;

  @UiField
  Anchor previousPage;

  @UiField
  Anchor nextPage;

  @UiField
  Label pageLow;

  @UiField
  Label pageHigh;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  CheckBox openEditor;

  @UiField
  TextBox derivedNameBox;

  @UiField
  ListBox datasourceNameBox;

  @UiField(provided = true)
  SuggestBox viewNameBox;

  MultiWordSuggestOracle viewNameSuggestions;

  private StepInHandler summaryHandler;

  private final WizardDialogBox dialog;

  private WizardStepChain stepChain;

  private WizardStepChain.Builder stepChainBuilder;

  private Map<String, List<String>> viewSuggestions;

  public DeriveVariableView() {
    viewNameBox = new SuggestBox(viewNameSuggestions = new MultiWordSuggestOracle());
    this.dialog = uiBinder.createAndBindUi(this);

    valuesTable.addColumn(new TextColumn<ValueDto>() {

      @Override
      public String getValue(ValueDto value) {
        return value.getValue();
      }
    }, "Value");

    datasourceNameBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        viewNameSuggestions.clear();
        for(String viewName : viewSuggestions.get(getDatasourceName())) {
          viewNameSuggestions.add(viewName);
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

    .append(summaryStep)//
    .title("Summary")//
    .onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        summaryHandler.onStepIn();
      }
    })//

    .append(conclusionStep)//
    .title("Conclusion")//

    .onNext().onPrevious().build();

    stepsFlow.add(summaryStep);
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
    summaryStep.setVisible(false);
    conclusionStep.setVisible(false);
  }

  @Override
  public void setSummaryTabWidget(WidgetDisplay widget) {
    summary.clear();
    summary.add(widget.asWidget());
  }

  @Override
  public void setSummaryStepInHandler(StepInHandler handler) {
    summaryHandler = handler;
  }

  @Override
  public void populateValues(JsArray<ValueDto> values) {
    JsArrayDataProvider<ValueDto> dataProvider = new JsArrayDataProvider<ValueDto>();
    dataProvider.addDataDisplay(valuesTable);
    dataProvider.setArray(JsArrays.toSafeArray(values));
    dataProvider.refresh();
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
      viewNameSuggestions.add(viewName);
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

    viewNameSuggestions.clear();
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
  public HandlerRegistration addNextPageClickHandler(ClickHandler handler) {
    return nextPage.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addPreviousPageClickHandler(ClickHandler handler) {
    return previousPage.addClickHandler(handler);
  }

  @Override
  public void setPageLimits(int low, int high) {
    previousPage.setEnabled(low > 1);
    pageLow.setText("" + low);
    pageHigh.setText("" + high);
  }

  @Override
  public boolean isOpenEditorSelected() {
    return openEditor.getValue();
  }

}
