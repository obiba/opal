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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.BreadCrumbTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.collect.ImmutableList;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

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
  CheckBox ignoreAllModifications;

  @UiField
  HTMLPanel help;

  @UiField
  BreadCrumbTabLayout tableTabs;

  @UiField
  CellTable<TableComparison> tableList;

  private List<TableComparison> tableComparisons = new ArrayList<TableComparison>();

  private ListDataProvider<TableComparison> tableComparisonsProvider;

  //
  // Constructors
  //

  public ComparedDatasourcesReportStepView() {
    initWidget(uiBinder.createAndBindUi(this));

    initTableList();

    ignoreAllModifications.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        tableList.redraw();
      }
    });
  }

  private void initTableList() {
    tableList.setPageSize(100);
    tableComparisonsProvider = new ListDataProvider<TableComparison>(tableComparisons);
    tableComparisonsProvider.addDataDisplay(tableList);
    tableList.setEmptyTableWidget(tableList.getLoadingIndicator());

    SelectionModel<TableComparison> selectionModel = new MultiSelectionModel<TableComparison>(new ProvidesKey<TableComparison>() {

      @Override
      public Object getKey(TableComparison item) {
        return item.getTableName();
      }
    });
    tableList.setSelectionModel(selectionModel);

    initTableListColumns();
  }

  private void initTableListColumns() {
    initTableListCheckColumn();
    initTableListTableNameColumn();
    initTableListCountColumns();
  }

  private void initTableListCountColumns() {
    tableList.addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        return Integer.toString(object.getUnmodifiedVariablesCount());
      }
    }, translations.unmodifiedVariablesLabel());

    tableList.addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        int conflicts = object.getNewVariablesConflictsCount();
        if(conflicts > 0) {
          return Integer.toString(object.getNewVariablesCount()) + " (" + conflicts + ")";
        } else {
          return Integer.toString(object.getNewVariablesCount());
        }
      }
    }, translations.newVariablesLabel());

    tableList.addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        int conflicts = object.getModifiedVariablesConflictsCount();
        if(conflicts > 0) {
          return Integer.toString(object.getModifiedVariablesCount()) + " (" + conflicts + ")";
        } else {
          return Integer.toString(object.getModifiedVariablesCount());
        }
      }
    }, translations.modifiedVariablesLabel());

    tableList.addColumn(new TextColumn<TableComparison>() {

      @Override
      public String getValue(TableComparison object) {
        return Integer.toString(object.getConflictsCount());
      }
    }, translations.conflictedVariablesLabel());
  }

  private void initTableListCheckColumn() {
    Column<TableComparison, Boolean> checkColumn = new Column<TableComparison, Boolean>(new CheckboxCell(true, true) {
      @Override
      public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        // check if forbidden or has conflict
        TableComparison tc = (TableComparison) context.getKey();
        if(tc.isSelectable()) {
          super.render(context, value, sb);
        } else {
          sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" disabled=\"true\" tabindex=\"-1\"/>"));
        }
      }
    }) {

      @Override
      public Boolean getValue(TableComparison object) {
        // Get the value from the selection model.
        return tableList.getSelectionModel().isSelected(object);
      }

    };
    checkColumn.setFieldUpdater(new FieldUpdater<ComparedDatasourcesReportStepView.TableComparison, Boolean>() {

      @Override
      public void update(int index, TableComparison object, Boolean value) {
        tableList.getSelectionModel().setSelected(object, value);
      }
    });

    tableList.addColumn(checkColumn, createTableListCheckColumnHeader());
  }

  private Header<Boolean> createTableListCheckColumnHeader() {
    Header<Boolean> checkHeader = new Header<Boolean>(new CheckboxCell(true, true)) {

      @Override
      public Boolean getValue() {
        if(tableComparisons.size() == 0) return false;
        boolean allSelected = true;
        for(TableComparison tc : tableComparisons) {
          if(tc.isSelectable() && tableList.getSelectionModel().isSelected(tc) == false) {
            return false;
          }
        }
        return allSelected;
      }
    };
    checkHeader.setUpdater(new ValueUpdater<Boolean>() {

      @Override
      public void update(Boolean value) {
        for(TableComparison tc : tableComparisons) {
          if(tc.isSelectable()) {
            tableList.getSelectionModel().setSelected(tc, value);
          }
        }
      }
    });
    return checkHeader;
  }

  private void initTableListTableNameColumn() {
    Column<TableComparison, String> tableNameColumn;
    tableList.addColumn(tableNameColumn = new Column<TableComparison, String>(new ClickableTextCell() {
      @Override
      public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if(value != null) {
          TableComparison tc = (TableComparison) context.getKey();
          sb.appendHtmlConstant("<a class=\"" + tc.getStatusStyle() + "\" title=\"" + translations.comparisonResultMap().get(tc.getStatus()) + "\">").append(value).appendHtmlConstant("</a>");
        }
      }
    }) {

      @Override
      public String getValue(TableComparison object) {
        return object.getTableName();
      }
    }, translations.tableLabel());
    tableNameColumn.setFieldUpdater(new FieldUpdater<ComparedDatasourcesReportStepView.TableComparison, String>() {

      @Override
      public void update(int index, TableComparison object, String value) {
        tableTabs.addAndSelect(getTableCompareTabContent(object.getTableCompareDto()), object.getTableName());
      }
    });
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  @Override
  public List<String> getSelectedTables() {
    ImmutableList.Builder<String> builder = ImmutableList.<String> builder();
    for(TableComparison tc : tableComparisons) {
      if(tableList.getSelectionModel().isSelected(tc)) {
        builder.add(tc.getTableName());
      }
    }
    return builder.build();
  }

  @Override
  public void clearDisplay() {
    ignoreAllModifications.setValue(false);
    ignoreAllModifications.setEnabled(false);
    tableComparisons.clear();
    tableComparisonsProvider.refresh();
  }

  @Override
  public void addTableComparison(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    tableComparisons.add(new TableComparison(tableCompareData, comparisonResult));
    tableComparisonsProvider.refresh();
    tableList.setVisible(true);
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
    HorizontalTabLayout variableChangesPanel = initVariableChangesPanel(tableCompareData, tableComparePanel);
    tableComparePanel.add(variableChangesPanel);
    return tableComparePanel;
  }

  @SuppressWarnings("unchecked")
  private HorizontalTabLayout initVariableChangesPanel(TableCompareDto tableCompareData, FlowPanel tableComparePanel) {
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

    table.addColumn(new VariableAttributeColumn("label"), translations.labelLabel());

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

  private class TableComparison {
    private final TableCompareDto dto;

    private final ComparisonResult result;

    public TableComparison(TableCompareDto dto, ComparisonResult result) {
      super();
      this.dto = dto;
      this.result = result;
    }

    public String getTableName() {
      return dto.getCompared().getName();
    }

    public String getStatus() {
      return result.toString();
    }

    public String getStatusStyle() {
      switch(result) {
      case FORBIDDEN:
        return "iconb i-disapprove";
      case CONFLICT:
        return "iconb i-alert";
      case CREATION:
        return "iconb i-plus";
      case MODIFICATION:
        return "iconb i-reblog";
      default:
        return "iconb i-done";
      }
    }

    public boolean isSelectable() {
      return result != ComparisonResult.FORBIDDEN && (ignoreAllModifications.getValue() || result != ComparisonResult.CONFLICT);
    }

    public TableCompareDto getTableCompareDto() {
      return dto;
    }

    public int getNewVariablesCount() {
      return JsArrays.toSafeArray(dto.getNewVariablesArray()).length();
    }

    public int getModifiedVariablesCount() {
      return JsArrays.toSafeArray(dto.getModifiedVariablesArray()).length();
    }

    public int getUnmodifiedVariablesCount() {
      return JsArrays.toSafeArray(dto.getUnmodifiedVariablesArray()).length();
    }

    public int getConflictsCount() {
      return JsArrays.toSafeArray(dto.getConflictsArray()).length();
    }

    public int getNewVariablesConflictsCount() {
      return getConflictsCounts(JsArrays.toSafeArray(dto.getConflictsArray()))[0];
    }

    public int getModifiedVariablesConflictsCount() {
      return getConflictsCounts(JsArrays.toSafeArray(dto.getConflictsArray()))[1];
    }

    private int[] getConflictsCounts(JsArray<ConflictDto> conflicts) {
      int conflictsCount[] = { 0, 0 };
      for(int i = 0; i < conflicts.length(); i++) {
        if(conflicts.get(i).getVariable().getIsNewVariable()) {
          conflictsCount[0]++;
        } else {
          conflictsCount[1]++;
        }
      }
      return conflictsCount;
    }
  }

}
