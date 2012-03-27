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
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 */
public class TableComparePanel extends FlowPanel {

  private static Translations translations = GWT.create(Translations.class);

  public TableComparePanel(TableCompareDto tableCompareData) {
    super();
    HorizontalTabLayout variableChangesPanel = initVariableChangesPanel(tableCompareData);
    add(variableChangesPanel);
  }

  @SuppressWarnings("unchecked")
  private HorizontalTabLayout initVariableChangesPanel(TableCompareDto tableCompareData) {
    HorizontalTabLayout variableChangesPanel = new HorizontalTabLayout();

    JsArray<VariableDto> newVariables = JsArrays.toSafeArray(tableCompareData.getNewVariablesArray());
    JsArray<VariableDto> unmodifiedVariables = JsArrays.toSafeArray(tableCompareData.getUnmodifiedVariablesArray());
    JsArray<VariableDto> modifiedVariables = JsArrays.toSafeArray(tableCompareData.getModifiedVariablesArray());
    JsArray<ConflictDto> conflicts = JsArrays.toSafeArray(tableCompareData.getConflictsArray());

    if(unmodifiedVariables.length() > 0) {
      addVariablesTab(unmodifiedVariables, variableChangesPanel, translations.unmodifiedVariablesLabel());
    }

    if(newVariables.length() > 0) {
      addVariablesTab(newVariables, variableChangesPanel, translations.newVariablesLabel());
    }

    if(modifiedVariables.length() > 0) {
      addVariablesTab(modifiedVariables, variableChangesPanel, translations.modifiedVariablesLabel());
    }

    if(conflicts.length() > 0) {
      addConflictsTab(conflicts, variableChangesPanel);
    }

    variableChangesPanel.setVisible(variableChangesPanel.getTabCount() > 0);

    return variableChangesPanel;
  }

  private void addConflictsTab(JsArray<ConflictDto> conflicts, HorizontalTabLayout variableChangesPanel) {
    CellTable<ConflictDto> variableConflictsDetails = new TableCompareConflictsTable();
    SimplePager variableConflictsPager = prepareVariableChangesTab(variableChangesPanel, translations.conflictedVariablesLabel(), variableConflictsDetails);

    JsArrayDataProvider<ConflictDto> dataProvider = new JsArrayDataProvider<ConflictDto>();
    dataProvider.addDataDisplay(variableConflictsDetails);
    populateVariableChangesTable(conflicts, dataProvider, variableConflictsPager);
  }

  private void addVariablesTab(JsArray<VariableDto> variables, HorizontalTabLayout variableChangesPanel, String tabTitle) {
    CellTable<VariableDto> variablesDetails = new TableCompareVariablesTable();
    SimplePager variableDetailsPager = prepareVariableChangesTab(variableChangesPanel, tabTitle, variablesDetails);

    JsArrayDataProvider<VariableDto> dataProvider = new JsArrayDataProvider<VariableDto>();
    dataProvider.addDataDisplay(variablesDetails);
    populateVariableChangesTable(variables, dataProvider, variableDetailsPager);
  }

  private <T extends JavaScriptObject> SimplePager prepareVariableChangesTab(HorizontalTabLayout variableChangesTabPanel, String tabTitle, CellTable<T> variableChangesTable) {
    variableChangesTable.addStyleName("variableChangesDetails");
    ScrollPanel variableChangesDetails = new ScrollPanel();
    VerticalPanel variableChangesDetailsVert = new VerticalPanel();
    variableChangesDetailsVert.setWidth("100%");
    SimplePager pager = new SimplePager();
    pager.setDisplay(variableChangesTable);
    variableChangesDetailsVert.add(initVariableChangesPager(variableChangesTable, pager));
    variableChangesDetailsVert.add(variableChangesTable);
    variableChangesDetailsVert.addStyleName("variableChangesDetailsVert");
    variableChangesDetails.add(variableChangesDetailsVert);
    variableChangesTabPanel.add(variableChangesDetails, tabTitle);
    return pager;
  }

  FlowPanel initVariableChangesPager(CellTable<? extends JavaScriptObject> table, SimplePager pager) {
    table.setPageSize(20);
    FlowPanel pagerPanel = new FlowPanel();
    pagerPanel.addStyleName("variableChangesPager");
    pagerPanel.add(pager);
    return pagerPanel;
  }

  private <T extends JavaScriptObject> void populateVariableChangesTable(final JsArray<T> conflicts, JsArrayDataProvider<T> dataProvider, SimplePager pager) {
    dataProvider.setArray(conflicts);
    pager.firstPage();
    dataProvider.refresh();
  }
}
