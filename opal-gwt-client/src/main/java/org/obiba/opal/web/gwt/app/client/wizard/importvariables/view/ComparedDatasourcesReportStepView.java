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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.BreadCrumbTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;

import com.google.common.collect.ImmutableList;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
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

  //
  // Instance Variables
  //

  @UiField
  CheckBox ignoreAllModifications;

  @UiField
  HTMLPanel help;

  @UiField
  BreadCrumbTabLayout tableTabs;

  @UiField(provided = true)
  Table<TableComparison> tableList;

  private List<TableComparison> tableComparisons = new ArrayList<TableComparison>();

  private ListDataProvider<TableComparison> tableComparisonsProvider;

  //
  // Constructors
  //

  public ComparedDatasourcesReportStepView() {
    initTableList();
    initWidget(uiBinder.createAndBindUi(this));

    ignoreAllModifications.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        tableList.redraw();
      }
    });
  }

  private void initTableList() {
    TableComparisonsTable table = new TableComparisonsTable() {

      @Override
      protected List<TableComparison> getTableComparisons() {
        return tableComparisons;
      }
    };
    table.setPageSize(100);
    tableComparisonsProvider = new ListDataProvider<TableComparison>(tableComparisons);
    tableComparisonsProvider.addDataDisplay(table);
    table.setEmptyTableWidget(table.getLoadingIndicator());

    SelectionModel<TableComparison> selectionModel = new MultiSelectionModel<TableComparison>(new ProvidesKey<TableComparison>() {

      @Override
      public Object getKey(TableComparison item) {
        return item.getTableName();
      }
    });
    table.setSelectionModel(selectionModel);

    table.getTableNameColumn().setFieldUpdater(new FieldUpdater<ComparedDatasourcesReportStepView.TableComparison, String>() {

      @Override
      public void update(int index, TableComparison object, String value) {
        tableTabs.addAndSelect(new TableComparePanel(object.getTableCompareDto()), object.getTableName());
      }

    });

    this.tableList = table;
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

  class TableComparison {
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
