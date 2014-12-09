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

import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourceUiHandlers;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.TabPanelAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class DatasourceView extends ViewWithUiHandlers<DatasourceUiHandlers> implements DatasourcePresenter.Display {

  private static final int PERMISSIONS_TAB_INDEX = 1;

  private static final int SORTABLE_COLUMN_NAME = 1;

  private static final int SORTABLE_COLUMN_VARIABLES = 3;

  private static final int SORTABLE_COLUMN_ENTITIES = 4;

  private static final int SORTABLE_COLUMN_LAST_UPDATED = 5;

  interface Binder extends UiBinder<Widget, DatasourceView> {}

  @UiField
  NavLink downloadDictionary;

  @UiField
  Button importData;

  @UiField
  Button exportData;

  @UiField
  Button copyData;

  @UiField
  TabPanel tabPanel;

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
  OpalSimplePager pager;

  @UiField
  TextBoxClearable filter;

  @UiField
  Panel permissionsPanel;

  private final ListDataProvider<TableDto> dataProvider = new ListDataProvider<TableDto>();

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final PlaceManager placeManager;

  private CheckboxColumn<TableDto> checkColumn;

  private ColumnSortEvent.ListHandler<TableDto> typeSortHandler;

  @Inject
  public DatasourceView(Binder uiBinder, Translations translations, TranslationMessages translationMessages,
      PlaceManager placeManager) {
    this.translationMessages = translationMessages;
    this.translations = translations;
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));

    addBtn.setText(translations.addTable());
    addTableColumns();
    initializeFilter();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    permissionsPanel.clear();
    if(content != null) {
      permissionsPanel.add(content);
    }
  }

  private void initializeFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterTables());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
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

  @UiHandler("exportSelectionAnchor")
  void onExportSelection(ClickEvent event) {
    getUiHandlers().onExportData();
  }

  @UiHandler("copySelectionAnchor")
  void onCopy(ClickEvent event) {
    getUiHandlers().onCopyData();
  }

  @UiHandler("filter")
  void onFilterUpdate(KeyUpEvent event) {
    getUiHandlers().onTablesFilterUpdate(filter.getText());
  }

  private void addTableColumns() {
    initializeColumns();
    dataProvider.addDataDisplay(table);
    initializeSortableColumns();
    table.setSelectionModel(new SingleSelectionModel<TableDto>());
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new InlineLabel(translationMessages.tableCount(0)));
    pager.setDisplay(table);
  }

  private void initializeColumns() {
    checkColumn = new CheckboxColumn<TableDto>(new DatasourceCheckStatusDisplay());
    table.addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Style.Unit.PX);
    table.addColumn(new NameColumn(), translations.nameLabel());
    table.addColumn(new TextColumn<TableDto>() {

      @Override
      public String getValue(TableDto object) {
        return object.getEntityType();
      }
    }, translations.entityTypeColumnLabel());
    table.addColumn(new VariablesColumn(), translations.variablesLabel());
    table.addColumn(new EntitiesColumn(), translations.entitiesLabel());
    table.addColumn(new LastUpdatedColumn(), translations.lastUpdatedLabel());
  }

  private void initializeSortableColumns() {
    typeSortHandler = new ColumnSortEvent.ListHandler<TableDto>(dataProvider.getList());
    typeSortHandler.setComparator(table.getColumn(SORTABLE_COLUMN_NAME), new NameComparator());
    typeSortHandler.setComparator(table.getColumn(SORTABLE_COLUMN_VARIABLES), new VariablesComparator());
    typeSortHandler.setComparator(table.getColumn(SORTABLE_COLUMN_ENTITIES), new EntitiesComparator());
    typeSortHandler.setComparator(table.getColumn(SORTABLE_COLUMN_LAST_UPDATED), new LastUpdateComparator());

    table.getHeader(SORTABLE_COLUMN_NAME).setHeaderStyleNames("sortable-header-column");
    table.getHeader(SORTABLE_COLUMN_VARIABLES).setHeaderStyleNames("sortable-header-column");
    table.getHeader(SORTABLE_COLUMN_ENTITIES).setHeaderStyleNames("sortable-header-column");
    table.getHeader(SORTABLE_COLUMN_LAST_UPDATED).setHeaderStyleNames("sortable-header-column");
    table.getColumnSortList().push(table.getColumn(SORTABLE_COLUMN_NAME));
    table.getColumnSortList().push(table.getColumn(SORTABLE_COLUMN_VARIABLES));
    table.getColumnSortList().push(table.getColumn(SORTABLE_COLUMN_ENTITIES));
    table.getColumnSortList().push(table.getColumn(SORTABLE_COLUMN_LAST_UPDATED));
    table.addColumnSortHandler(typeSortHandler);
  }

  @Override
  public void beforeRenderRows() {
    pager.setPagerVisible(false);
    table.showLoadingIndicator(dataProvider);
    initializeFilter();
  }

  @Override
  public void afterRenderRows() {
    dataProvider.refresh();
    boolean enableItem = table.getRowCount() > 0;
    pager.setPagerVisible(table.getRowCount() > Table.DEFAULT_PAGESIZE);
    downloadDictionary.setDisabled(!enableItem);
    exportData.setEnabled(enableItem);
    copyData.setEnabled(enableItem);
    table.hideLoadingIndicator();
  }

  @Override
  public void renderRows(JsArray<TableDto> rows) {
    dataProvider.setList(JsArrays.toList(rows));
    typeSortHandler.setList(dataProvider.getList());
    ColumnSortEvent.fire(table, table.getColumnSortList());
    pager.firstPage();
  }

  @Override
  public void setDatasource(DatasourceDto dto) {
    addBtn.setVisible(false);
    boolean isNull = "null".equals(dto.getType());
    importData.setEnabled(!isNull);
    addTable.setDisabled(isNull);
    addUpdateTables.setDisabled(isNull);

    if(isNull) addTable.setText(translations.addTableNoStorageLabel());
    else addTable.setText(translations.addTableLabel());

    if(isNull) addUpdateTables.setText(translations.addUpdateTablesNoStorageLabel());
    else addUpdateTables.setText(translations.addUpdateTablesLabel());

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
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, PERMISSIONS_TAB_INDEX);
  }

  @Override
  public List<TableDto> getSelectedTables() {
    return checkColumn.getSelectedItems();
  }

  @Override
  public List<TableDto> getAllTables() {
    return dataProvider.getList();
  }

  private static class LastUpdatedColumn extends TextColumn<TableDto> {

    private LastUpdatedColumn() {
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(TableDto object) {
      return object.hasTimestamps() && object.getTimestamps().hasLastUpdate() //
          ? Moment.create(object.getTimestamps().getLastUpdate()).fromNow() //
          : "";
    }
  }

  private static class VariablesColumn extends TextColumn<TableDto> {

    private VariablesColumn() {
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(TableDto object) {
      if(object.hasVariableCount()) return Integer.toString(object.getVariableCount());
      return "-";
    }
  }

  private static class EntitiesColumn extends TextColumn<TableDto> {

    private EntitiesColumn() {
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(TableDto object) {
      if(object.hasValueSetCount()) return Integer.toString(object.getValueSetCount());
      return "-";
    }
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
    public String getNItemLabel(int nb) {
      return translationMessages.nTablesLabel(nb).toLowerCase();
    }

    @Override
    public Alert getAlert() {
      return selectAllItemsAlert;
    }
  }

  private static final class NameComparator implements Comparator<TableDto> {
    @Override
    public int compare(TableDto o1, TableDto o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  private static final class VariablesComparator implements Comparator<TableDto> {
    @Override
    public int compare(TableDto o1, TableDto o2) {
      int c1 = o1.hasVariableCount() ? o1.getVariableCount() : 0;
      int c2 = o2.hasVariableCount() ? o2.getVariableCount() : 0;
      return c1 - c2;
    }
  }

  private static final class EntitiesComparator implements Comparator<TableDto> {
    @Override
    public int compare(TableDto o1, TableDto o2) {
      int c1 = o1.hasValueSetCount() ? o1.getValueSetCount() : 0;
      int c2 = o2.hasValueSetCount() ? o2.getValueSetCount() : 0;
      return c1 - c2;
    }
  }

  private static final class LastUpdateComparator implements Comparator<TableDto> {
    @Override
    public int compare(TableDto o1, TableDto o2) {
      Moment m1 = Moment.create(o1.getTimestamps().getLastUpdate());
      Moment m2 = Moment.create(o2.getTimestamps().getLastUpdate());
      if(m1 == null) {
        return m2 == null ? 0 : 1;
      }
      return m2 == null ? -1 : m2.unix() - m1.unix();
    }
  }

  private class NameColumn extends Column<TableDto, TableDto> {
    public NameColumn() {
      super(new PlaceRequestCell<TableDto>(DatasourceView.this.placeManager) {

        @Override
        public PlaceRequest getPlaceRequest(TableDto value) {
          return ProjectPlacesHelper.getTablePlace(value.getDatasourceName(), value.getName());
        }

        @Override
        public String getText(TableDto value) {
          String name = value.getName();
          return value.hasViewLink()
              ? "<i class=\"icon-th-large\"></i>&nbsp;" + name
              : "<i class=\"icon-table\"></i>&nbsp;" + name;
        }
      });
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public TableDto getValue(TableDto object) {
      return object;
    }
  }
}
