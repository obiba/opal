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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TableUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.TabPanelAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TableView extends ViewWithUiHandlers<TableUiHandlers> implements TablePresenter.Display {

  interface Binder extends UiBinder<Widget, TableView> {}

  private static final Integer VARIABLES_TAB_INDEX = 0;

  private static final Integer VALUES_TAB_INDEX = 2;

  private boolean hasLinkAuthorization = true;

  @UiField
  Label entityType;

  @UiField
  PropertiesTable propertiesTable;

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
  Panel valuesPanel;

  @UiField
  SimplePager pager;

  @UiField
  TextBoxClearable filter;

  @UiField
  TabPanel tabPanel;

  @UiField
  NavLink exportData;

  @UiField
  NavLink copyData;

  @UiField
  NavLink downloadDictionary;

  @UiField
  NavLink downloadView;

  @UiField
  IconAnchor edit;

  @UiField
  Button remove;

  private final ListDataProvider<VariableDto> dataProvider = new ListDataProvider<VariableDto>();

  private VariableClickableColumn variableNameColumn;

  private VariableClickableColumn variableIndexColumn;

  private final Translations translations = GWT.create(Translations.class);

  private CheckboxColumn<VariableDto> checkColumn;

  @Inject
  public TableView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    addTableColumns();
    initializeAnchorTexts();
    initializeFilter();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = null;
    if(slot == Slots.Values) {
      panel = valuesPanel;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content.asWidget());
      }
    }
  }

  private void initializeFilter() {
    filter.getTextBox().setPlaceholder(translations.filterVariables());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
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
        String categories = "";
        int count = 1;
        for (CategoryDto category : JsArrays.toIterable(JsArrays.toSafeArray(object.getCategoriesArray()))) {
          if (count>10) {
            categories = categories + " ...";
            break;
          }
          if (!categories.isEmpty()) {
            categories = categories + ", " + category.getName();
          } else {
            categories = category.getName();
          }
          count++;
        }
        return categories;
      }
    }, translations.categoriesLabel());

    table.setSelectionModel(new SingleSelectionModel<VariableDto>());
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(noVariables);
    table.getColumnSortList().push(new ColumnSortInfo(variableIndexColumn, true));
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);

    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterVariables());
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

    table.addColumn(checkColumn, checkColumn.getTableListCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Unit.PX);
  }

  @Override
  public void beforeRenderRows() {
    pager.setVisible(false);
    table.showLoadingIndicator(dataProvider);
  }

  @Override
  public void afterRenderRows() {
    boolean enableItem = dataProvider.getList().size() > 0;
    pager.setVisible(dataProvider.getList().size() > Table.DEFAULT_PAGESIZE);
    downloadDictionary.setDisabled(!enableItem);
    exportData.setDisabled(!enableItem);
    copyData.setDisabled(!enableItem);
    table.hideLoadingIndicator();
  }

  @Override
  public void renderRows(JsArray<VariableDto> rows) {
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
  public void clear(boolean cleanFilter) {
    renderRows((JsArray<VariableDto>) JavaScriptObject.createArray());
    checkColumn.clearSelection();
    if(cleanFilter) filter.setText("");
  }

  @Override
  public void setTable(TableDto dto) {
    entityType.setText(dto.getEntityType());
    edit.setVisible(dto.hasViewLink());
  }

  @Override
  public void setFromTables(JsArrayString tableNames) {
    if (propertiesTable.getRowCount()>1) {
      propertiesTable.removeProperty(1);
    }
    if(tableNames != null) {
      FlowPanel fromTableLinks = new FlowPanel();
      for(int i = 0; i < tableNames.length(); i++) {
        final String tableFullName = tableNames.get(i);
        Anchor a = new Anchor();
        a.setText(tableFullName);
        a.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            getUiHandlers().onFromTable(tableFullName);
          }
        });
        fromTableLinks.add(a);

        if(i < tableNames.length() - 1) {
          fromTableLinks.add(new InlineLabel(", "));
        }
      }
      propertiesTable.addProperty(new Label(translations.tableReferencesLabel()), fromTableLinks);
    }
  }

  @UiHandler("downloadDictionary")
  void onDownloadDictionary(ClickEvent event) {
    getUiHandlers().onDownloadDictionary();
  }

  @UiHandler("downloadView")
  void onDownloadView(ClickEvent event) {
    getUiHandlers().onDownloadView();
  }

  @UiHandler("remove")
  void onRemove(ClickEvent event) {
    getUiHandlers().onRemove();
  }

  @UiHandler("edit")
  void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("exportData")
  void onExportData(ClickEvent event) {
    getUiHandlers().onExportData();
  }

  @UiHandler("copyData")
  void onCopyData(ClickEvent event) {
    getUiHandlers().onCopyData();
  }

  @UiHandler("copyVariables")
  void onAddVariablesToView(ClickEvent event) {
    getUiHandlers().onAddVariablesToView(checkColumn.getSelectedItems());
  }

  @UiHandler("clearIndexLink")
  void onIndexClear(ClickEvent event) {
    getUiHandlers().onIndexClear();
  }

  @UiHandler("indexNowLink")
  void onIndexNow(ClickEvent event) {
    getUiHandlers().onIndexNow();
  }

  @UiHandler("cancelLink")
  void onIndexCancel(ClickEvent event) {
    getUiHandlers().onIndexCancel();
  }

  @UiHandler("scheduleLink")
  void onIndexSchedule(ClickEvent event) {
    getUiHandlers().onIndexSchedule();
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
    return new WidgetAuthorizer(edit);
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
  public HasAuthorization getRemoveAuthorizer() {
    return new WidgetAuthorizer(remove);
  }

  @Override
  public HasAuthorization getExcelDownloadAuthorizer() {
    return new WidgetAuthorizer(downloadDictionary);
  }

  @Override
  public HasAuthorization getViewDownloadAuthorizer() {
    return new WidgetAuthorizer(downloadView);
  }

  @Override
  public HasAuthorization getValuesAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, VALUES_TAB_INDEX);
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
        super.unauthorized();
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
    tabPanel.addShownHandler(new TabPanel.ShownEvent.Handler() {
      @Override
      public void onShow(TabPanel.ShownEvent shownEvent) {
        if(tabPanel.getSelectedTab() == VALUES_TAB_INDEX) {
          cmd.execute();
        }
      }
    });
  }

  @Override
  public void setVariablesTabCommand(final Command cmd) {
    tabPanel.addShownHandler(new TabPanel.ShownEvent.Handler() {
      @Override
      public void onShow(TabPanel.ShownEvent shownEvent) {
        if(tabPanel.getSelectedTab() == VARIABLES_TAB_INDEX) {
          cmd.execute();
        }
      }
    });
  }

  @Override
  public boolean isValuesTabSelected() {
    return tabPanel.getSelectedTab() == VALUES_TAB_INDEX;
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
    return filter.getTextBox().addKeyUpHandler(handler);
  }

  @Override
  public TextBoxClearable getFilter() {
    return filter;
  }

  @Override
  public void setCancelVisible(boolean b) {
    cancelLink.setVisible(b);
  }
}
