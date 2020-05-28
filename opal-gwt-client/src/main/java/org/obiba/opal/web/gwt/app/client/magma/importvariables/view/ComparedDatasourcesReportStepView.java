/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importvariables.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.ui.BreadcrumbsTabPanel;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.collect.ImmutableList;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class ComparedDatasourcesReportStepView extends ViewImpl
    implements ComparedDatasourcesReportStepPresenter.Display {

  private final Translations translations;

  @UiField
  CheckBox ignoreAllModifications;

  @UiField
  BreadcrumbsTabPanel tableTabs;

  private TableComparisonsTable tableList;

  private List<TableComparison> tableComparisons = new ArrayList<TableComparison>();

  private ListDataProvider<TableComparison> tableComparisonsProvider;

  //
  // Constructors
  //

  @Inject
  public ComparedDatasourcesReportStepView(Binder uiBinder, Translations translations) {
    this.translations = translations;
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
    tableList = new TableComparisonsTable() {

      @Override
      protected List<TableComparison> getTableComparisons() {
        return tableComparisons;
      }
    };
    tableList.setPageSize(100);
    tableComparisonsProvider = new ListDataProvider<TableComparison>(tableComparisons);
    tableComparisonsProvider.addDataDisplay(tableList);
    tableList.setEmptyTableWidget(tableList.getLoadingIndicator());

    SelectionModel<TableComparison> selectionModel = new MultiSelectionModel<TableComparison>(
        new ProvidesKey<TableComparison>() {

          @Override
          public Object getKey(TableComparison item) {
            return item.getTableName();
          }
        });
    tableList.setSelectionModel(selectionModel);
    tableList.getTableNameColumn().setFieldUpdater(new TableComparisonFieldUpdater());

    tableTabs.add(tableList, translations.tablesLabel());
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  @Override
  public List<String> getSelectedTables() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
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
    tableComparisonsProvider.setList(tableComparisons);
    tableComparisonsProvider.refresh();
    tableTabs.selectTab(0);
  }

  @Override
  public void addTableComparison(TableCompareDto tableCompareData, ComparisonResult comparisonResult) {
    TableComparison tableComparison = new TableComparison(tableCompareData, comparisonResult);
    tableComparisons.add(tableComparison);
    tableComparisonsProvider.refresh();
    tableList.getSelectionModel().setSelected(tableComparison, tableComparison.isSelectable());
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

  @Override
  public boolean isIgnoreAllModificationsVisible() {
    return ignoreAllModifications.isVisible();
  }

  //
  // Inner Classes / Interfaces
  //

  private final class TableComparisonFieldUpdater implements FieldUpdater<TableComparison, String> {
    @Override
    public void update(int index, TableComparison object, String value) {
      TableComparePanel panel = new TableComparePanel(object.getTableCompareDto(),
          new TableCompareVariableFieldUpdater(), new TableCompareConflictFieldUpdater());
      tableTabs.addAndSelect(panel, object.getTableName());
    }
  }

  private final class TableCompareVariableFieldUpdater implements FieldUpdater<VariableDto, String> {

    @Override
    public void update(int index, VariableDto object, String value) {
      tableTabs.addAndSelect(new TableCompareVariablePanel(object), object.getName());
    }

  }

  private final class TableCompareConflictFieldUpdater implements FieldUpdater<ConflictDto, String> {

    @Override
    public void update(int index, ConflictDto object, String value) {
      tableTabs.addAndSelect(new TableCompareVariablePanel(object.getVariable()), object.getVariable().getName());
    }

  }

  interface Binder extends UiBinder<Widget, ComparedDatasourcesReportStepView> {}

  class TableComparison {
    private final TableCompareDto dto;

    private final ComparisonResult result;

    TableComparison(TableCompareDto dto, ComparisonResult result) {
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
      return result != ComparisonResult.FORBIDDEN &&
          (ignoreAllModifications.getValue() || result != ComparisonResult.CONFLICT);
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
