/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.ui.*;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.app.client.ui.celltable.VariableAnnotationColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.gwt.rest.client.authorization.*;
import org.obiba.opal.web.model.client.magma.*;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.TableIndexationStatus;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import java.util.ArrayList;
import java.util.List;

public class TableView extends ViewWithUiHandlers<TableUiHandlers> implements TablePresenter.Display {

  interface Binder extends UiBinder<Widget, TableView> {
  }

  private static final int VALUES_TAB_INDEX = 2;

  private static final int ANALYSES_TAB_INDEX = 3;

  private static final int PERMISSIONS_TAB_INDEX = 4;

  @UiField
  Label name;

  @UiField
  Label entityType;

  @UiField
  PropertiesTable propertiesTable;

  @UiField
  Panel viewProperties;

  @UiField
  CodeBlock whereScript;

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
  IconAnchor clearIndexLink;

  @UiField
  IconAnchor indexNowLink;

  @UiField
  IconAnchor scheduleLink;

  @UiField
  IconAnchor cancelLink;

  @UiField
  ProgressBar progress;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Dropdown applyAttributeButton;

  @UiField
  IconAnchor deleteVariables;

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
  OpalSimplePager pager;

  @UiField
  OpalSimplePager pager2;

  @UiField
  TextBoxClearable filter;

  @UiField
  TabPanel tabPanel;

  @UiField
  Button exportData;

  @UiField
  ButtonGroup copyDataBtn;

  @UiField
  Button copyData;

  @UiField
  DropdownButton copyViewBtn;

  @UiField
  NavLink downloadDictionary;

  @UiField
  NavLink downloadView;

  @UiField
  IconAnchor edit;

  @UiField
  IconAnchor editWhere;

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

  @UiField
  Dropdown deleteAttributeButton;

  @UiField
  Button clearCrossVariables;

  @UiField
  DropdownButton downloadBtn;

  @UiField
  NavLink addVariablesFromFile;

  @UiField
  DropdownButton addVariablesButton;

  @UiField
  ButtonGroup tableAddVariableGroup;

  @UiField
  Tab analysesTab;

  @UiField
  SimplePanel analysesPanel;

  @UiField
  NavLink downloadAnalyses;

  @UiField
  IconAnchor analyseVariables;

  @UiField
  Button sqlQuery;

  private Button reconnectButton;

  private final ListDataProvider<VariableDto> dataProvider = new ListDataProvider<>();

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final PlaceManager placeManager;

  private TableDto tableDto;

  private CheckboxColumn<VariableDto> checkColumn;

  private VariableAnnotationColumn variableAnnotationColumn;

  private boolean hasLinkAuthorization = true;

