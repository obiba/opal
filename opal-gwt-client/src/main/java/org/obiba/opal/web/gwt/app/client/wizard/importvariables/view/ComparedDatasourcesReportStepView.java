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
import com.google.gwt.user.client.ui.Label;
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

  private SimplePager<VariableDto> variableDetailsPager;

  private SimplePager<ConflictDto> variableConflictsPager;

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

    JsArray<VariableDto> newVariables = (JsArray<VariableDto>) (tableCompareData.getNewVariablesArray() != null ? tableCompareData.getNewVariablesArray() : JsArray.createArray());
    JsArray<VariableDto> modifiedVariables = (JsArray<VariableDto>) (tableCompareData.getExistingVariablesArray() != null ? tableCompareData.getExistingVariablesArray() : JsArray.createArray());
    JsArray<ConflictDto> conflicts = (JsArray<ConflictDto>) (tableCompareData.getConflictsArray() != null ? tableCompareData.getConflictsArray() : JsArray.createArray());

    addVariableChangesSummary(tableComparePanel, newVariables, modifiedVariables);

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

  private void addVariableChangesSummary(FlowPanel tableComparePanel, JsArray<VariableDto> newVariables, JsArray<VariableDto> modifiedVariables) {
    FlowPanel variableChangesSummaryPanel = new FlowPanel();
    variableChangesSummaryPanel.setStyleName("variableChangesSummaryPanel");
    variableChangesSummaryPanel.add(new Label(translations.newVariablesLabel() + ": " + String.valueOf(newVariables.length())));
    variableChangesSummaryPanel.add(new Label(translations.modifiedVariablesLabel() + ": " + String.valueOf(modifiedVariables.length())));
    tableComparePanel.add(variableChangesSummaryPanel);
  }

  private void addConflictsTab(JsArray<ConflictDto> conflicts, TabLayoutPanel variableChangesPanel) {
    CellTable<ConflictDto> variableConflictsDetails = initVariableConflictsTable();
    variableConflictsDetails.setStyleName("variableChangesDetails");
    ScrollPanel conflictsPanel = new ScrollPanel();
    VerticalPanel conflictsPanelVert = new VerticalPanel();
    conflictsPanelVert.setWidth("100%");
    conflictsPanelVert.add(initVariableConflictsPager(variableConflictsDetails));
    conflictsPanelVert.add(variableConflictsDetails);
    conflictsPanelVert.setStyleName("variableConflictsDetailsVert");
    conflictsPanel.add(conflictsPanelVert);
    variableChangesPanel.add(conflictsPanel, translations.conflictedVariablesLabel());
    populateConflictsTable(conflicts, variableConflictsDetails);
  }

  private void addVariablesTab(JsArray<VariableDto> variables, TabLayoutPanel variableChangesPanel, String tabTitle) {
    CellTable<VariableDto> variablesDetails = initVariableDetailsTable();
    variablesDetails.setStyleName("variableChangesDetails");
    ScrollPanel variablePanel = new ScrollPanel();
    VerticalPanel variablePanelVert = new VerticalPanel();
    variablePanelVert.setWidth("100%");
    variablePanelVert.add(initVariableDetailsPager(variablesDetails));
    variablePanelVert.add(variablesDetails);
    variablePanelVert.setStyleName("variableChangesDetailsVert");
    variablePanel.add(variablePanelVert);
    variableChangesPanel.add(variablePanel, tabTitle);
    populateVariableDetailsTable(variables, variablesDetails);
  }

  private void populateVariableDetailsTable(final JsArray<VariableDto> variables, CellTable<VariableDto> table) {
    table.setDelegate(new Delegate<VariableDto>() {

      @Override
      public void onRangeChanged(ListView<VariableDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(variables, start, length));
      }

    });

    variableDetailsPager.firstPage();
    table.setDataSize(variables.length(), true);
    table.setData(0, table.getPageSize(), JsArrays.toList(variables, 0, table.getPageSize()));
    table.redraw();
  }

  private void populateConflictsTable(final JsArray<ConflictDto> conflicts, CellTable<ConflictDto> table) {
    table.setDelegate(new Delegate<ConflictDto>() {

      @Override
      public void onRangeChanged(ListView<ConflictDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(conflicts, start, length));
      }

    });

    variableConflictsPager.firstPage();
    table.setDataSize(conflicts.length(), true);
    table.setData(0, table.getPageSize(), JsArrays.toList(conflicts, 0, table.getPageSize()));
    table.redraw();
  }

  private CellTable<ConflictDto> initVariableConflictsTable() {
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

  private CellTable<VariableDto> initVariableDetailsTable() {

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
      @SuppressWarnings("unchecked")
      @Override
      public String getValue(VariableDto variable) {
        JsArray<AttributeDto> attributes = (JsArray<AttributeDto>) (variable.getAttributesArray() != null ? variable.getAttributesArray() : JsArray.createArray());
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
        labels.append(attribute.getLocale().toString() + ":");
        labels.append(attribute != null ? attribute.getValue() : "");
        if(i < attributes.length() - 1) {
          labels.append(", ");
        }
      }
    }, translations.labelLabel());

    return table;
  }

  FlowPanel initVariableDetailsPager(CellTable<VariableDto> table) {
    table.setPageSize(20);
    variableDetailsPager = new SimplePager<VariableDto>(table);
    table.setPager(variableDetailsPager);
    FlowPanel pagerPanel = new FlowPanel();
    pagerPanel.setStyleName("variableDetailsPager");
    pagerPanel.add(variableDetailsPager);
    return pagerPanel;
  }

  FlowPanel initVariableConflictsPager(CellTable<ConflictDto> table) {
    table.setPageSize(20);
    variableConflictsPager = new SimplePager<ConflictDto>(table);
    table.setPager(variableConflictsPager);
    FlowPanel pagerPanel = new FlowPanel();
    pagerPanel.setStyleName("variableConflictsPager");
    pagerPanel.add(variableConflictsPager);
    return pagerPanel;
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
