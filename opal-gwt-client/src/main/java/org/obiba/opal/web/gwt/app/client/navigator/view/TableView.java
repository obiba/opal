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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.MenuItemAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.TabAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;

public class TableView extends ViewImpl implements TablePresenter.Display {

  @UiTemplate("TableView.ui.xml")
  interface TableViewUiBinder extends UiBinder<Widget, TableView> {}

  private static final TableViewUiBinder uiBinder = GWT.create(TableViewUiBinder.class);

  private static final Integer VALUES_TAB_INDEX = 1;

  private static final Integer PERMISSIONS_TAB_INDEX = 2;

  private final Widget widget;

  private final List<Anchor> tables = new ArrayList<Anchor>();

  private boolean hasLinkAuthorization = true;

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
  FlowPanel fromTable;

  @UiField
  FlowPanel fromTableLinks;

  @UiField
  Label fromTableLabel;

  @UiField
  FlowPanel indexStatus;

  @UiField
  Label indexStatusText;

  @UiField
  Alert indexStatusAlert;

  @UiField
  Anchor clearIndexLink;

  @UiField
  Anchor indexNowLink;

  @UiField
  Anchor scheduleLink;

  @UiField
  Anchor cancelLink;

  @UiField
  ProgressBar progress;

  @UiField
  InlineLabel noVariables;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  Anchor copyVariables;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  Anchor selectAllAnchor;

  @UiField
  Anchor clearSelectionAnchor;

  @UiField
  Table<VariableDto> table;

  @UiField
  Panel values;

  @UiField
  SimplePager pager;

  @UiField
  TextBox filter;

  @UiField
  Panel permissions;

  private final ListDataProvider<VariableDto> dataProvider = new ListDataProvider<VariableDto>();

  private VariableClickableColumn variableNameColumn;

  private VariableClickableColumn variableIndexColumn;

  private final Translations translations = GWT.create(Translations.class);

  private MenuItem removeItem;

  private MenuItem addVariablesToViewItem;

  private MenuItemSeparator removeItemSeparator;

  private CheckboxColumn<VariableDto> checkColumn;

  public TableView() {
    widget = uiBinder.createAndBindUi(this);
    toolbarPanel.add(toolbar = new NavigatorMenuBar());
    filter.addStyleName("variables-filter-box");
    addTableColumns();
    initializeAnchorTexts();
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
    addCheckColumn();

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

    table.setPageSize(NavigatorView.PAGE_SIZE);
    table.setEmptyTableWidget(noVariables);
    table.getColumnSortList().push(new ColumnSortInfo(variableIndexColumn, true));
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);

