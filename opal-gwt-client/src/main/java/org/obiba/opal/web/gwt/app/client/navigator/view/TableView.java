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
import org.obiba.opal.web.gwt.app.client.navigator.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter.Display;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.DefaultSuggestBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.MenuItemAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.TabAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.gwtplatform.mvp.client.ViewImpl;

public class TableView extends ViewImpl implements TablePresenter.Display {

  @UiTemplate("TableView.ui.xml")
  interface TableViewUiBinder extends UiBinder<Widget, TableView> {
  }

  private static final TableViewUiBinder uiBinder = GWT.create(TableViewUiBinder.class);

  private static final Integer VALUES_TAB_INDEX = 1;

  private static final Integer PERMISSIONS_TAB_INDEX = 2;

  private final Widget widget;

  @UiField
  FlowPanel toolbarPanel;

  private final NavigatorMenuBar toolbar;

  @UiField
  Label tableName;

  @UiField
  Label tableOrView;

  @UiField
  Label entityType;

  @UiField
  Label entityCount;

  @UiField
  InlineLabel noVariables;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  CellTable<VariableDto> table;

  @UiField
  Panel values;

  @UiField
  SimplePager pager;

  @UiField
  DefaultSuggestBox variableNameSuggestBox;

  @UiField
  Panel permissions;

  private final ListDataProvider<VariableDto> dataProvider = new ListDataProvider<VariableDto>();

  private VariableClickableColumn variableNameColumn;

  private VariableClickableColumn variableIndexColumn;

  private final Translations translations = GWT.create(Translations.class);

  private MenuItem removeItem;

  private MenuItem createCodingViewItem;

  private MenuItemSeparator removeItemSeparator;

