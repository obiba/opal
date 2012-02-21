/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.gwt.app.client.workbench.view.VerticalTabLayout;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ComparedDatasourcesReportStepView extends Composite implements ComparedDatasourcesReportStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  VerticalTabLayout tableChangesPanel;

  @UiField
  CheckBox ignoreAllModifications;

  @UiField
  HTMLPanel help;

  @UiField
  Image refreshPending;

  //
  // Constructors
  //

  public ComparedDatasourcesReportStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  @Override
  public void clearDisplay() {
    refreshPending.setVisible(true);
    tableChangesPanel.clear();
    tableChangesPanel.setVisible(false);
    ignoreAllModifications.setValue(false);
    ignoreAllModifications.setEnabled(false);
  }

  @Override
  public void addTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    tableChangesPanel.add(getTableCompareTabContent(tableCompareData), getTableCompareTabHeader(tableCompareData, comparisonResult));
    tableChangesPanel.setVisible(true);
    refreshPending.setVisible(false);
  }

  @Override
  public void addForbiddenTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    Anchor tabHeader = getTableCompareTabHeader(tableCompareData, comparisonResult);
    tabHeader.addStyleName("icon-before i-disapprove");
    tableChangesPanel.add(getTableCompareTabContent(tableCompareData), tabHeader);
    tableChangesPanel.setVisible(true);
    refreshPending.setVisible(false);
  }

  @Override
  public boolean ignoreAllModifications() {
    return ignoreAllModifications.getValue();
  }

  @Override
  public void setIgnoreAllModificationsVisible(boolean visible) {
    ignoreAllModifications.setVisible(visible);
  }

  @Override
  public void setIgnoreAllModificationsEnabled(boolean enabled) {
    ignoreAllModifications.setEnabled(enabled);
  }

  private FlowPanel getTableCompareTabContent(TableCompareDto tableCompareData) {
    FlowPanel tableComparePanel = new FlowPanel();

    Label tableName = new Label(tableCompareData.getCompared().getName());
    tableName.addStyleName("title");
    tableComparePanel.add(tableName);

    HorizontalTabLayout variableChangesPanel = initVariableChangesPanel(tableCompareData, tableComparePanel);

    tableComparePanel.add(variableChangesPanel);

    return tableComparePanel;
  }

  private Anchor getTableCompareTabHeader(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    Anchor tabHeader = new Anchor(tableCompareData.getCompared().getName());
    if(comparisonResult == ComparisonResult.CONFLICT) {
      tabHeader.addStyleName("icon-before i-alert");
    } else if(comparisonResult == ComparisonResult.CREATION) {
      tabHeader.addStyleName("icon-before i-plus");
    } else if(comparisonResult == ComparisonResult.MODIFICATION) {
      tabHeader.addStyleName("icon-before i-reblog");
    } else {
      tabHeader.addStyleName("icon-before i-done");
    }
    return tabHeader;
  }

  @SuppressWarnings("unchecked")
  private HorizontalTabLayout initVariableChangesPanel(TableCompareDto tableCompareData, FlowPanel tableComparePanel) {
    HorizontalTabLayout variableChangesPanel = new HorizontalTabLayout();

    JsArray<VariableDto> newVariables = JsArrays.toSafeArray(tableCompareData.getNewVariablesArray());
    JsArray<VariableDto> unmodifiedVariables = JsArrays.toSafeArray(tableCompareData.getUnmodifiedVariablesArray());
    JsArray<VariableDto> modifiedVariables = JsArrays.toSafeArray(tableCompareData.getModifiedVariablesArray());
    JsArray<ConflictDto> conflicts = JsArrays.toSafeArray(tableCompareData.getConflictsArray());
    addVariableChangesSummary(tableComparePanel, newVariables, modifiedVariables, conflicts);

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

  private void addVariableChangesSummary(FlowPanel tableComparePanel, JsArray<VariableDto> newVariables, JsArray<VariableDto> modifiedVariables, JsArray<ConflictDto> conflicts) {
    FlowPanel variableChangesSummaryPanel = new FlowPanel();
    variableChangesSummaryPanel.addStyleName("variableChangesSummaryPanel");
    int conflictsCount[] = getConflictsCounts(conflicts);
    variableChangesSummaryPanel.add(new HTML(getNewVariablesCountLabel(newVariables, conflictsCount)));
    variableChangesSummaryPanel.add(new HTML(getModifiedVariablesCountLabel(modifiedVariables, conflictsCount)));
    tableComparePanel.add(variableChangesSummaryPanel);
  }

  private String getModifiedVariablesCountLabel(JsArray<VariableDto> modifiedVariables, int[] conflictsCount) {
    return translations.modifiedVariablesLabel() + ": " + String.valueOf(modifiedVariables.length() + conflictsCount[1]) + (conflictsCount[1] > 0 ? " <em>(" + conflictsCount[1] + " " + translations.conflictedVariablesLabel() + ")</em>" : "");
  }

  private String getNewVariablesCountLabel(JsArray<VariableDto> newVariables, int[] conflictsCount) {
    return translations.newVariablesLabel() + ": " + String.valueOf(newVariables.length() + conflictsCount[0]) + (conflictsCount[0] > 0 ? " <em>(" + conflictsCount[0] + " " + translations.conflictedVariablesLabel() + ")</em>" : "");
  }

  private int[] getConflictsCounts(JsArray<ConflictDto> conflicts) {
    int conflictsCount[] = { 0, 0 };
    for(int i = 0; i < conflicts.length(); i++) {

      GWT.log("conflict " + conflicts.get(i).getVariable().getIsNewVariable());
      if(conflicts.get(i).getVariable().getIsNewVariable()) {
        conflictsCount[0]++;
      } else {
        conflictsCount[1]++;
      }
    }
    return conflictsCount;
  }

  private void addConflictsTab(JsArray<ConflictDto> conflicts, HorizontalTabLayout variableChangesPanel) {
    CellTable<ConflictDto> variableConflictsDetails = setupColumnsForConflicts();
    SimplePager variableConflictsPager = prepareVariableChangesTab(variableChangesPanel, translations.conflictedVariablesLabel(), variableConflictsDetails);

    JsArrayDataProvider<ConflictDto> dataProvider = new JsArrayDataProvider<ConflictDto>();
    dataProvider.addDataDisplay(variableConflictsDetails);
    populateVariableChangesTable(conflicts, dataProvider, variableConflictsPager);
  }

  private void addVariablesTab(JsArray<VariableDto> variables, HorizontalTabLayout variableChangesPanel, String tabTitle) {
    CellTable<VariableDto> variablesDetails = setupColumnsForVariables();
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
    table.setPageSize(18);
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

  private CellTable<ConflictDto> setupColumnsForConflicts() {
    CellTable<ConflictDto> table = new Table<ConflictDto>();
    table.addColumn(new TextColumn<ConflictDto>() {
      @Override
      public String getValue(ConflictDto conflict) {
        return conflict.getVariable().getName();
      }
    }, translations.nameLabel());
    table.addColumn(new TextColumn<ConflictDto>() {
      @Override
      public String getValue(ConflictDto conflict) {

        StringBuilder builder = new StringBuilder();
        JsArrayString arguments = (JsArrayString) (conflict.getArgumentsArray() != null ? conflict.getArgumentsArray() : JsArrayString.createArray());
        for(int i = 0; i < arguments.length(); i++) {
          builder.append(arguments.get(i).toString() + " ");
        }

        return translations.datasourceComparisonErrorMap().get(conflict.getCode()) + ", " + builder.toString();
      }
    }, translations.messageLabel());

    return table;
  }

  private CellTable<VariableDto> setupColumnsForVariables() {

    CellTable<VariableDto> table = new Table<VariableDto>();
    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto variable) {
        return variable.getName();
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto variable) {
        return variable.getValueType();
      }
    }, translations.valueTypeLabel());

    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto variable) {
        return variable.getUnit();
      }
    }, translations.unitLabel());

    table.addColumn(new TextColumn<VariableDto>() {

      @Override
      public String getValue(VariableDto variable) {
        return getVariableLabels(variable);
      }

      private String getVariableLabels(VariableDto variable) {
        JsArray<AttributeDto> attributes = JsArrays.toSafeArray(variable.getAttributesArray());
        AttributeDto attribute = null;
        StringBuilder labels = new StringBuilder();

        for(int i = 0; i < attributes.length(); i++) {
          attribute = attributes.get(i);
          if(attribute.getName().equals("label")) {
            appendLabel(attribute, labels);
          }
        }

        return labels.toString();
      }

      private void appendLabel(AttributeDto attr, StringBuilder labels) {
        if(attr.hasValue() && attr.getValue().trim().length() > 0) {
          if(labels.length() > 0) {
            labels.append(", ");
          }
          if(attr.hasLocale() && attr.getLocale().trim().length() > 0) {
            labels.append("(").append(attr.getLocale()).append(") ");
          }
          labels.append(attr.getValue());
        }
      }
    }, translations.labelLabel());

    return table;
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

  @UiTemplate("ComparedDatasourcesReportStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ComparedDatasourcesReportStepView> {
  }

  @Override
  public Widget getStepHelp() {
    return help;
  }

}
