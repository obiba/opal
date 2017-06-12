/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.search.entity.*;
import org.obiba.opal.web.gwt.app.client.ui.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;
import org.obiba.opal.web.model.client.search.EntitiesResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;

public class SearchEntitiesView extends ViewWithUiHandlers<SearchEntitiesUiHandlers> implements SearchEntitiesPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchEntitiesView> {}

  private final Translations translations;

  private final PlaceManager placeManager;

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
  Panel entityResultPanel;

  @UiField
  Heading entityTitle;

  @UiField
  FlowPanel resultsPanel;

  @UiField
  Image refreshPending;

  @UiField
  CriteriaPanel criteriaPanel;

  private DefaultFlexTable resultsTable;

  private IndexedVariableSuggestOracle oracle;

  @Inject
  public SearchEntitiesView(EventBus eventBus, SearchEntitiesView.Binder uiBinder, Translations translations, PlaceManager placeManager) {
    initVariableTypeahead(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    this.placeManager = placeManager;
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
        getUiHandlers().onVariableFilter(variableSuggestion.getDatasource(), variableSuggestion.getTable(), variableSuggestion.getVariable());
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
    if (!searchButton.isEnabled()) return;
    List<String> queries = criteriaPanel.getQueryStrings();
    if (queries.isEmpty()) reset();
    getUiHandlers().onSearch(typeDropdown.getSelection(), queries);
  }

  @UiHandler("clearButton")
  public void onClear(ClickEvent event) {
    reset();
    getUiHandlers().onClear();
  }

  @Override
  public void setIndexedTables(List<TableDto> tables) {
    oracle.setIndexedTables(tables);
  }

  @Override
  public void searchEnabled(boolean enabled) {
    searchButton.setEnabled(enabled);
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
  public void showResults(EntitiesResultDto results) {
    List<String> queries = criteriaPanel.getQueryStrings();
    List<CriterionDropdown> criterions = criteriaPanel.getCriterions();
    if (criterions.isEmpty()) {
      refreshPending.setVisible(false);
      return;
    }
    prepareResultsTable();
    int row = 1;
    for (EntitiesResultDto result : JsArrays.toIterable(results.getPartialResultsArray())) {
      int idx = queries.indexOf(result.getQuery());
      ValueSetCriterionDropdown criterion = (ValueSetCriterionDropdown) criterions.get(idx);
      setResultsRow(row, criterion, result.getTotalHits());
      row++;
    }
    Label query = new Label(criteriaPanel.getQueryText());
    query.setTitle(results.getQuery());
    if (queries.size() == 1) {
      ValueSetCriterionDropdown criterion = (ValueSetCriterionDropdown) criterions.get(0);
      setResultsRow(row, criterion, results.getTotalHits());
    } else {
      Label all = new Label(translations.allLabel());
      all.addStyleName("property-key");
      resultsTable.setWidget(row, 0, all);
      resultsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
      resultsTable.setWidget(row, 1, query);
      Label total = new Label(results.getTotalHits() + "");
      total.addStyleName("property-key");
      resultsTable.setWidget(row, 2, total);
    }
    entityResultPanel.setVisible(true);
    refreshPending.setVisible(false);
  }

  @Override
  public void clearResults(boolean searchProgress) {
    entityResultPanel.setVisible(false);
    resultsPanel.clear();
    refreshPending.setVisible(searchProgress);
  }

  @Override
  public void reset() {
    clearResults(false);
    criteriaPanel.clear();
    variableInput.setText("");
  }

  @Override
  public void addCategoricalCriterion(ValueSetVariableCriterion filter, QueryResultDto facet) {
    addVariableFilter(new CategoricalCriterionDropdown(filter, facet) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    });
  }

  @Override
  public void addNumericalCriterion(ValueSetVariableCriterion filter, QueryResultDto facet) {
    addVariableFilter(new NumericalCriterionDropdown(filter, facet) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    });
  }

  @Override
  public void addDateCriterion(ValueSetVariableCriterion filter) {
    addVariableFilter(new DateTimeCriterionDropdown(filter) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    });
  }

  @Override
  public void addDefaultCriterion(ValueSetVariableCriterion filter) {
    addVariableFilter(new DefaultCriterionDropdown(filter) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    });
  }

  private void addVariableFilter(CriterionDropdown criterion) {
    criteriaPanel.addCriterion(criterion);
    onSearch(null);
  }

  private void prepareResultsTable() {
    resultsPanel.clear();
    resultsTable = new DefaultFlexTable();
    resultsPanel.add(resultsTable);
    resultsTable.setHeader(0, translations.tableLabel());
    resultsTable.setHeader(1, translations.variableLabel());
    resultsTable.setHeader(2, translations.queryLabel());
    resultsTable.setHeader(3, translations.countLabel());
  }

  private void setResultsRow(int row, ValueSetCriterionDropdown criterion, int count) {
    resultsTable.setWidget(row, 0, createTableLink(criterion.getDatasource(), criterion.getTable()));
    resultsTable.getFlexCellFormatter().setColSpan(row, 0, 1);
    resultsTable.setWidget(row, 1, createVariableLink(criterion.getDatasource(), criterion.getTable(), criterion.getVariable()));
    Label query = new Label(criterion.getText());
    query.setTitle(criterion.getQueryString());
    resultsTable.setWidget(row, 2, query);
    resultsTable.setText(row, 3, count + "");
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
}
