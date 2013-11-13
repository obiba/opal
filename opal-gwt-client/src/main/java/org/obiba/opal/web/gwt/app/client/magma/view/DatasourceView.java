/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourceUiHandlers;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class DatasourceView extends ViewWithUiHandlers<DatasourceUiHandlers> implements DatasourcePresenter.Display {

  interface Binder extends UiBinder<Widget, DatasourceView> {}

  @UiField
  Button downloadDictionary;

  @UiField
  DropdownButton tasksBtn;

  @UiField
  NavLink importData;

  @UiField
  NavLink exportData;

  @UiField
  NavLink copyData;

  @UiField
  DropdownButton addBtn;

  @UiField
  NavLink addTable;

  @UiField
  NavLink addUpdateTables;

  @UiField
  NavLink addView;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  Table<TableDto> table;

  @UiField
  SimplePager pager;

  private final ListDataProvider<TableDto> dataProvider = new ListDataProvider<TableDto>();

  private final Translations translations;

  private final PlaceManager placeManager;

  private CheckboxColumn<TableDto> checkColumn;

  @Inject
  public DatasourceView(Binder uiBinder, Translations translations, PlaceManager placeManager) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    this.placeManager = placeManager;
    addTableColumns();
  }

  @UiHandler("downloadDictionary")
  void onDownloadDictionary(ClickEvent event) {
    getUiHandlers().onDownloadDictionary();
  }

  @UiHandler("importData")
  void onImportData(ClickEvent event) {
    getUiHandlers().onImportData();
  }

  @UiHandler("exportData")
  void onExportData(ClickEvent event) {
    getUiHandlers().onExportData();
  }

  @UiHandler("copyData")
  void onCopyData(ClickEvent event) {
    getUiHandlers().onCopyData();
  }

  @UiHandler("addTable")
  void onAddTable(ClickEvent event) {
    getUiHandlers().onAddTable();
  }

  @UiHandler("addUpdateTables")
  void onAddUpdateTables(ClickEvent event) {
    getUiHandlers().onAddUpdateTables();
  }

  @UiHandler("addView")
  void onAddView(ClickEvent event) {
    getUiHandlers().onAddView();
  }

  @UiHandler("deleteTables")
  void onDeleteTables(ClickEvent event) {
    getUiHandlers().onDeleteTables(checkColumn.getSelectedItems());
  }

  private void addTableColumns() {
    checkColumn = new CheckboxColumn<TableDto>(new DatasourceCheckStatusDisplay());

    table.addColumn(checkColumn, checkColumn.getTableListCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Style.Unit.PX);

    table.addColumn(new Column<TableDto, TableDto>(new PlaceRequestCell<TableDto>(placeManager) {

      @Override
      public PlaceRequest getPlaceRequest(TableDto value) {
        return ProjectPlacesHelper.getTablePlace(value.getDatasourceName(), value.getName());
      }

      @Override
      public String getText(TableDto value) {
        String name = value.getName();
        return value.hasViewLink()
            ? "<i class=\"icon-cog\"></i>&nbsp;" + name
            : "<i class=\"icon-table\"></i>&nbsp;" + name;
      }
    }) {
      @Override
      public TableDto getValue(TableDto object) {
        return object;
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<TableDto>() {

      @Override
      public String getValue(TableDto object) {
        return object.getEntityType();
      }
    }, translations.entityTypeColumnLabel());

    table.addColumn(new TextColumn<TableDto>() {

      @Override
      public String getValue(TableDto object) {
        return Integer.toString(object.getVariableCount());
      }
    }, translations.variablesLabel());

    table.addColumn(new TextColumn<TableDto>() {

      @Override
      public String getValue(TableDto object) {
        return Integer.toString(object.getValueSetCount());
      }
    }, translations.entitiesLabel());

    dataProvider.addDataDisplay(table);
    table.setSelectionModel(new SingleSelectionModel<TableDto>());
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new InlineLabel(translations.noTablesLabel()));
    pager.setDisplay(table);
  }

  @Override
  public void setTableSelection(TableDto tableDto, int index) {
    int pageIndex = index / table.getPageSize();
    if(pageIndex != pager.getPage()) {
      pager.setPage(pageIndex);
    }
    table.getSelectionModel().setSelected(tableDto, true);
  }

  @Override
  public void beforeRenderRows() {
    pager.setVisible(false);
    table.showLoadingIndicator(dataProvider);
  }

  @Override
  public void afterRenderRows() {
    dataProvider.refresh();
    boolean enableItem = table.getRowCount() > 0;
    pager.setVisible(table.getRowCount() > Table.DEFAULT_PAGESIZE);
    downloadDictionary.setEnabled(enableItem);
    exportData.setDisabled(!enableItem);
    copyData.setDisabled(!enableItem);
    table.hideLoadingIndicator();
  }

  @Override
  public void renderRows(JsArray<TableDto> rows) {
    dataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    pager.firstPage();
  }

  @Override
  public void setDatasource(DatasourceDto dto) {
    addBtn.setVisible(false);
    boolean isNull = "null".equals(dto.getType());
    importData.setDisabled(isNull);
    addTable.setDisabled(isNull);
    addUpdateTables.setDisabled(isNull);
    checkColumn.clearSelection();
  }

  @Override
  public HasAuthorization getAddUpdateTablesAuthorizer() {
    return new CompositeAuthorizer(new WidgetAuthorizer(addTable), new WidgetAuthorizer(addUpdateTables)) {
      @Override
      public void authorized() {
        super.authorized();
        addBtn.setVisible(true);
      }
    };
  }

  @Override
  public HasAuthorization getAddViewAuthorizer() {
    return new WidgetAuthorizer(addView) {

      @Override
      public void authorized() {
        super.authorized();
        addBtn.setVisible(true);
      }

    };
  }

  @Override
  public HasAuthorization getImportDataAuthorizer() {
    return new WidgetAuthorizer(importData);
  }

  @Override
  public HasAuthorization getExportDataAuthorizer() {
    return new WidgetAuthorizer(exportData);
  }

  @Override
  public HasAuthorization getCopyDataAuthorizer() {
    return new WidgetAuthorizer(copyData);
  }

  @Override
  public HasAuthorization getExcelDownloadAuthorizer() {
    return new WidgetAuthorizer(downloadDictionary);
  }

  @Override
  public List<TableDto> getSelectedTables() {
    return checkColumn.getSelectedItems();
  }

  @Override
  public List<TableDto> getAllTables() {
    return dataProvider.getList();
  }

  private class DatasourceCheckStatusDisplay implements CheckboxColumn.Display<TableDto> {
    @Override
    public Table<TableDto> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(TableDto item) {
      return item.getName();
    }

    @Override
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public ListDataProvider<TableDto> getDataProvider() {
      return dataProvider;
    }

    @Override
    public Label getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public String getItemNamePlural() {
      return translations.tablesLabel().toLowerCase();
    }

    @Override
    public String getItemNameSingular() {
      return translations.tableLabel().toLowerCase();
    }

    @Override
    public Alert getAlert() {
      return selectAllItemsAlert;
    }
  }
}
