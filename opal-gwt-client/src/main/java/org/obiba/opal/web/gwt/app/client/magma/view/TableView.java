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

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TableUiHandlers;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.ui.CategoricalVariableSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.CrossableVariableSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.app.client.ui.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.datetime.client.Moment;
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
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class TableView extends ViewWithUiHandlers<TableUiHandlers> implements TablePresenter.Display {

  private final EventBus eventBus;

  interface Binder extends UiBinder<Widget, TableView> {}

  private static final int VARIABLES_TAB_INDEX = 0;

  private static final int VALUES_TAB_INDEX = 2;

  private static final int PERMISSIONS_TAB_INDEX = 3;

  @UiField
  Label name;

  @UiField
  Label entityType;

  @UiField
  PropertiesTable propertiesTable;

  @UiField
  Label timestamps;

  @UiField
  Label variableCount;

  @UiField
  Label entityCount;

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
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

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
  Button addVariable;

  @UiField
  Button remove;

  @UiField
  Panel permissionsPanel;

  @UiField(provided = true)
  Typeahead categoricalVariables;

  @UiField
  FlowPanel crossResultsPanel;

  @UiField
  TextBox categoricalVariable;

  @UiField(provided = true)
  Typeahead crossWithVariables;

  @UiField
  TextBox crossWithVariable;

  @UiField
  FluidRow contingencyTablePanel;

  private final ListDataProvider<VariableDto> dataProvider = new ListDataProvider<VariableDto>();

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final PlaceManager placeManager;

  private TableDto tableDto;

  private CheckboxColumn<VariableDto> checkColumn;

  private boolean hasLinkAuthorization = true;

  @Inject
  public TableView(Binder uiBinder, EventBus eventBus, Translations translations,
      TranslationMessages translationMessages, PlaceManager placeManager) {
    this.eventBus = eventBus;
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.placeManager = placeManager;

    categoricalVariables = new Typeahead(new CategoricalVariableSuggestOracle(this.eventBus));
    crossWithVariables = new Typeahead(new CrossableVariableSuggestOracle(this.eventBus));

    initWidget(uiBinder.createAndBindUi(this));
    addTableColumns();
    initializeAnchorTexts();
    initializeFilter();

    progress.setWidth("150px");
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = null;
    if(slot == Slots.Values) {
      panel = valuesPanel;
    } else if(slot == Slots.Permissions) {
      panel = permissionsPanel;
    } else if(slot == Slots.ContingencyTable) {
      panel = crossResultsPanel;
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

    table.addColumn(new VariableColumn(new VariableLinkCell(placeManager)), translations.nameLabel());
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
        StringBuilder categories = new StringBuilder();
        int count = 1;
        for(CategoryDto category : JsArrays.toIterable(JsArrays.toSafeArray(object.getCategoriesArray()))) {
          if(count > 10) {
            categories.append(" ...");
            break;
          }
          if(categories.length() == 0) {
            categories.append(category.getName());
          } else {
            categories.append(", ").append(category.getName());
          }
          count++;
        }
        return categories.toString();
      }
    }, translations.categoriesLabel());

    table.setSelectionModel(new SingleSelectionModel<VariableDto>());
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new InlineLabel(translationMessages.variableCount(0)));
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);

    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterVariables());
  }

  private void addCheckColumn() {
    checkColumn = new CheckboxColumn<VariableDto>(new VariableDtoDisplay());

    table.addColumn(checkColumn, checkColumn.getTableListCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Unit.PX);
  }

  @Override
  public void beforeRenderRows() {
    pager.setVisible(false);
    selectAllItemsAlert.setVisible(false);
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
    dataProvider.setList(JsArrays.toList(rows));
    pager.firstPage();
    dataProvider.refresh();
  }

  @Override
  public void initContingencyTable(JsArray<VariableDto> rows) {
    contingencyTablePanel.setVisible(true);

    // Prepare cross table form
    categoricalVariable.setPlaceholder(translations.selectCategoricalVariableLabel());
    categoricalVariable.setText("");
    crossWithVariable.setPlaceholder(translations.selectAnotherVariableLabel());
    crossWithVariable.setText("");

    categoricalVariables.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        categoricalVariable.setText(((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable());
        getUiHandlers().onCrossVariables();
        return ((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable();
      }
    });

    crossWithVariables.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        crossWithVariable.setText(((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable());
        getUiHandlers().onCrossVariables();
        return ((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable();
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void clear(boolean cleanFilter) {
    renderRows((JsArray<VariableDto>) JavaScriptObject.createArray());
    checkColumn.clearSelection();
    if(cleanFilter) filter.setText("");
    crossResultsPanel.clear();
    contingencyTablePanel.setVisible(false);
  }

  @Override
  public void setTable(TableDto dto) {
    tableDto = dto;
    name.setText(dto.getName());
    entityType.setText(dto.getEntityType());
    timestamps.setText(Moment.create(dto.getTimestamps().getLastUpdate()).fromNow());
    variableCount.setText(dto.hasVariableCount() ? dto.getVariableCount() + "" : "-");
    entityCount.setText(dto.hasValueSetCount() ? dto.getValueSetCount() + "" : "-");

    VariableSuggestOracle oracle = (VariableSuggestOracle) categoricalVariables.getSuggestOracle();
    oracle.setDatasource("\"" + tableDto.getDatasourceName() + "\"");
    oracle.setTable("\"" + tableDto.getName() + "\"");

    VariableSuggestOracle crossOracle = (VariableSuggestOracle) crossWithVariables.getSuggestOracle();
    crossOracle.setDatasource("\"" + tableDto.getDatasourceName() + "\"");
    crossOracle.setTable("\"" + tableDto.getName() + "\"");
  }

  @Override
  public void setFromTables(JsArrayString tableNames) {
    if(propertiesTable.getRowCount() > 2) {
      propertiesTable.removeProperty(2);
    }
    if(tableNames != null) {
      FlowPanel fromTableLinks = new FlowPanel();
      for(int i = 0; i < tableNames.length(); i++) {
        String tableFullName = tableNames.get(i);
        Anchor a = new Anchor();
        a.setText(tableFullName);
        MagmaPath.Parser parser = MagmaPath.Parser.parse(tableFullName);
        a.setHref("#" + placeManager
            .buildHistoryToken(ProjectPlacesHelper.getTablePlace(parser.getDatasource(), parser.getTable())));
        fromTableLinks.add(a);

        if(i < tableNames.length() - 1) {
          fromTableLinks.add(new InlineLabel(", "));
        }
      }
      propertiesTable.addProperty(new Label(translations.tableReferencesLabel()), fromTableLinks);
    }
  }

  @Override
  public String getSelectedVariableName() {
    return categoricalVariable.getText();
  }

  @Override
  public String getCrossWithVariableName() {
    return crossWithVariable.getText();
  }

  @Override
  public void hideContingencyTable() {
    contingencyTablePanel.setVisible(false);
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

  @UiHandler("addVariable")
  void onAddVariable(ClickEvent event) {
    getUiHandlers().onAddVariable();
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

  @UiHandler("deleteVariables")
  void onDeleteVariables(ClickEvent event) {
    getUiHandlers().onDeleteVariables(checkColumn.getSelectedItems());
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

  @UiHandler("crossVariablesButton")
  void onCrossVariables(ClickEvent event) {
    crossResultsPanel.clear();
    getUiHandlers().onCrossVariables();
  }

  @UiHandler("addAttribute")
  void onAddAttribute(ClickEvent event) {
    getUiHandlers().onAddAttribute(checkColumn.getSelectedItems());
  }

  @Override
  public HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler) {
    return table.addColumnSortHandler(handler);
  }

  @Override
  public HasAuthorization getEditAuthorizer() {
    return new WidgetAuthorizer(edit, addVariable);
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
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, PERMISSIONS_TAB_INDEX);
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
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
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

    @Override
    public Alert getAlert() {
      return selectAllItemsAlert;
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

  private class VariableLinkCell extends PlaceRequestCell<VariableDto> {

    private VariableLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(VariableDto value) {
      return ProjectPlacesHelper.getVariablePlace(tableDto.getDatasourceName(), tableDto.getName(), value.getName());
    }

    @Override
    public String getText(VariableDto value) {
      return value.getName();
    }
  }

  private class VariableColumn extends Column<VariableDto, VariableDto> {

    private VariableColumn(VariableLinkCell cell) {
      super(cell);
    }

    @Override
    public VariableDto getValue(VariableDto object) {
      return object;
    }
  }
}
