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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.Skippable;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveFromVariablePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DeriveFromVariableView extends ViewImpl implements DeriveFromVariablePresenter.Display {

  @UiTemplate("DeriveFromVariableView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveFromVariableView> {
  }

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField WizardStep deriveFromVariableStep;

  @UiField ListBox datasourceBox;

  @UiField ListBox tableBox;

  @UiField ListBox variableBox;

  public DeriveFromVariableView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public BranchingWizardStepController.Builder getDeriveFromVariableStepController(final boolean skip) {
    return (BranchingWizardStepController.Builder) BranchingWizardStepController.Builder
        .create(deriveFromVariableStep, null, new Skippable() {
          @Override
          public boolean skip() {
            return skip;
          }
        }).title(translations.deriveFromVariableStepTitle());
  }

  @Override
  public void setDatasources(List<String> datasources, @Nullable String selectedDatasource) {
    datasourceBox.clear();

    for(int i = 0; i < datasources.size(); i++) {
      String name = datasources.get(i);
      datasourceBox.addItem(name);
      if(selectedDatasource != null && selectedDatasource.equals(name)) {
        datasourceBox.setSelectedIndex(i);
      }
    }
    if(datasourceBox.getSelectedIndex() == -1) datasourceBox.setSelectedIndex(0);
  }

  @Override
  public String getSelectedDatasource() {
    int index = datasourceBox.getSelectedIndex();
    return index == -1 ? null : datasourceBox.getValue(index);
  }

  @Override
  public HasChangeHandlers getDatasourceList() {
    return datasourceBox;
  }

  @Override
  public HasChangeHandlers getVariableList() {
    return variableBox;
  }

  @Override
  public void setTables(List<String> tables, @Nullable String selectedTable) {
    tableBox.clear();
    for(int i = 0; i < tables.size(); i++) {
      String name = tables.get(i);
      tableBox.addItem(name);
      if(selectedTable != null && selectedTable.equals(name)) {
        tableBox.setSelectedIndex(i);
      }
    }
    if(tableBox.getSelectedIndex() == -1) tableBox.setSelectedIndex(0);
  }

  @Override
  public String getSelectedTable() {
    int index = tableBox.getSelectedIndex();
    return index == -1 ? null : tableBox.getValue(index);
  }

  @Override
  public String getSelectedVariable() {
    int index = variableBox.getSelectedIndex();
    return index == -1 ? null : variableBox.getValue(index);
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables, @Nullable String selectedVariable) {
    variableBox.clear();
    if(variables != null) {
      for(int i = 0; i < variables.length(); i++) {
        String name = variables.get(i).getName();
        variableBox.addItem(name);
        if(selectedVariable != null && selectedVariable.equals(name)) {
          variableBox.setSelectedIndex(i);
        }
      }
      if(variableBox.getSelectedIndex() == -1) variableBox.setSelectedIndex(0);
    }
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HasChangeHandlers getTableList() {
    return tableBox;
  }
}
