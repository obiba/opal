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
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
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
  HorizontalTabLayout tableTabs;

  @UiField
  CellTable<TableComparision> tableList;

  private List<TableComparision> tableComparisions = new ArrayList<TableComparision>();

  private ListDataProvider<TableComparision> tableComparisionsProvider;

  //
  // Constructors
  //

  public ComparedDatasourcesReportStepView() {
    initWidget(uiBinder.createAndBindUi(this));

    tableTabs.addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem().intValue() == 0) {
          while(tableTabs.getWidgetCount() > 1) {
            tableTabs.remove(tableTabs.getWidgetCount() - 1);
          }
        }
      }
    });

    initTablesColumns();
  }

  private void initTablesColumns() {
    tableList.setPageSize(100);
    tableComparisionsProvider = new ListDataProvider<TableComparision>(tableComparisions);
    tableComparisionsProvider.addDataDisplay(tableList);
    tableList.setEmptyTableWidget(tableList.getLoadingIndicator());

    final SelectionModel<TableComparision> selectionModel = new MultiSelectionModel<TableComparision>(new ProvidesKey<TableComparision>() {

      @Override
      public Object getKey(TableComparision item) {
        return item.getTableName();
      }
    });
    tableList.setSelectionModel(selectionModel);

    tableList.addColumn(new Column<TableComparision, Boolean>(new CheckboxCell(true, true)) {

      @Override
      public Boolean getValue(TableComparision object) {
        // Get the value from the selection model.
        return selectionModel.isSelected(object);
      }
    }, new Header<Boolean>(new CheckboxCell(true, true)) {

      @Override
      public Boolean getValue() {
        if(tableComparisions.size() == 0) return false;
        boolean allSelected = true;
        for(TableComparision tc : tableComparisions) {
          if(selectionModel.isSelected(tc) == false) {
            return false;
          }
        }
        return allSelected;
      }
    });

    ClickableColumn<TableComparision> tableNameColumn;
    tableList.addColumn(tableNameColumn = new ClickableColumn<TableComparision>() {

      @Override
      public String getValue(TableComparision object) {
        return object.getTableName();
      }
    }, "Table");
    tableNameColumn.setFieldUpdater(new FieldUpdater<ComparedDatasourcesReportStepView.TableComparision, String>() {

      @Override
      public void update(int index, TableComparision object, String value) {
        while(tableTabs.getWidgetCount() > 1) {
          tableTabs.remove(tableTabs.getWidgetCount() - 1);
        }
        tableTabs.add(getTableCompareTabContent(object.getTableCompareDto()), object.getTableName());
        tableTabs.selectTab(1);
      }
    });

    tableList.addColumn(new Column<TableComparision, String>(new TextCell() {
      @Override
      public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {

        if(value != null) {
          TableComparision tc = (TableComparision) context.getKey();
          sb.appendHtmlConstant("<span class=\"" + tc.getStatusStyle() + "\" title=\"" + value.asString() + "\">").appendHtmlConstant("</span>");
        }
      }
    }) {

      @Override
      public String getValue(TableComparision object) {
        return object.getStatus();
      }
    }, "Status");

    tableList.addColumn(new TextColumn<TableComparision>() {

      @Override
      public String getValue(TableComparision object) {
        return Integer.toString(object.getUnmodifiedVariablesCount());
      }
    }, "Unmodified Variables");

    tableList.addColumn(new TextColumn<TableComparision>() {

      @Override
      public String getValue(TableComparision object) {
        return Integer.toString(object.getNewVariablesCount());
      }
    }, "New Variables");

    tableList.addColumn(new TextColumn<TableComparision>() {

      @Override
      public String getValue(TableComparision object) {
        return Integer.toString(object.getModifiedVariablesCount());
      }
    }, "Modified Variables");

    tableList.addColumn(new TextColumn<TableComparision>() {

      @Override
      public String getValue(TableComparision object) {
        return Integer.toString(object.getConflictsCount());
      }
    }, "Conflicts");
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  @Override
  public void clearDisplay() {
    ignoreAllModifications.setValue(false);
    ignoreAllModifications.setEnabled(false);
    tableComparisions.clear();
    tableComparisionsProvider.refresh();
  }

  @Override
  public void addTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    TableComparision tc;
    tableComparisions.add(tc = new TableComparision(tableCompareData, comparisonResult));
    tableComparisionsProvider.refresh();
    tableList.setVisible(true);
    // tableList.getSelectionModel().setSelected(tc, true);
  }

  @Override
  public void addForbiddenTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    TableComparision tc;
    tableComparisions.add(tc = new TableComparision(tableCompareData, comparisonResult, true));
    tableComparisionsProvider.refresh();
    tableList.setVisible(true);
    // tableList.getSelectionModel().setSelected(tc, false);
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
    String tableName = tableCompareData.getCompared().getName();
    Anchor tabHeader = new Anchor(tableName);
    if(comparisonResult == ComparisonResult.CONFLICT) {
      tabHeader.addStyleName("iconb i-alert");
    } else if(comparisonResult == ComparisonResult.CREATION) {
      tabHeader.addStyleName("iconb i-plus");
    } else if(comparisonResult == ComparisonResult.MODIFICATION) {
      tabHeader.addStyleName("iconb i-reblog");
    } else {
      tabHeader.addStyleName("iconb i-done");
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

  private static class TableComparision {
    private final TableCompareDto dto;

    private final ComparisonResult result;

    private final boolean forbidden;

    public TableComparision(TableCompareDto dto, ComparisonResult result) {
      this(dto, result, false);
    }

    public TableComparision(TableCompareDto dto, ComparisonResult result, boolean forbidden) {
      super();
      this.dto = dto;
      this.result = result;
      this.forbidden = forbidden;
    }

    public String getTableName() {
      return dto.getCompared().getName();
    }

    public String getStatus() {
      return forbidden ? "FORBIDDEN" : result.toString();
    }

    public String getStatusStyle() {
      if(forbidden) {
        return "iconb i-disapprove";
      } else if(result == ComparisonResult.CONFLICT) {
        return "iconb i-alert";
      } else if(result == ComparisonResult.CREATION) {
        return "iconb i-plus";
      } else if(result == ComparisonResult.MODIFICATION) {
        return "iconb i-reblog";
      } else {
        return "iconb i-done";
      }
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
  }

}
