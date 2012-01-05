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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.MenuItemAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.TabAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.gwt.user.cellview.client.ClickableColumn;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.gwtplatform.mvp.client.ViewImpl;

public class DatasourceView extends ViewImpl implements DatasourcePresenter.Display {

  @UiTemplate("DatasourceView.ui.xml")
  interface DatasourceViewUiBinder extends UiBinder<Widget, DatasourceView> {
  }

  private static DatasourceViewUiBinder uiBinder = GWT.create(DatasourceViewUiBinder.class);

  private final Widget widget;

  @UiField
  Label datasourceName;

  @UiField
  Label datasourceType;

  @UiField
  InlineLabel noTables;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  CellTable<TableDto> table;

  @UiField
  SimplePager pager;

  @UiField
  FlowPanel toolbarPanel;

  @UiField
  Panel permissions;

  private NavigatorMenuBar toolbar;

  private MenuItem removeMenuItem;

  private MenuItemSeparator removeMenuItemSeparator;

  private ListDataProvider<TableDto> dataProvider = new ListDataProvider<TableDto>();

  private ClickableColumn<TableDto> tableNameColumn;

  private Translations translations = GWT.create(Translations.class);

  public DatasourceView() {
    widget = uiBinder.createAndBindUi(this);
    toolbarPanel.add(toolbar = new NavigatorMenuBar());
    toolbar.setParentName(null);
    addTableColumns();
  }

  private void addTableColumns() {

    table.addColumn(tableNameColumn = new ClickableColumn<TableDto>() {

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

    dataProvider.addDataDisplay(table);
    table.setSelectionModel(new SingleSelectionModel<TableDto>());
    table.setPageSize(NavigatorView.PAGE_SIZE);
    table.setEmptyTableWidget(noTables);
    pager.setDisplay(table);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTableSelection(TableDto tableDto, int index) {
    int pageIndex = (int) (index / table.getPageSize());
    if(pageIndex != pager.getPage()) {
      pager.setPage(pageIndex);
    }
    table.getSelectionModel().setSelected(tableDto, true);
  }

  @Override
  public void beforeRenderRows() {
    pager.setVisible(false);
    table.setEmptyTableWidget(table.getLoadingIndicator());
  }

  @Override
  public void afterRenderRows() {
    pager.setVisible(table.getRowCount() > NavigatorView.PAGE_SIZE);
    toolbar.setExportDataItemEnabled(table.getRowCount() > 0);
    table.setEmptyTableWidget(noTables);
  }

  @Override
  public void renderRows(final JsArray<TableDto> rows) {
    dataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    pager.firstPage();
    dataProvider.refresh();
  }

  @Override
  public void setDatasource(DatasourceDto dto) {
    datasourceName.setText(dto.getName());
    datasourceType.setText(translations.datasourceTypeMap().get(dto.getType()));
    toolbar.getAddItem().setVisible(false);
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
    removeMenuItemSeparator = toolbar.getToolsMenu().addSeparator();
    removeMenuItem = toolbar.getToolsMenu().addItem(new MenuItem(translations.removeLabel(), cmd));
  }

  @Override
  public void setAddViewCommand(Command cmd) {
    toolbar.setAddViewCommand(cmd);
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

  @Override
  public HasAuthorization getAddUpdateTablesAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getAddUpdateTablesItem()) {

      @Override
      public void authorized() {
        super.authorized();
        toolbar.getAddItem().setVisible(true);
      }

    };
  }

  @Override
  public HasAuthorization getAddViewAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getAddViewItem()) {

      @Override
      public void authorized() {
        super.authorized();
        toolbar.getAddItem().setVisible(true);
      }

    };
  }

  @Override
  public HasAuthorization getExportDataAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getExportDataItem());
  }

  @Override
  public HasAuthorization getCopyDataAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getCopyDataItem());
  }

  @Override
  public HasAuthorization getRemoveDatasourceAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(removeMenuItem), new UIObjectAuthorizer(removeMenuItemSeparator));
  }

  @Override
  public HasAuthorization getExcelDownloadAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getExcelDownloadItem());
  }

  @Override
  public void setPermissionsDisplay(AuthorizationPresenter.Display display) {
    display.setExplanation(translations.datasourcePermissions());
    permissions.clear();
    permissions.add(display.asWidget());
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabAuthorizer(tabs, 1);
  }

}