    filter.setText("");
    filter.setPlaceholder(translations.filterVariables());
  }

  @SuppressWarnings({ "unchecked" })
  private void addCheckColumn() {
    checkColumn = new CheckboxColumn<VariableDto>(new VariableDtoDisplay());
    checkColumn.setActionHandler(new ActionHandler<Integer>() {
      @Override
      public void doAction(Integer object, String actionName) {
        selectAllItemsAlert.setVisible(object > 0);
      }
    });
//    checkColumn.setCellStyleNames("checkbox-column");

    table.addColumn(checkColumn, checkColumn.getTableListCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Unit.PX);
  }

  @Override
  public void beforeRenderRows() {
    pager.setVisible(false);
//    variableNameSuggestBox.getSuggestOracle().clear();
//    variableNameSuggestBox.setText("");
    table.setEmptyTableWidget(table.getLoadingIndicator());

  }

  @Override
  public void afterRenderRows() {
    boolean enableItem = dataProvider.getList().size() > 0;
    pager.setVisible(dataProvider.getList().size() > NavigatorView.PAGE_SIZE);
    toolbar.setExportVariableDictionaryItemEnabled(enableItem);
    toolbar.setExportDataItemEnabled(enableItem);
    toolbar.setCopyDataItemEnabled(enableItem);
    table.setEmptyTableWidget(noVariables);
  }

  @Override
  public void renderRows(JsArray<VariableDto> rows) {
    addVariablesToViewItem.setEnabled(rows.length() > 0);
    dataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    pager.firstPage();
    dataProvider.refresh();
  }

  @Override
  public void setVariableSelection(VariableDto variable, int index) {
    int pageIndex = index / table.getPageSize();
    if(pageIndex != pager.getPage()) {
      pager.setPage(pageIndex);
    }
    table.getSelectionModel().setSelected(variable, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void clear() {
    renderRows((JsArray<VariableDto>) JavaScriptObject.createArray());
    checkColumn.getSelectionModel().clear();
    filter.setText("");
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
  public void setFromTables(JsArrayString tableNames) {
    if(tableNames == null) {
      fromTable.setVisible(false);
    } else {
      fromTable.setVisible(true);
      fromTableLinks.clear();
      tables.clear();
      for(int i = 0; i < tableNames.length(); i++) {
        Anchor a = new Anchor();
        a.setText(tableNames.get(i));
        fromTableLinks.add(a);

        tables.add(a);

        if(i < tableNames.length() - 1) {
          Label l = new Label(", ");
          l.addStyleName("inline");
          fromTableLinks.add(l);
        }
      }
    }
  }

  @Override
  public List<Anchor> getFromTablesAnchor() {
    return tables;
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
  public void setAddVariablesToViewCommand(Command cmd) {

    if(addVariablesToViewItem != null) {
      // toolbar.getToolsMenu().removeSeparator(removeItemSeparator);
      toolbar.getToolsMenu().removeItem(addVariablesToViewItem);
    }

    if(cmd != null) {
      addVariablesToViewItem = toolbar.getToolsMenu().addItem(new MenuItem(translations.addVariablesToView(), cmd));
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
  public HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler) {
    return table.addColumnSortHandler(handler);
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
    return new MenuItemAuthorizer(toolbar.getExportVariableDictionaryItem());
  }

  @Override
  public HasAuthorization getViewDownloadAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getViewDownloadItem());
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
  public HasAuthorization getTableIndexStatusAuthorizer() {
    return new UIObjectAuthorizer(indexStatus);
  }

  @Override
  public HasAuthorization getTableIndexEditAuthorizer() {
    return new CompositeAuthorizer(new UIObjectAuthorizer(indexStatusAlert), new UIObjectAuthorizer(clearIndexLink),
        new UIObjectAuthorizer(indexNowLink), new UIObjectAuthorizer(scheduleLink),
        new UIObjectAuthorizer(cancelLink)) {
      @Override
      public void authorized() {
        super.authorized();
        hasLinkAuthorization = true;
      }

      @Override
      public void unauthorized() {
        super.unauthorized();    //To change body of overridden methods use File | Settings | File Templates.
        hasLinkAuthorization = false;
      }
    };
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
        if(event.getSelectedItem().equals(VALUES_TAB_INDEX)) {
          cmd.execute();
        }
      }
    });
  }

  @Override
  public boolean isValuesTabSelected() {
    return tabs.getSelectedIndex() == VALUES_TAB_INDEX;
  }

  @Override
  public void setIndexStatusVisible(boolean b) {
    indexStatus.setVisible(b);
  }

  @SuppressWarnings({ "IfStatementWithTooManyBranches" })
  @Override
  public void setIndexStatusAlert(TableIndexStatusDto statusDto) {

    if(statusDto.getStatus().getName().equals(TableIndexationStatus.UPTODATE.getName())) {
      setStatusText(translations.indexAlertUpToDate(), AlertType.SUCCESS, true, false, true, false, false);
      setProgressBar(false, 0);
    } else if(statusDto.getStatus().getName().equals(TableIndexationStatus.OUTDATED.getName())) {
      setStatusText(translations.indexStatusOutOfDate(), AlertType.ERROR, false, true, true, false, false);
      setProgressBar(false, 0);
    } else if(statusDto.getStatus().getName().equals(TableIndexationStatus.IN_PROGRESS.getName())) {
      setStatusText(translations.indexStatusInProgress(), AlertType.INFO, false, false, false, true, true);
      setProgressBar(true, (int) (statusDto.getProgress() * 100));
    } else if(statusDto.getStatus().getName().equals(TableIndexationStatus.NOT_INDEXED.getName())) {
      setStatusText(translations.indexStatusNotIndexed(), AlertType.WARNING, false, true, true, false, false);
      setProgressBar(false, 0);
    }
  }

  @SuppressWarnings("PMD.ExcessiveParameterList")
  private void setStatusText(String text, AlertType type, boolean clear, boolean indexNow, boolean schedule,
      boolean cancel, boolean progressBar) {
    indexStatusText.setText(text);
    indexStatusAlert.setType(type);

    if(hasLinkAuthorization) {
      clearIndexLink.setVisible(clear);
      indexNowLink.setVisible(indexNow);
      scheduleLink.setVisible(schedule);
      cancelLink.setVisible(cancel);
      progress.setVisible(progressBar);
    }
  }

  private void setProgressBar(boolean progressBar, int percent) {
    if(progressBar) {
      progress.setVisible(true);
      progress.setType(ProgressBar.Style.ANIMATED);
      progress.setPercent(percent);
      progress.setTitle(percent + "%");
    } else {
      progress.setVisible(false);
    }
  }

  private void initializeAnchorTexts() {
    clearIndexLink.setText(translations.indexActionClear());
    indexNowLink.setText(translations.indexActionIndexNow());
    scheduleLink.setText(translations.indexActionScheduleIndexing());
  }

  @Override
  public HasClickHandlers getClear() {
    return clearIndexLink;
  }

  @Override
  public HasClickHandlers getCancel() {
    return cancelLink;
  }

  @Override
  public HasClickHandlers getIndexNow() {
    return indexNowLink;
  }

  @Override
  public HasClickHandlers getScheduleIndexing() {
    return scheduleLink;
  }

  @Override
  public HasClickHandlers getCopyVariables() {
    return copyVariables;
  }

  @Override
  public List<VariableDto> getSelectedItems() {
    return checkColumn.getSelectedItems();
  }

  private class VariableDtoDisplay implements CheckboxColumn.Display<VariableDto> {
    @Override
    public Table<VariableDto> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(VariableDto item) {
      return item.getName();
    }

    @Override
    public Anchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public Anchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public ListDataProvider<VariableDto> getDataProvider() {
      return dataProvider;
    }

    @Override
    public Label getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public String getItemNamePlural() {
      return translations.variablesLabel().toLowerCase();
    }

    @Override
    public String getItemNameSingular() {
      return translations.variableLabel().toLowerCase();
    }
  }

  @Override
  public HandlerRegistration addFilterVariableHandler(KeyUpHandler handler) {
    return filter.addKeyUpHandler(handler);
  }

  @Override
  public HasText getFilter() {
    return filter;
  }

}
