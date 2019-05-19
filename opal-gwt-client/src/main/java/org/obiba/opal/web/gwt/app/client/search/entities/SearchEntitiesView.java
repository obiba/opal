/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entities;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.search.entity.*;
import org.obiba.opal.web.gwt.app.client.ui.*;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;
import org.obiba.opal.web.model.client.search.EntitiesResultDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;

public class SearchEntitiesView extends ViewWithUiHandlers<SearchEntitiesUiHandlers> implements SearchEntitiesPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchEntitiesView> {}

  private final Translations translations;

  private final PlaceManager placeManager;

  private final TokenFormatter tokenFormatter;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Panel entityPanel;

  @UiField
  EntityTypeDropdown typeDropdown;

  @UiField(provided = true)
  Typeahead variableTypeahead;

  @UiField
  TextBox variableInput;

  @UiField
  Button searchButton;

  @UiField
  Panel countsResultPanel;

  @UiField
  FlowPanel countsPanel;

  @UiField
  Panel entitiesResultPanel;

  @UiField
  OpalSimplePager entityItemPager;

  @UiField
  EntityItemTable entityItemTable;

  @UiField
  Image resetPending;

  @UiField
  Image refreshPending;

  @UiField
  SimplePanel idPanel;

  @UiField
  CriteriaPanel criteriaPanel;

  private CriterionPanel idCriterionPanel;

  private DefaultFlexTable resultsTable;

  private IndexedVariableSuggestOracle oracle;

  private EntityItemProvider entityItemProvider;

  @Inject
  public SearchEntitiesView(EventBus eventBus, SearchEntitiesView.Binder uiBinder, Translations translations,
                            PlaceManager placeManager, TokenFormatter tokenFormatter) {
    initVariableTypeahead(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    this.placeManager = placeManager;
    this.tokenFormatter = tokenFormatter;
    typeDropdown.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        getUiHandlers().onEntityType(typeDropdown.getSelection());
        reset();
      }
    });
  }

  private void initIdCriterionPanel(RQLIdentifierCriterionParser idFilter) {
    if (idCriterionPanel != null) return;
    idPanel.clear();
    this.idCriterionPanel = new CriterionPanel(new IdentifiersCriterionDropdown(idFilter) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    }, false, false);
    idPanel.add(idCriterionPanel);
  }

  private void initVariableTypeahead(EventBus eventBus) {
    oracle = new IndexedVariableSuggestOracle(eventBus);
    oracle.setLimit(15);
    variableTypeahead = new Typeahead(oracle);
    variableTypeahead.setMinLength(2);
    variableTypeahead.setDisplayItemCount(15);
    variableTypeahead.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        VariableSuggestOracle.VariableSuggestion variableSuggestion = (VariableSuggestOracle.VariableSuggestion) selectedSuggestion;
        getUiHandlers().onVariableCriterion(variableSuggestion.getDatasource(), variableSuggestion.getTable(), variableSuggestion.getVariable());
        return "";
      }
    });
    variableTypeahead.setMatcherCallback(new Typeahead.MatcherCallback() {
      @Override
      public boolean compareQueryToItem(String query, String item) {
        return true;
      }
    });
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("searchButton")
  public void onSearch(ClickEvent event) {
    onSearch(0, Table.DEFAULT_PAGESIZE);
  }

  @UiHandler("clearButton")
  public void onClear(ClickEvent event) {
    reset();
    getUiHandlers().onClear();
  }

  @UiHandler("addToView")
  public void onAddToView(ClickEvent event) {
    List<String> statements = Lists.newArrayList();
    String idJs = idCriterionPanel.getMagmaJsStatement();
    if (!Strings.isNullOrEmpty(idJs)) statements.add(idJs);
    statements.addAll(criteriaPanel.getMagmaJsStatements());
    getUiHandlers().onAddToView(criteriaPanel.getRQLFields(), statements);
  }

  @Override
  public void setIndexedTables(List<TableDto> tables) {
    oracle.setIndexedTables(tables);
  }

  @Override
  public void searchEnabled(boolean enabled) {
    resetPending.setVisible(!enabled);
    searchButton.setEnabled(enabled);
  }

  @Override
  public void searchUnavailable() {
    resetPending.setVisible(false);
    searchButton.setEnabled(false);
  }

  @Override
  public void triggerSearch() {
    onSearch(null);
  }

  @Override
  public void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType) {
    typeDropdown.setEntityTypes(entityTypes, selectedType);
    entityPanel.setVisible(!entityTypes.isEmpty());
  }

  @Override
  public void setEntityType(String selectedType) {
    typeDropdown.setSelection(selectedType);
  }

  @Override
  public void setQuery(String query) {
    if (Strings.isNullOrEmpty(query)) clearResults(false);
  }

  @Override
  public void showResults(EntitiesResultDto results, int offset, int limit) {
    showCountsResults(results);
    showIdentifiersResults(results, offset, limit);
    refreshPending.setVisible(false);
  }

  @Override
  public void clearResults(boolean searchProgress) {
    countsResultPanel.setVisible(false);
    countsPanel.clear();
    entitiesResultPanel.setVisible(false);
    refreshPending.setVisible(searchProgress);
  }

  @Override
  public void reset() {
    clearResults(false);
    criteriaPanel.clear();
    idPanel.clear();
    idCriterionPanel = null;
    variableInput.setText("");
  }

  @Override
  public void addCategoricalCriterion(RQLIdentifierCriterionParser idFilter, RQLValueSetVariableCriterionParser filter, QueryResultDto facet, boolean opened) {
    addVariableFilter(idFilter, new CategoricalCriterionDropdown(filter, facet) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    }, opened);
  }

  @Override
  public void addNumericalCriterion(RQLIdentifierCriterionParser idFilter, RQLValueSetVariableCriterionParser filter, QueryResultDto facet, boolean opened) {
    addVariableFilter(idFilter, new NumericalCriterionDropdown(filter, facet) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    }, opened);
  }

  @Override
  public void addDateCriterion(RQLIdentifierCriterionParser idFilter, RQLValueSetVariableCriterionParser filter, boolean opened) {
    addVariableFilter(idFilter, new DateTimeCriterionDropdown(filter) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    }, opened);
  }

  @Override
  public void addDefaultCriterion(RQLIdentifierCriterionParser idFilter, RQLValueSetVariableCriterionParser filter, boolean opened) {
    addVariableFilter(idFilter, new DefaultCriterionDropdown(filter) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    }, opened);
  }

  private void addVariableFilter(RQLIdentifierCriterionParser idFilter, CriterionDropdown criterion, boolean opened) {
    initIdCriterionPanel(idFilter);
    criteriaPanel.addCriterion(criterion, true, opened);
  }

  private void onSearch(int offset, int limit) {
    if (!searchButton.isEnabled()) return;
    List<String> queries = criteriaPanel.getRQLQueryStrings();
    if (queries.isEmpty()) reset();
    String idQuery = idCriterionPanel == null ? "" : idCriterionPanel.getRQLQueryString();
    getUiHandlers().onSearch(typeDropdown.getSelection(), idQuery, queries, offset, limit);
  }

  private void showIdentifiersResults(EntitiesResultDto results, int offset, int limit) {
    if (getValueSetCriterions().isEmpty()) {
      refreshPending.setVisible(false);
      return;
    }
    List<ItemResultDto> identifiers = JsArrays.toList(results.getHitsArray());
    if (limit == 0 || identifiers.isEmpty()) {
      entitiesResultPanel.setVisible(false);
      return;
    }
    initEntityItemTable();
    entityItemProvider.updateRowData(offset, identifiers);
    entityItemProvider.updateRowCount(results.getTotalHits(), true);
    entityItemPager.setPagerVisible(results.getTotalHits() > Table.DEFAULT_PAGESIZE);
    entitiesResultPanel.setVisible(true);
  }

  private void showCountsResults(EntitiesResultDto results) {
    List<String> queries = getValueSetRQLQueryStrings();
    List<CriterionDropdown> criterions = getValueSetCriterions();
    if (criterions.isEmpty()) {
      refreshPending.setVisible(false);
      return;
    }
    prepareResultsTable();
    int row = 1;
    for (EntitiesResultDto result : JsArrays.toIterable(results.getPartialResultsArray())) {
      int idx = queries.indexOf(result.getQuery());
      ValueSetCriterionDropdown criterion = (ValueSetCriterionDropdown) criterions.get(idx);
      setCountResultRow(row, criterion, result.getTotalHits());
      row++;
    }
    if (queries.size() == 1) {
      ValueSetCriterionDropdown criterion = (ValueSetCriterionDropdown) criterions.get(0);
      setCountResultRow(row, criterion, results.getTotalHits());
    } else {
      Label all = new Label(translations.allLabel());
      all.addStyleName("property-key");
      resultsTable.setWidget(row, 0, all);
      resultsTable.getFlexCellFormatter().setColSpan(row, 0, 3);
      Label query = new Label(getIDCriterionPrefix() + criteriaPanel.getQueryText());
      query.setTitle(getIDCriterionRQLPrefix() + results.getQuery());
      resultsTable.setWidget(row, 1, query);
      Label total = new Label(results.getTotalHits() + "");
      total.addStyleName("property-key");
      resultsTable.setWidget(row, 2, total);
    }
    countsResultPanel.setVisible(true);
  }

  private String getIDCriterionRQLPrefix() {
    return idCriterionPanel.isAll() ? "" : idCriterionPanel.getRQLQueryString() + " AND ";
  }

  private String getIDCriterionPrefix() {
    return idCriterionPanel.isAll() ? "" : idCriterionPanel.getQueryText() + " AND " ;
  }

  private List<String> getValueSetRQLQueryStrings() {
    return criteriaPanel.getRQLQueryStrings();
  }

  private List<CriterionDropdown> getValueSetCriterions() {
    return criteriaPanel.getCriterions();
  }

  private void prepareResultsTable() {
    countsPanel.clear();
    resultsTable = new DefaultFlexTable();
    countsPanel.add(resultsTable);
    resultsTable.setHeader(0, translations.tableLabel());
    resultsTable.setHeader(1, translations.variableLabel());
    resultsTable.setHeader(2, translations.labelLabel());
    resultsTable.setHeader(3, translations.queryLabel());
    resultsTable.setHeader(4, translations.countLabel());
  }

  private void setCountResultRow(int row, ValueSetCriterionDropdown criterion, int count) {
    resultsTable.setWidget(row, 0, createTableLink(criterion.getDatasource(), criterion.getTable()));
    resultsTable.getFlexCellFormatter().setColSpan(row, 0, 1);
    resultsTable.setWidget(row, 1, createVariableLink(criterion.getDatasource(), criterion.getTable(), criterion.getVariable()));
    resultsTable.setWidget(row, 2, createVariableLabel(criterion.getVariable()));
    Label query = new Label(getIDCriterionPrefix() + criterion.getText());
    query.setTitle(getIDCriterionRQLPrefix() + criterion.getRQLQueryString());
    resultsTable.setWidget(row, 3, query);
    resultsTable.setText(row, 4, count + "");
  }

  private Widget createTableLink(final String datasource, final String table) {
    IconAnchor link = new IconAnchor();
    link.setIcon(IconType.TABLE);
    link.setText(datasource + "." + table);
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        PlaceRequest request = ProjectPlacesHelper.getTablePlace(datasource, table);
        placeManager.revealPlace(request);
      }
    });
    return link;
  }

  private Widget createVariableLink(final String datasource, final String table, final VariableDto variable) {
    Anchor link = new Anchor(variable.getName());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        PlaceRequest request = ProjectPlacesHelper.getVariablePlace(datasource, table, variable.getName());
        placeManager.revealPlace(request);
      }
    });
    return link;
  }

  private Widget createVariableLabel(final VariableDto variable) {
    NamedAttributePanel labelPanel = new NamedAttributePanel("label");
    JsArray<AttributeDto> attributesArray = JsArrays.toSafeArray(variable.getAttributesArray());
    labelPanel.initialize(attributesArray, null);
    return labelPanel;
  }

  private void initEntityItemTable() {
    if (entityItemProvider == null || !typeDropdown.getSelection().equals(entityItemProvider.entityType)) {
      entityItemProvider = new EntityItemProvider(typeDropdown.getSelection());
      entityItemTable.initialize(tokenFormatter, typeDropdown.getSelection());
      entityItemPager.setDisplay(entityItemTable);
      entityItemProvider.addDataDisplay(entityItemTable);
    }
  }

  private class EntityItemProvider extends AsyncDataProvider<ItemResultDto> {

    private final String entityType;

    private EntityItemProvider(String entityType) {
      this.entityType = entityType;
    }

    @Override
    protected void onRangeChanged(HasData<ItemResultDto> display) {

      Range range = display.getVisibleRange();
      entitiesResultPanel.setVisible(false);
      refreshPending.setVisible(true);
      onSearch(range.getStart(), range.getLength());
    }
  }
}
