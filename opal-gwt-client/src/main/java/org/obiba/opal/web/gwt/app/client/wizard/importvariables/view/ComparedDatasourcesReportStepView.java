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
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.ListView.Delegate;

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
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TabLayoutPanel tableChangesPanel;

  @UiField
  CheckBox ignoreAllModifications;

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
  public HandlerRegistration addSaveClickHandler(ClickHandler handler) {
    return saveButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return cancelButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addIgnoreAllModificationsHandler(ClickHandler handler) {
    return ignoreAllModifications.addClickHandler(handler);
  }

  @Override
  public void clearDisplay() {
    tableChangesPanel.clear();
    saveButton.setEnabled(false);
    ignoreAllModifications.setValue(false);
    ignoreAllModifications.setEnabled(false);
  }

  @Override
  public void addTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {

    FlowPanel tableComparePanel = new FlowPanel();

    TabLayoutPanel variableChangesPanel = initVariableChangesPanel(tableCompareData, tableComparePanel);

    tableComparePanel.add(variableChangesPanel);
    tableChangesPanel.add(tableComparePanel, getTableCompareTabHeader(tableCompareData, comparisonResult));
    tableChangesPanel.setHeight("730px");
  }

  @Override
  public void setEnabledSaveButton(boolean enabled) {
    saveButton.setEnabled(enabled);
  }

  @Override
  public boolean ignoreAllModifications() {
    return ignoreAllModifications.getValue();
  }

  @Override
  public void setEnabledIgnoreAllModifications(boolean enabled) {
    ignoreAllModifications.setEnabled(enabled);
  }

  private FlowPanel getTableCompareTabHeader(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    FlowPanel tabHeader = new FlowPanel();
    if(comparisonResult == ComparisonResult.CONFLICT) {
      tabHeader.addStyleName("tableComparison-conflict");
    } else if(comparisonResult == ComparisonResult.CREATION) {
      tabHeader.addStyleName("tableComparison-creation");
    } else {
      tabHeader.addStyleName("tableComparison-modification");
    }

    tabHeader.add(new HTML(tableCompareData.getCompared().getName()));
    return tabHeader;
  }

  @SuppressWarnings("unchecked")
  private TabLayoutPanel initVariableChangesPanel(TableCompareDto tableCompareData, FlowPanel tableComparePanel) {
    TabLayoutPanel variableChangesPanel = new TabLayoutPanel(3, Unit.EM);
    variableChangesPanel.setStyleName("variableChanges");

    JsArray<VariableDto> newVariables = getNullAsEmptyArray(tableCompareData.getNewVariablesArray());
    JsArray<VariableDto> modifiedVariables = getNullAsEmptyArray(tableCompareData.getExistingVariablesArray());
    JsArray<ConflictDto> conflicts = getNullAsEmptyArray(tableCompareData.getConflictsArray());
    addVariableChangesSummary(tableComparePanel, newVariables, modifiedVariables, conflicts);

    if(newVariables.length() > 0) {
      addVariablesTab(newVariables, variableChangesPanel, translations.newVariablesLabel());
    }

    if(modifiedVariables.length() > 0) {
      addVariablesTab(modifiedVariables, variableChangesPanel, translations.modifiedVariablesLabel());
    }

    if(conflicts.length() > 0) {
      addConflictsTab(conflicts, variableChangesPanel);
    }
    return variableChangesPanel;
  }

  @SuppressWarnings("unchecked")
  private <T extends JavaScriptObject> JsArray<T> getNullAsEmptyArray(JsArray<T> array) {
    return (JsArray<T>) (array != null ? array : JsArray.createArray());
  }

  private void addVariableChangesSummary(FlowPanel tableComparePanel, JsArray<VariableDto> newVariables, JsArray<VariableDto> modifiedVariables, JsArray<ConflictDto> conflicts) {
    FlowPanel variableChangesSummaryPanel = new FlowPanel();
    variableChangesSummaryPanel.setStyleName("variableChangesSummaryPanel");
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

  private void addConflictsTab(JsArray<ConflictDto> conflicts, TabLayoutPanel variableChangesPanel) {
    CellTable<ConflictDto> variableConflictsDetails = setupColumnsForConflicts();
    SimplePager<ConflictDto> variableConflictsPager = prepareVariableChangesTab(variableChangesPanel, translations.conflictedVariablesLabel(), variableConflictsDetails);
    populateVariableChangesTable(conflicts, variableConflictsDetails, variableConflictsPager);
  }

  private void addVariablesTab(JsArray<VariableDto> variables, TabLayoutPanel variableChangesPanel, String tabTitle) {
    CellTable<VariableDto> variablesDetails = setupColumnsForVariables();
    SimplePager<VariableDto> variableDetailsPager = prepareVariableChangesTab(variableChangesPanel, tabTitle, variablesDetails);
    populateVariableChangesTable(variables, variablesDetails, variableDetailsPager);
  }

  private <T extends JavaScriptObject> SimplePager<T> prepareVariableChangesTab(TabLayoutPanel variableChangesTabPanel, String tabTitle, CellTable<T> variableChangesTable) {
    variableChangesTable.setStyleName("variableChangesDetails");
    ScrollPanel variableChangesDetails = new ScrollPanel();
    VerticalPanel variableChangesDetailsVert = new VerticalPanel();
    variableChangesDetailsVert.setWidth("100%");
    SimplePager<T> pager = new SimplePager<T>(variableChangesTable);
    variableChangesTable.setPager(pager);
    variableChangesDetailsVert.add(initVariableChangesPager(variableChangesTable, pager));
    variableChangesDetailsVert.add(variableChangesTable);
    variableChangesDetailsVert.setStyleName("variableChangesDetailsVert");
    variableChangesDetails.add(variableChangesDetailsVert);
    variableChangesTabPanel.add(variableChangesDetails, tabTitle);
    return pager;
  }

  FlowPanel initVariableChangesPager(CellTable<? extends JavaScriptObject> table, SimplePager<? extends JavaScriptObject> pager) {
    table.setPageSize(18);
    FlowPanel pagerPanel = new FlowPanel();
    pagerPanel.setStyleName("variableChangesPager");
    pagerPanel.add(pager);
    return pagerPanel;
  }

  private <T extends JavaScriptObject> void populateVariableChangesTable(final JsArray<T> conflicts, CellTable<T> table, SimplePager<T> pager) {
    table.setDelegate(new Delegate<T>() {

      @Override
      public void onRangeChanged(ListView<T> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(conflicts, start, length));
      }

    });

    pager.firstPage();
    table.setDataSize(conflicts.length(), true);
    table.setData(0, table.getPageSize(), JsArrays.toList(conflicts, 0, table.getPageSize()));
    table.redraw();
  }

  private CellTable<ConflictDto> setupColumnsForConflicts() {
    CellTable<ConflictDto> table = new CellTable<ConflictDto>();
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

    CellTable<VariableDto> table = new CellTable<VariableDto>();
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
        JsArray<AttributeDto> attributes = getNullAsEmptyArray(variable.getAttributesArray());
        AttributeDto attribute = null;
        StringBuilder labels = new StringBuilder();
        for(int i = 0; i < attributes.length(); i++) {
          attribute = attributes.get(i);
          if(attribute.getName().equals("label")) {
            getLabelsString(attributes, attribute, labels, i);
          }
        }
        return labels.toString();
      }

      private void getLabelsString(JsArray<AttributeDto> attributes, AttributeDto attribute, StringBuilder labels, int i) {
        labels.append(attribute.getLocale().toString() + ":" + attribute.getValue());
        if(i < attributes.length() - 1) {
          labels.append(", ");
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

}
