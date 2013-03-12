/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.PropertiesTable;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 *
 */
public class TableComparePanel extends FlowPanel {

  private static Translations translations = GWT.create(Translations.class);

  private final TableCompareDto tableCompareDto;

  private final FieldUpdater<VariableDto, String> variableFieldUpdater;

  private final FieldUpdater<ConflictDto, String> conflictFieldUpdater;

  public TableComparePanel(TableCompareDto tableCompareDto, FieldUpdater<VariableDto, String> variableFieldUpdater,
      FieldUpdater<ConflictDto, String> conflictFieldUpdater) {
    super();
    this.tableCompareDto = tableCompareDto;
    this.variableFieldUpdater = variableFieldUpdater;
    this.conflictFieldUpdater = conflictFieldUpdater;

    add(initProperties());
    add(initVariableChangesPanel());
  }

  private PropertiesTable initProperties() {
    PropertiesTable properties = new PropertiesTable();
    properties.setZebra(true);
    properties.setCondensed(true);
    properties.setBorderedCell(false);
    properties.setKeyStyleNames("span2");

    properties.addProperty(translations.entityTypeLabel(), tableCompareDto.getCompared().getEntityType());

    return properties;
  }

  private HorizontalTabLayout initVariableChangesPanel() {
    HorizontalTabLayout tabs = new HorizontalTabLayout();
    tabs.addStyleName("top-margin");

    JsArray<VariableDto> newVariables = JsArrays.toSafeArray(tableCompareDto.getNewVariablesArray());
    JsArray<VariableDto> unmodifiedVariables = JsArrays.toSafeArray(tableCompareDto.getUnmodifiedVariablesArray());
    JsArray<VariableDto> modifiedVariables = JsArrays.toSafeArray(tableCompareDto.getModifiedVariablesArray());
    JsArray<ConflictDto> conflicts = JsArrays.toSafeArray(tableCompareDto.getConflictsArray());

    if(unmodifiedVariables.length() > 0) {
      addVariablesTab(unmodifiedVariables, tabs, translations.unmodifiedVariablesLabel());
    }

    if(newVariables.length() > 0) {
      addVariablesTab(newVariables, tabs, translations.newVariablesLabel());
    }

    if(modifiedVariables.length() > 0) {
      addVariablesTab(modifiedVariables, tabs, translations.modifiedVariablesLabel());
    }

    if(conflicts.length() > 0) {
      addConflictsTab(conflicts, tabs);
    }

    tabs.setVisible(tabs.getTabCount() > 0);

    return tabs;
  }

  private void addConflictsTab(JsArray<ConflictDto> conflicts, HorizontalTabLayout variableChangesPanel) {
    TableCompareConflictsTable variableConflictsDetails = new TableCompareConflictsTable();
    variableConflictsDetails.getVariableNameColumn().setFieldUpdater(conflictFieldUpdater);

    addTab(variableChangesPanel, translations.conflictedVariablesLabel(), variableConflictsDetails);

    JsArrayDataProvider<ConflictDto> dataProvider = new JsArrayDataProvider<ConflictDto>();
    dataProvider.addDataDisplay(variableConflictsDetails);
    dataProvider.setArray(conflicts);
  }

  private void addVariablesTab(JsArray<VariableDto> variables, HorizontalTabLayout variableChangesPanel,
      String tabTitle) {
    TableCompareVariablesTable variablesDetails = new TableCompareVariablesTable();
    variablesDetails.getVariableNameColumn().setFieldUpdater(variableFieldUpdater);

    addTab(variableChangesPanel, tabTitle, variablesDetails);

    JsArrayDataProvider<VariableDto> dataProvider = new JsArrayDataProvider<VariableDto>();
    dataProvider.addDataDisplay(variablesDetails);
    dataProvider.setArray(variables);
  }

  private <T extends JavaScriptObject> SimplePager addTab(HorizontalTabLayout tabs, String tabTitle,
      CellTable<T> table) {
    FlowPanel panel = new FlowPanel();
    SimplePager pager = new SimplePager();
    pager.addStyleName("right-aligned");
    pager.setDisplay(table);
    panel.add(pager);

    table.addStyleName("left-aligned");
    table.setWidth("100%");
    panel.add(table);
    tabs.add(panel, tabTitle);

    return pager;
  }

}
