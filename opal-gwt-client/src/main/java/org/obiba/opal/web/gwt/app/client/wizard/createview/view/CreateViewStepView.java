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

import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
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

  //
  // Instance Variables
  //

  @UiField
  Button cancelButton;

  @UiField
  Button createButton;

  @UiField
  RadioButton selectExistingDatasourceRadioButton;

  @UiField
  RadioButton createNewDatasourceRadioButton;

  @UiField
  TextBox createNewDatasourceTextBox;

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
  }

  //
  // CreateViewStepPresenter.Display Methods
  //

  public void clear() {
    selectExistingDatasourceRadioButton.setEnabled(true);
    selectExistingDatasourceRadioButton.setValue(false);
    if(datasourceSelector != null) {
      datasourceSelector.setEnabled(false);
      datasourceSelector.selectFirst();
    }

    createNewDatasourceRadioButton.setEnabled(true);
    createNewDatasourceRadioButton.setValue(false);
    createNewDatasourceTextBox.setText("");
    createNewDatasourceTextBox.setEnabled(false);

    viewNameTextBox.setText("");

    if(tableSelector != null) {
      tableSelector.clear();
    }

    applyingGlobalVariableFilterRadioButton.setValue(false);
    addingVariablesOneByOneRadioButton.setValue(false);
  }

  public void setDatasourceSelector(DatasourceSelectorPresenter.Display datasourceSelector) {
    this.datasourceSelector = datasourceSelector;
    datasourceSelectorPanel.add(datasourceSelector.asWidget());
  }

  public void setDatasourceSelectorEnabled(boolean enabled) {
    datasourceSelector.setEnabled(enabled);
  }

  public void setNewDatasourceInputEnabled(boolean enabled) {
    createNewDatasourceTextBox.setEnabled(enabled);
  }

  public void setTableSelector(TableListPresenter.Display tableSelector) {
    this.tableSelector = tableSelector;
    tableSelectorPanel.add(tableSelector.asWidget());
  }

  public HasValue<Boolean> getAttachToExistingDatasourceOption() {
    return selectExistingDatasourceRadioButton;
  }

  public void setAttachToExistingDatasourceOptionEnabled(boolean enabled) {
    selectExistingDatasourceRadioButton.setEnabled(enabled);
  }

  public HasValue<Boolean> getAttachToNewDatasourceOption() {
    return createNewDatasourceRadioButton;
  }

  public void setAttachToNewDatasourceOptionEnabled(boolean enabled) {
    createNewDatasourceRadioButton.setEnabled(enabled);
  }

  public HasText getViewName() {
    return viewNameTextBox;
  }

  public HasValue<Boolean> getApplyGlobalVariableFilterOption() {
    return applyingGlobalVariableFilterRadioButton;
  }

  public HasValue<Boolean> getAddVariablesOneByOneOption() {
    return addingVariablesOneByOneRadioButton;
  }

  public HasText getExistingDatasourceName() {
    final String selectedDatasourceName = getAttachToExistingDatasourceOption().getValue() ? datasourceSelector.getSelection() : null;

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

  public HasText getNewDatasourceName() {
    return createNewDatasourceTextBox;
  }

  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return cancelButton.addClickHandler(handler);
  }

  public HandlerRegistration addCreateClickHandler(ClickHandler handler) {
    return createButton.addClickHandler(handler);
  }

  public HandlerRegistration addSelectExistingDatasourceClickHandler(ClickHandler handler) {
    return selectExistingDatasourceRadioButton.addClickHandler(handler);
  }

  public HandlerRegistration addCreateNewDatasourceClickHandler(ClickHandler handler) {
    return createNewDatasourceRadioButton.addClickHandler(handler);
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
