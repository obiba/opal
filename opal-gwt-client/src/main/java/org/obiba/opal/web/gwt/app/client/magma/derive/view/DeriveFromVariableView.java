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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveFromVariablePresenter;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.gwt.app.client.ui.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.Skippable;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DeriveFromVariableView extends ViewImpl implements DeriveFromVariablePresenter.Display {

  interface Binder extends UiBinder<Widget, DeriveFromVariableView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  WizardStep deriveFromVariableStep;

  @UiField
  TableChooser tableChooser;

  @UiField
  Chooser variableBox;

  @Inject
  public DeriveFromVariableView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    tableChooser.addStyleName("table-chooser-large");
    variableBox.addStyleName("table-chooser-large");
  }

  @Override
  public BranchingWizardStepController.Builder getDeriveFromVariableStepController(final boolean skip) {
    return (BranchingWizardStepController.Builder) BranchingWizardStepController.Builder
        .create(deriveFromVariableStep, new Skippable() {
          @Override
          public boolean skip() {
            return skip;
          }
        }).title(translations.deriveFromVariableStepTitle());
  }

  @Override
  public HasChangeHandlers getVariableList() {
    return variableBox;
  }

  @Override
  public void setVariableListEnabled(boolean b) {
    variableBox.setEnabled(b);
  }

  @Override
  public String getSelectedVariable() {
    int index = variableBox.getSelectedIndex();
    return index == -1 ? null : variableBox.getValue(index);
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables, @Nullable String selectedVariable) {
    variableBox.clear();
//    variableBox.setWidth("30em");
    variableBox.forceRedraw();
    if(variables != null) {
      for(int i = 0; i < variables.length(); i++) {
        String name = variables.get(i).getName();
        variableBox.insertItem(name, i);
        if(selectedVariable != null && selectedVariable.equals(name)) {
          variableBox.setSelectedValue(name);
        }
      }
      if(variableBox.getSelectedIndex() == -1) variableBox.setSelectedIndex(0);
    }
  }

  @Override
  public HasChangeHandlers getTableList() {
    return tableChooser;
  }

  @Override
  public void addTableSelections(JsArray<TableDto> tables) {
    tableChooser.addTableSelections(tables);
  }

  @Override
  public void selectTable(TableDto tableDto) {
    tableChooser.selectTable(tableDto);
  }

  @Override
  public TableDto getSelectedTable() {
    if(tableChooser.getSelectedTables().size() == 1) {
      return tableChooser.getSelectedTables().get(0);
    }
    return null;
  }
}
