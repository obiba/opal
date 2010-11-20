/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CreateViewStepView extends Composite implements CreateViewStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep selectTypeStep;

  @UiField
  HTMLPanel helpPanel;

  @UiField
  SimplePanel datasourceSelectorPanel;

  @UiField
  SimplePanel tableSelectorPanel;

  @UiField
  TextBox viewNameTextBox;

  @UiField
  RadioButton applyingGlobalVariableFilterRadioButton;

  @UiField
  RadioButton addingVariablesOneByOneRadioButton;

  private DatasourceSelectorPresenter.Display datasourceSelector;

  private TableListPresenter.Display tableSelector;

  //
  // Constructors
  //

  public CreateViewStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWizardDialog();
    initSelectTypeStep();
  }

  private void initWizardDialog() {
    dialog.setGlassEnabled(false);
    dialog.hide();
    dialog.setHelpTooltip(helpPanel);
    dialog.setNextEnabled(false);
    dialog.setFinishEnabled(true);
  }

  private void initSelectTypeStep() {
    selectTypeStep.setStepTitle(translations.createDatasourceStepSummary());

  }

  //
  // CreateViewStepPresenter.Display Methods
  //

  public void clear() {
    selectTypeStep.setVisible(true);
    dialog.setHelpTooltip(helpPanel);

    datasourceSelector.selectFirst();

    viewNameTextBox.setText("");

    if(tableSelector != null) {
      tableSelector.clear();
    }

    applyingGlobalVariableFilterRadioButton.setValue(true);
    addingVariablesOneByOneRadioButton.setValue(false);
  }

  public void setDatasourceSelector(DatasourceSelectorPresenter.Display datasourceSelector) {
    this.datasourceSelector = datasourceSelector;
    datasourceSelectorPanel.add(datasourceSelector.asWidget());
  }

  public void setDatasourceSelectorEnabled(boolean enabled) {
    datasourceSelector.setEnabled(enabled);
  }

  public void setTableSelector(TableListPresenter.Display tableSelector) {
    this.tableSelector = tableSelector;
    tableSelectorPanel.add(tableSelector.asWidget());
  }

  public HasText getViewName() {
    return viewNameTextBox;
  }

  public HasText getDatasourceName() {
    final String selectedDatasourceName = datasourceSelector.getSelection();

    return new HasText() {

      public String getText() {
        return selectedDatasourceName;
      }

      public void setText(String text) {
        if(text != null) {
          datasourceSelector.setSelection(text);
        }
      }
    };
  }

  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  public HandlerRegistration addCreateClickHandler(final ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  public HasValue<Boolean> getApplyGlobalVariableFilterOption() {
    return applyingGlobalVariableFilterRadioButton;
  }

  public HasValue<Boolean> getAddVariablesOneByOneOption() {
    return addingVariablesOneByOneRadioButton;
  }

  @Override
  public void showDialog() {
    clear();
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CreateViewStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CreateViewStepView> {
  }

}