  @Inject
  public TableView(Binder uiBinder, EventBus eventBus, Translations translations,
                   TranslationMessages translationMessages, PlaceManager placeManager) {
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.placeManager = placeManager;

    categoricalVariables = new Typeahead(new CategoricalVariableSuggestOracle(eventBus));
    crossWithVariables = new Typeahead(new CrossableVariableSuggestOracle(eventBus));

    initWidget(uiBinder.createAndBindUi(this));

    downloadBtn.setText(translations.downloadLabel());
    addTableColumns();
    initializeFilter();

    progress.setWidth("150px");
    addVariablesButton.setText(translations.addVariables());
    applyAttributeButton.setText(translations.applyAttribute());
    deleteAttributeButton.setText(translations.removeAttribute());
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = null;
    if (slot == Slots.Values) {
      panel = valuesPanel;
    } else if (slot == Slots.Permissions) {
      panel = permissionsPanel;
    } else if (slot == Slots.ContingencyTable) {
      panel = crossResultsPanel;
    } else if (slot == Slots.Analyses) {
      panel = analysesPanel;
    }

    if (panel != null) {
      panel.clear();

      if (content != null) {
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
        for (CategoryDto category : JsArrays.toIterable(JsArrays.toSafeArray(object.getCategoriesArray()))) {
          if (count > 10) {
            categories.append(" ...");
            break;
          }
          if (categories.length() == 0) {
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
    pager2.setDisplay(table);
    dataProvider.addDataDisplay(table);

    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterVariables());
  }

  private void addCheckColumn() {
    checkColumn = new CheckboxColumn<>(new VariableDtoDisplay());

    table.addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Unit.PX);
  }

  @Override
  public void beforeRenderRows() {
    pager.setPagerVisible(false);
    pager2.setPagerVisible(false);
    selectAllItemsAlert.setVisible(false);
    table.showLoadingIndicator(dataProvider);

    if (checkColumn != null) {
      checkColumn.clearSelection();
    }
  }

  @Override
  public void afterRenderRows() {
    boolean enableItem = !dataProvider.getList().isEmpty();
    pager.setPagerVisible(dataProvider.getList().size() > Table.DEFAULT_PAGESIZE);
    pager2.setPagerVisible(dataProvider.getList().size() > Table.DEFAULT_PAGESIZE);
    downloadDictionary.setDisabled(!enableItem);
    exportData.setEnabled(enableItem);
    copyData.setEnabled(enableItem);
    table.hideLoadingIndicator();
  }

  @Override
  public void renderRows(JsArray<VariableDto> rows) {
    dataProvider.setList(JsArrays.toList(rows));
    pager.firstPage();
    pager2.firstPage();
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
        return ((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable();
      }
    });

    crossWithVariables.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        crossWithVariable.setText(((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable());
        return ((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable();
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void clear(boolean cleanFilter) {
    renderRows((JsArray<VariableDto>) JavaScriptObject.createArray());
    checkColumn.clearSelection();
    if (cleanFilter) filter.setText("");
    crossResultsPanel.clear();
    contingencyTablePanel.setVisible(false);
  }

  @Override
  public void initialize(TableDto dto, List<TaxonomyDto> taxonomies) {
    setTable(dto);
    setTaxonomies(taxonomies);
  }

  @Override
  public void setTableSummary(String varCount, String valueSetCount) {
    variableCount.setText(varCount);
    entityCount.setText(valueSetCount);
  }

  @Override
  public void setView(ViewDto viewDto) {
    while (propertiesTable.getRowCount() > 2) {
      propertiesTable.removeProperty(2);
    }
    if (viewDto == null) {
      viewProperties.setVisible(false);
    } else {
      if (viewDto.getExtension(ResourceViewDto.ViewDtoExtensions.view) == null) {
        setFromTables(viewDto.getFromArray(), viewDto.getInnerFromArray());
        setWhereScript(viewDto.hasWhere() ? viewDto.getWhere() : null);
      } else {
        setRecourceViewProperties((ResourceViewDto) viewDto.getExtension(ResourceViewDto.ViewDtoExtensions.view));
        setFromResources(viewDto.getFromArray());
      }
    }
  }

  @Override
  public void resourceViewReconnectCompleted() {
    if (reconnectButton != null)
      reconnectButton.setEnabled(true);
  }

  private void setFromResources(JsArrayString resourceNames) {
    if (resourceNames != null) {
      FlowPanel fromResourceLinks = new FlowPanel();
      for (int i = 0; i < resourceNames.length(); i++) {
        String tableFullName = resourceNames.get(i);
        IconAnchor a = new IconAnchor();
        a.setText(tableFullName);
        MagmaPath.Parser parser = MagmaPath.Parser.parse(tableFullName);
        a.setHref("#" + placeManager
            .buildHistoryToken(ProjectPlacesHelper.getResourcePlace(parser.getDatasource(), parser.getTable())));
        fromResourceLinks.add(a);

        if (i < resourceNames.length() - 1) {
          fromResourceLinks.add(new InlineLabel(", "));
        }
      }
      reconnectButton = new Button();
      reconnectButton.setIcon(IconType.REFRESH);
      reconnectButton.setSize(ButtonSize.SMALL);
      reconnectButton.setText("Reconnect");
      reconnectButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          reconnectButton.setEnabled(false);
          getUiHandlers().onReconnectResourceView();
        }
      });
      FlowPanel panel = new FlowPanel();
      panel.add(fromResourceLinks);
      panel.add(reconnectButton);
      propertiesTable.addProperty(new Label(translations.resourceReferenceLabel()), panel);
    }
  }

  private void setRecourceViewProperties(ResourceViewDto resView) {
    propertiesTable.addProperty(new Label(translations.idColumnLabel()), new Label(resView.getIdColumn()));
    String allColumnsLabel = (!resView.hasAllColumns() || resView.getAllColumns()) ? translations.yesLabel() : translations.noLabel();
    propertiesTable.addProperty(new Label(translations.allColumnsLabel()), new Label(allColumnsLabel));
    propertiesTable.addProperty(new Label(translations.rServerProfileLabel()), new Label(resView.getProfile()));
  }

  private void setFromTables(JsArrayString tableNames, JsArrayString innerTables) {
    List<String> innerTablesList = new ArrayList<String>(JsArrays.toList(innerTables));
    if (tableNames != null) {
      FlowPanel fromTableLinks = new FlowPanel();
      for (int i = 0; i < tableNames.length(); i++) {
        String tableFullName = tableNames.get(i);
        IconAnchor a = new IconAnchor();
        a.setText(tableFullName);
        a.setIcon(innerTablesList.contains(tableFullName) ? IconType.CIRCLE_BLANK : IconType.CIRCLE);
        MagmaPath.Parser parser = MagmaPath.Parser.parse(tableFullName);
        a.setHref("#" + placeManager
          .buildHistoryToken(ProjectPlacesHelper.getTablePlace(parser.getDatasource(), parser.getTable())));
        fromTableLinks.add(a);

        if (i < tableNames.length() - 1) {
          fromTableLinks.add(new InlineLabel(", "));
        }
      }
      propertiesTable.addProperty(new Label(translations.tableReferencesLabel()), fromTableLinks);
    }
  }

  private void setWhereScript(String script) {
    viewProperties.setVisible(true);
    if (Strings.isNullOrEmpty(script)) {
      whereScript.setText("// " + translations.noFilter());
    } else {
      whereScript.setText(script);
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

  @Override
  public void setVariableFilter(String variableFilter) {
    filter.setText(variableFilter);
  }

  @UiHandler("dictionnaryTab")
  void onDictionnaryTabSelect(ClickEvent event) {
    getUiHandlers().onShowDictionary();
  }

  @UiHandler("valuesTab")
  void onValuesTabSelect(ClickEvent event) {
    getUiHandlers().onShowValues();
  }

  @UiHandler("analysesTab")
  void onAnalysesTabSelect(ClickEvent event) {
    getUiHandlers().onShowAnalyses();
  }

  @UiHandler("downloadDictionary")
  void onDownloadDictionary(ClickEvent event) {
    getUiHandlers().onDownloadDictionary();
  }

  @UiHandler("downloadView")
  void onDownloadView(ClickEvent event) {
    getUiHandlers().onDownloadView();
  }

  @UiHandler("downloadAnalyses")
  void onDownloadAnalyses(ClickEvent event) {
    getUiHandlers().onDownloadAnalyses();
  }

  @UiHandler("searchVariables")
  void onSearchVariables(ClickEvent event) {
    getUiHandlers().onSearchVariables();
  }

  @UiHandler("sqlQuery")
  void onSQLQuery(ClickEvent event) {
    getUiHandlers().onSQLQuery();
  }

  @UiHandler("remove")
  void onRemove(ClickEvent event) {
    getUiHandlers().onRemove();
  }

  @UiHandler("edit")
  void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("editWhere")
  void onEditWhere(ClickEvent event) {
    getUiHandlers().onEditWhere();
  }

  @UiHandler({"addVariable", "addTableVariable"})
  void onAddVariable(ClickEvent event) {
    getUiHandlers().onAddVariable();
  }

  @UiHandler("addVariablesFromFile")
  void onAddVariableFromFile(ClickEvent event) {
    getUiHandlers().onAddVariablesFromFile();
  }

  @UiHandler("exportData")
  void onExportData(ClickEvent event) {
    getUiHandlers().onExportData();
  }

  @UiHandler("copyData")
  void onCopyData(ClickEvent event) {
    getUiHandlers().onCopyData();
  }

  @UiHandler("copyViewData")
  void onCopyViewData(ClickEvent event) {
    getUiHandlers().onCopyData();
  }

  @UiHandler("copyView")
  void onCopyView(ClickEvent event) {
    getUiHandlers().onCopyView();
  }

  @UiHandler("copyVariables")
  void onAddVariablesToView(ClickEvent event) {
    getUiHandlers().onAddVariablesToView(checkColumn.getSelectedItems());
  }

  @UiHandler("cartVariables")
  void onAddVariablesToCart(ClickEvent event) {
    getUiHandlers().onAddVariablesToCart(checkColumn.getSelectedItems());
  }

  @UiHandler("analyseVariables")
  void onAnalyseVariables(ClickEvent event) {
    tabPanel.selectTab(ANALYSES_TAB_INDEX);
    getUiHandlers().onShowAnalyses();
    getUiHandlers().onAnalyseVariables(checkColumn.getSelectedItems());
  }

  @UiHandler("deleteVariables")
  void onDeleteVariables(ClickEvent event) {
    getUiHandlers().onDeleteVariables(checkColumn.getSelectedItems());
    checkColumn.clearSelection();
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

  @UiHandler("clearCrossVariables")
  void onClearCrossVariables(ClickEvent event) {
    crossResultsPanel.clear();
    categoricalVariable.setText("");
    crossWithVariable.setText("");
  }

  @UiHandler("applyTaxonomy")
  void onApplyTaxonomyAttribute(ClickEvent event) {
    getUiHandlers().onApplyTaxonomyAttribute(checkColumn.getSelectedItems());
  }

  @UiHandler("applyAttribute")
  void onApplyCustomAttribute(ClickEvent event) {
    getUiHandlers().onApplyCustomAttribute(checkColumn.getSelectedItems());
  }

  @UiHandler("deleteTaxonomy")
  void onDeleteTaxonomyAttribute(ClickEvent event) {
    getUiHandlers().onDeleteTaxonomyAttribute(checkColumn.getSelectedItems());
  }

  @UiHandler("deleteAttribute")
  void onDeleteCustomAttribute(ClickEvent event) {
    getUiHandlers().onDeleteCustomAttribute(checkColumn.getSelectedItems());
  }

  @UiHandler("filter")
  void onFilterUpdate(KeyUpEvent event) {
    getUiHandlers().onVariablesFilterUpdate(filter.getText());
  }

  @Override
  public HandlerRegistration addVariableSortHandler(ColumnSortEvent.Handler handler) {
    return table.addColumnSortHandler(handler);
  }

  @Override
  public HasAuthorization getEditAuthorizer() {
    return new WidgetAuthorizer(edit, editWhere, tableDto.hasViewLink() ? addVariablesButton : tableAddVariableGroup, applyAttributeButton, deleteAttributeButton, deleteVariables);
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
  public HasAuthorization getAnalysesDownloadAuthorizer() {
    return new WidgetAuthorizer(downloadAnalyses);
  }

  @Override
  public HasAuthorization getValuesAuthorizer() {
    return new CompositeAuthorizer(new TabPanelAuthorizer(tabPanel, VALUES_TAB_INDEX), new WidgetAuthorizer(sqlQuery));
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
  public HasAuthorization getAnalysesAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, ANALYSES_TAB_INDEX);
  }

  @Override
  public void enableAnalyses(boolean enable) {
    if (enable) {
      analysesTab.asWidget().getElement().removeAttribute("style");
    } else {
      analysesTab.asWidget().getElement().setAttribute("style", "display:none");
    }

    analyseVariables.setVisible(enable);
    downloadAnalyses.setVisible(enable);
  }

  @Override
  public boolean isValuesTabSelected() {
    return tabPanel.getSelectedTab() == VALUES_TAB_INDEX;
  }

  @Override
  public boolean isAnalysesTabSelected() {
    return tabPanel.getSelectedTab() == ANALYSES_TAB_INDEX;
  }

  @Override
  public void setIndexStatusVisible(boolean b) {
    indexStatus.setVisible(b);
  }

  @SuppressWarnings({"IfStatementWithTooManyBranches"})
  @Override
  public void setIndexStatusAlert(TableIndexStatusDto statusDto) {

    if (statusDto.getStatus().getName().equals(TableIndexationStatus.UPTODATE.getName())) {
      setStatusText(translations.indexAlertUpToDate(), AlertType.SUCCESS, true, false, true, false, false);
      setProgressBar(false, 0);
    } else if (statusDto.getStatus().getName().equals(TableIndexationStatus.OUTDATED.getName())) {
      setStatusText(translations.indexStatusOutOfDate(), AlertType.ERROR, false, true, true, false, false);
      setProgressBar(false, 0);
    } else if (statusDto.getStatus().getName().equals(TableIndexationStatus.IN_PROGRESS.getName())) {
      setStatusText(translations.indexStatusInProgress(), AlertType.INFO, false, false, false, true, true);
      setProgressBar(true, (int) (statusDto.getProgress() * 100));
    } else if (statusDto.getStatus().getName().equals(TableIndexationStatus.NOT_INDEXED.getName())) {
      setStatusText(translations.indexStatusNotIndexed(), AlertType.WARNING, false, true, true, false, false);
      setProgressBar(false, 0);
    }
  }

  //
  // Private methods
  //

  private void setTable(TableDto dto) {
    tableDto = dto;
    clear(true);
    name.setText(dto.getName());
    entityType.setText(dto.getEntityType());
    timestamps.setText(Moment.create(dto.getTimestamps().getLastUpdate()).fromNow());
    variableCount.setText(dto.hasVariableCount() ? dto.getVariableCount() + "" : "-");
    entityCount.setText(dto.hasValueSetCount() ? dto.getValueSetCount() + "" : "-");
    addVariablesButton.setVisible(dto.hasViewLink());
    tableAddVariableGroup.setVisible(!addVariablesButton.isVisible());
    sqlQuery.setEnabled(!dto.hasValueSetCount() || dto.getValueSetCount() > 0);

    copyDataBtn.setVisible(!tableDto.hasViewLink());
    copyViewBtn.setVisible(tableDto.hasViewLink());

    VariableSuggestOracle oracle = (VariableSuggestOracle) categoricalVariables.getSuggestOracle();
    oracle.setDatasource("\"" + tableDto.getDatasourceName() + "\"");
    oracle.setTable("\"" + tableDto.getName() + "\"");

    VariableSuggestOracle crossOracle = (VariableSuggestOracle) crossWithVariables.getSuggestOracle();
    crossOracle.setDatasource("\"" + tableDto.getDatasourceName() + "\"");
    crossOracle.setTable("\"" + tableDto.getName() + "\"");

    viewProperties.setVisible(false);
    initializeFilter();
  }

  private void setTaxonomies(List<TaxonomyDto> taxonomies) {
    if (variableAnnotationColumn == null && taxonomies != null) {
      table.addColumn(variableAnnotationColumn = new VariableAnnotationColumn(), translations.annotationsLabel());
    }
    if (variableAnnotationColumn != null) {
      variableAnnotationColumn.initialize(tableDto, taxonomies);
    }
  }

  @SuppressWarnings("PMD.ExcessiveParameterList")
  private void setStatusText(String text, AlertType type, boolean clear, boolean indexNow, boolean schedule,
                             boolean cancel, boolean progressBar) {
    indexStatusText.setText(text);
    indexStatusAlert.setType(type);
    if (hasLinkAuthorization) {
      clearIndexLink.setVisible(clear);
      indexNowLink.setVisible(indexNow);
      scheduleLink.setVisible(schedule);
      cancelLink.setVisible(cancel);
      progress.setVisible(progressBar);
    }
  }

  private void setProgressBar(boolean progressBar, int percent) {
    if (progressBar) {
      progress.setVisible(true);
      progress.setType(ProgressBar.Style.ANIMATED);
      progress.setPercent(percent);
      progress.setTitle(percent + "%");
    } else {
      progress.setVisible(false);
    }
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
    public void selectAllItems(CheckboxColumn.ItemSelectionHandler<VariableDto> handler) {
      for (VariableDto item : dataProvider.getList())
        handler.onItemSelection(item);
    }

    @Override
    public Label getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nVariablesLabel(nb).toLowerCase();
    }

    @Override
    public Alert getSelectActionsAlert() {
      return selectAllItemsAlert;
    }

    @Override
    public Alert getSelectTipsAlert() {
      return selectItemTipsAlert;
    }
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