  public TableView() {
    this.widget = uiBinder.createAndBindUi(this);
    toolbarPanel.add(toolbar = new NavigatorMenuBar());
    addTableColumns();
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    HasWidgets panel = null;
    if(slot == Slots.Permissions) {
      panel = permissions;
    } else if(slot == Slots.Values) {
      panel = values;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content);
      }
    }
  }

  private void addTableColumns() {
    variableIndexColumn = new VariableClickableColumn("index") {
      @Override
      public String getValue(VariableDto object) {
        return object.getIndex() + 1 + "";
      }
    };
    table.addColumn(variableIndexColumn, "#");
    table.setColumnWidth(variableIndexColumn, 1, Unit.PX);
    variableIndexColumn.setSortable(true);

    table.addColumn(variableNameColumn = new VariableClickableColumn("name") {
      @Override
      public String getValue(VariableDto object) {
        return object.getName();
      }
    }, translations.nameLabel());
    variableNameColumn.setSortable(true);

    table.addColumn(new VariableAttributeColumn("label"), translations.labelLabel());

    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto object) {
        return object.getValueType();
      }
    }, translations.valueTypeLabel());

    table.addColumn(new TextColumn<VariableDto>() {

      @Override
      public String getValue(VariableDto object) {
        return object.getUnit();
      }
    }, translations.unitLabel());

    table.setSelectionModel(new SingleSelectionModel<VariableDto>());
    table.setPageSize(NavigatorView.PAGE_SIZE);
    table.setEmptyTableWidget(noVariables);
    table.getColumnSortList().push(new ColumnSortInfo(variableIndexColumn, true));
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);
  }

  @Override
  public void beforeRenderRows() {
    pager.setVisible(false);
    variableNameSuggestBox.getSuggestOracle().clear();
    variableNameSuggestBox.setText("");
    table.setEmptyTableWidget(table.getLoadingIndicator());
  }

  @Override
  public void afterRenderRows() {
    pager.setVisible(dataProvider.getList().size() > NavigatorView.PAGE_SIZE);
    toolbar.setExportDataItemEnabled(dataProvider.getList().size() > 0);
    table.setEmptyTableWidget(noVariables);
  }

  @Override
  public void renderRows(final JsArray<VariableDto> rows) {
    dataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    pager.firstPage();
    dataProvider.refresh();
  }

  @Override
  public void setVariableSelection(VariableDto variable, int index) {
    int pageIndex = (int) (index / table.getPageSize());
    if(pageIndex != pager.getPage()) {
      pager.setPage(pageIndex);
    }
    table.getSelectionModel().setSelected(variable, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void clear() {
    renderRows((JsArray<VariableDto>) JavaScriptObject.createArray());
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTable(TableDto dto) {
    tableName.setText(dto.getName());

    String tableOrViewText = dto.hasViewLink() ? translations.viewLabel() : translations.tableLabel();
    tableOrView.setText("(" + tableOrViewText + ")");

    entityType.setText(dto.getEntityType());

    entityCount.setText(Integer.toString(dto.getValueSetCount()));
  }

  @Override
  public void setExcelDownloadCommand(Command cmd) {
    toolbar.setExcelDownloadCommand(cmd);
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
  public void setViewDownloadCommand(Command cmd) {
    if(cmd != null) {
      toolbar.setViewDownloadCommand(cmd);
    } else {
      toolbar.removeViewDownloadCommand();
    }
  }

  @Override
  public void setParentName(String name) {
    toolbar.setParentName(name);
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
  public void setParentCommand(Command cmd) {
    toolbar.setParentCommand(cmd);
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
  public void setRemoveCommand(Command cmd) {

    if(removeItem != null) {
      toolbar.getToolsMenu().removeSeparator(removeItemSeparator);
      toolbar.getToolsMenu().removeItem(removeItem);
    }

    if(cmd != null) {
      removeItemSeparator = toolbar.getToolsMenu().addSeparator();
      removeItem = toolbar.getToolsMenu().addItem(new MenuItem(translations.removeLabel(), cmd));
    }

  }

  @Override
  public void setCreateCodingViewCommand(Command cmd) {

    if(createCodingViewItem != null) {
      // toolbar.getToolsMenu().removeSeparator(removeItemSeparator);
      toolbar.getToolsMenu().removeItem(createCodingViewItem);
    }

    if(cmd != null) {
      createCodingViewItem = toolbar.getToolsMenu().addItem(new MenuItem(translations.createCodingView(), cmd));
    }

  }

  @Override
  public void setEditCommand(Command cmd) {
    toolbar.setEditCommand(cmd);
  }

  private abstract static class VariableClickableColumn extends ClickableColumn<VariableDto> {

    private final String name;

    private VariableClickableColumn(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  @Override
  public void setVariableNameFieldUpdater(FieldUpdater<VariableDto, String> updater) {
    variableNameColumn.setFieldUpdater(updater);
  }

  @Override
  public void setVariableIndexFieldUpdater(FieldUpdater<VariableDto, String> updater) {
    variableIndexColumn.setFieldUpdater(updater);
  }

  @Override
  public void addVariableSuggestion(String suggestion) {
    variableNameSuggestBox.getSuggestOracle().add(suggestion);
  }

  @Override
  public HandlerRegistration addVariableSuggestionHandler(SelectionHandler<Suggestion> handler) {
    return variableNameSuggestBox.addSelectionHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler) {
    return table.addColumnSortHandler(handler);
  }

  @Override
  public void clearVariableSuggestion() {
    variableNameSuggestBox.setText("");
  }

  @Override
  public HasAuthorization getEditAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getEditItem());
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
  public HasAuthorization getRemoveAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(removeItem), new UIObjectAuthorizer(removeItemSeparator));
  }

  @Override
  public HasAuthorization getExcelDownloadAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getExcelDownloadItem());
  }

  @Override
  public HasAuthorization getViewDownloadAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getViewDownloadItem());
  }

  @Override
  public void setValuesDisplay(Display display) {
    values.add(display.asWidget());
  }

  @Override
  public HasAuthorization getValuesAuthorizer() {
    return new TabAuthorizer(tabs, VALUES_TAB_INDEX);
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabAuthorizer(tabs, PERMISSIONS_TAB_INDEX);
  }

  @Override
  public String getClickableColumnName(Column<?, ?> column) {
    if(column instanceof VariableClickableColumn) {
      return ((VariableClickableColumn) column).getName();
    }
    return null;
  }

  @Override
  public void setValuesTabCommand(final Command cmd) {
    tabs.addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem() == VALUES_TAB_INDEX) {
          cmd.execute();
        }
      }
    });
  }

  @Override
  public boolean isValuesTabSelected() {
    return tabs.getSelectedIndex() == VALUES_TAB_INDEX;
  }

}
