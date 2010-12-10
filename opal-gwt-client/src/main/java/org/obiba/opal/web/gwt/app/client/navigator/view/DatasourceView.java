/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.ListView.Delegate;

public class DatasourceView extends Composite implements DatasourcePresenter.Display {
  @UiTemplate("DatasourceView.ui.xml")
  interface DatasourceViewUiBinder extends UiBinder<Widget, DatasourceView> {
  }

  private static DatasourceViewUiBinder uiBinder = GWT.create(DatasourceViewUiBinder.class);

  @UiField
  Label datasourceName;

  @UiField
  Label datasourceType;

  @UiField
  InlineLabel noTables;

  @UiField
  CellTable<TableDto> table;

  SelectionModel<TableDto> selectionModel = new SingleSelectionModel<TableDto>();

  SimplePager<TableDto> pager;

  @UiField
  FlowPanel toolbarPanel;

  private NavigatorMenuBar toolbar;

  @UiField
  Image loading;

  private TableNameColumn tableNameColumn;

  private Translations translations = GWT.create(Translations.class);

  public DatasourceView() {
    initWidget(uiBinder.createAndBindUi(this));
    toolbarPanel.add(toolbar = new NavigatorMenuBar());
    toolbar.setParentName(null);
    addTableColumns();
  }

  private void addTableColumns() {

    table.addColumn(tableNameColumn = new TableNameColumn() {

      @Override
      public String getValue(TableDto object) {
        return object.getName();
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
    }, translations.entitiesCountLabel());

    table.setSelectionEnabled(true);
    table.setSelectionModel(selectionModel);
    table.setPageSize(50);
    pager = new SimplePager<TableDto>(table);
    table.setPager(pager);
    VerticalPanel p = ((VerticalPanel) table.getParent());
    p.insert(pager, 0);
    DOM.removeElementAttribute(pager.getElement(), "style");
    DOM.setStyleAttribute(pager.getElement(), "cssFloat", "right");
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setTableSelection(TableDto tableDto, int index) {
    int pageIndex = (int) (index / table.getPageSize());
    if(pageIndex != pager.getPage()) {
      pager.setPage(pageIndex);
    }
    selectionModel.setSelected(tableDto, true);
    // int row = index - table.getPageSize() * pageIndex;
    // System.out.println("row=" + row);
    // table.getRowElement(row).addClassName("selected");
  }

  @Override
  public void beforeRenderRows() {
    pager.setVisible(false);
    table.setVisible(false);
    noTables.setVisible(false);
    loading.setVisible(true);
  }

  @Override
  public void afterRenderRows() {
    pager.setVisible(table.getDataSize() > 0);
    table.setVisible(table.getDataSize() > 0);
    toolbar.setExportDataItemVisible(table.getDataSize() > 0);
    noTables.setVisible(table.getDataSize() == 0);
    loading.setVisible(false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void renderRows(final JsArray<TableDto> rows) {
    final JsArray<TableDto> tableRows = (rows != null) ? rows : (JsArray<TableDto>) JsArray.createArray();

    table.setDelegate(new Delegate<TableDto>() {

      @Override
      public void onRangeChanged(ListView<TableDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(tableRows, start, length));
      }

    });
    pager.firstPage();
    table.setData(0, table.getPageSize(), JsArrays.toList(tableRows, 0, table.getPageSize()));
    table.setDataSize(tableRows.length(), true);
    table.redraw();
  }

  @Override
  public void setDatasource(DatasourceDto dto) {
    datasourceName.setText(dto.getName());
    datasourceType.setText(translations.datasourceTypeMap().get(dto.getType()));
  }

  @Override
  public void setNextName(String name) {
    toolbar.setNextName(name);
  }

  @Override
  public void setPreviousName(String name) {
    toolbar.setPreviousName(name);
  }

  @Override
  public void setNextCommand(Command cmd) {
    toolbar.setNextCommand(cmd);
  }

  @Override
  public void setPreviousCommand(Command cmd) {
    toolbar.setPreviousCommand(cmd);
  }

  @Override
  public void setExcelDownloadCommand(Command cmd) {
    toolbar.setExcelDownloadCommand(cmd);
  }

  @Override
  public void setRemoveDatasourceCommand(Command cmd) {
    toolbar.getToolsMenu().addSeparator();
    toolbar.getToolsMenu().addItem(new MenuItem(translations.removeLabel(), cmd));
  }

  @Override
  public void setAddViewCommand(Command cmd) {
    toolbar.setAddViewCommand(cmd);
  }

  private abstract class TableNameColumn extends Column<TableDto, String> implements HasFieldUpdater<TableDto, String> {
    public TableNameColumn() {
      super(new ClickableTextCell());
    }
  }

  @Override
  public void setTableNameFieldUpdater(FieldUpdater<TableDto, String> updater) {
    tableNameColumn.setFieldUpdater(updater);
  }

  @Override
  public void setExportDataCommand(Command cmd) {
    toolbar.setExportDataCommand(cmd);
  }

  @Override
  public void setCopyDataCommand(Command cmd) {
    toolbar.setCopyDataCommand(cmd);
  }

  @Override
  public void setAddUpdateTablesCommand(Command cmd) {
    toolbar.setAddUpdateTablesCommand(cmd);
  }

}
