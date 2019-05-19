/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
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
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CriteriaPanel;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.ToggleAnchor;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.rql.client.RQLParser;
import org.obiba.opal.web.gwt.rql.client.RQLQuery;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;

public class SearchVariablesView extends ViewWithUiHandlers<SearchVariablesUiHandlers> implements SearchVariablesPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchVariablesView> {
  }

  private final TranslationMessages translationMessages;

  private final PlaceManager placeManager;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  CriteriaPanel queryPanel;

  @UiField(provided = true)
  Typeahead queryTypeahead;

  @UiField
  TextBox containsInput;

  @UiField
  TextBox queryInput;

  @UiField
  TextArea queryArea;

  @UiField
  Button searchButton;

  @UiField
  ToggleAnchor queryMode;

  @UiField
  Image resetPending;

  @UiField
  Image refreshPending;

  @UiField
  Panel variableItemTablePanel;

  @UiField
  Button addToCartAll;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  Panel selectAllProgressBox;

  @UiField
  ProgressBar selectAllProgress;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  VariableItemTable variableItemTable;

  @UiField
  OpalSimplePager variableItemPager;

  @UiField
  SortDropdown sortDropdown;

  private VariableItemProvider variableItemProvider;

  private VariableFieldSuggestOracle oracle;

  private String resultsQuery;

  @Inject
  public SearchVariablesView(SearchVariablesView.Binder uiBinder, Translations translations, TranslationMessages translationMessages, PlaceManager placeManager) {
    this.translationMessages = translationMessages;
    initQueryTypeahead();
    initWidget(uiBinder.createAndBindUi(this));
    this.placeManager = placeManager;
    queryMode.setOnText(translations.advancedLabel());
    queryMode.setOffText(translations.basicLabel());
    queryMode.removeStyleName("label");
    queryMode.setDelegate(new ToggleAnchor.Delegate() {
      @Override
      public void executeOn() {
        advancedVisible(true);
      }

      @Override
      public void executeOff() {
        advancedVisible(false);
      }

      private void advancedVisible(boolean visible) {
        if (visible) queryArea.setText(getQuery());
        showAdvancedQuery(visible);
        onSearch(0);
      }
    });
    sortDropdown.addClickListener(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        onSearch(event);
      }
    });
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("searchButton")
  public void onSearch(ClickEvent event) {
    onSearch(0);
  }

  @UiHandler("clearButton")
  public void onClear(ClickEvent event) {
    reset();
    showAdvancedQuery(false);
    getUiHandlers().onClear();
  }

  @UiHandler("containsInput")
  public void onContainsTyped(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER || getQuery().isEmpty()) onSearch(0);
  }

  @UiHandler("queryArea")
  public void onAdvancedQueryTyped(KeyUpEvent event) {
    if ((event.getNativeKeyCode() == KeyCodes.KEY_ENTER && event.isControlKeyDown()) || getQuery().isEmpty())
      onSearch(0);
  }

  @UiHandler("addToCart")
  public void onAddToCart(ClickEvent event) {
    getUiHandlers().onAddToCart(variableItemTable.getSelectedItems());
  }

  @UiHandler("addToCartAll")
  public void onAddToCartAll(ClickEvent event) {
    addToCartAll.setEnabled(false);
    addToCartAll.getElement().getStyle().setCursor(Style.Cursor.WAIT);
    getUiHandlers().onSearchAll(getQuery(), new SearchVariablesPresenter.QueryResultHandler() {

      private final List<ItemResultDto> allResults = Lists.newArrayList();

      @Override
      public void onQueryResult(String rqlQuery, int offset, QueryResultDto results) {
        allResults.addAll(JsArrays.toList(results.getHitsArray()));
        if (allResults.size() == results.getTotalHits()) {
          getUiHandlers().onAddToCart(allResults);
          addToCartAll.setEnabled(true);
          addToCartAll.getElement().getStyle().setCursor(Style.Cursor.DEFAULT);
        }
      }
    });
  }

  @Override
  public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    ((VariableFieldSuggestOracle) queryTypeahead.getSuggestOracle()).setTaxonomyTerms(taxonomies);
    ((VariableFieldSuggestOracle) queryTypeahead.getSuggestOracle()).setTaxonomyVocabularies(taxonomies);
  }

  @Override
  public void setTables(List<TableDto> tables) {
    ((VariableFieldSuggestOracle) queryTypeahead.getSuggestOracle()).setTables(tables);
    resetPending.setVisible(false);
  }

  @Override
  public void setQuery(String query, int offset, String sort) {
    sortDropdown.setSelection(sort);
    if (Strings.isNullOrEmpty(query) || query.equals(getBasicQuery())) {
      queryMode.setOn(true, false);
      showAdvancedQuery(false);
    } else {
      queryMode.setOn(false, false);
      showAdvancedQuery(true);
      queryArea.setText(query);
    }
  }

  @Override
  public void setRQLQuery(String rqlQuery, int offset, String sort) {
    reset();
    sortDropdown.setSelection(sort);
    if (Strings.isNullOrEmpty(rqlQuery)) return;
    showAdvancedQuery(false);
    RQLQuery root = RQLParser.parse(rqlQuery.replaceAll(" ", "+"));
    for (int i = 0; i < root.getArgumentsSize(); i++) {
      RQLQuery query = root.getRQLQuery(i);
      if (!query.getName().equals("contains")) {
        String fieldName;
        if ("not".equals(query.getName())) {
          fieldName = query.getRQLQuery(0).getString(0);
        } else
          fieldName = query.getString(0);
        VariableFieldSuggestOracle.VariableFieldSuggestion suggestion = oracle.findSuggestion(fieldName);
        addCriterion(suggestion, query, null, false);
      } else {
        containsInput.setText(query.getString(0).replaceAll("\\+", " "));
      }
    }
    onSearch(offset);
  }

  @Override
  public void showResults(String query, int offset, QueryResultDto results) {
    initVariableItemTable();
    // if query has changed, clear selection model
    if (!query.equals(resultsQuery)) {
      variableItemTable.clearSelectedItems();
      resultsQuery = query;
    }
    variableItemProvider.updateRowData(offset, JsArrays.toList(results.getHitsArray()));
    variableItemProvider.updateRowCount(results.getTotalHits(), true);
    variableItemTable.setPageStart(offset);
    variableItemPager.setPagerVisible(results.getTotalHits() > Table.DEFAULT_PAGESIZE);
    setVariablesVisible(true);
    addToCartAll.setEnabled(results.getTotalHits() > 0);
  }

  @Override
  public void clearResults() {
    setVariablesVisible(false);
    refreshPending.setVisible(false);
  }

  @Override
  public void reset() {
    clearResults();
    queryPanel.clear();
    containsInput.setText("");
    queryInput.setText("");
    queryArea.setText("");
    queryMode.setOn(true);
  }


  //
  // Private methods
  //

  private void onSearch(int offset) {
    setVariablesVisible(false);
    getUiHandlers().onSearchRange(getQuery(), getRQLQuery(), offset, getSortWithOrder());
  }

  private String getQuery() {
    if (queryArea.isVisible()) return getAdvancedQuery();
    return getBasicQuery();
  }

  private String getRQLQuery() {
    return queryArea.isVisible() ? null : getBasicRQLQuery();
  }

  private String getSortWithOrder() {
    return sortDropdown.getSelection();
  }

  private String getBasicQuery() {
    String queries = queryPanel.getQueryString();
    if ("*".equals(queries)) queries = "";
    String queryText = containsInput.getText().trim();
    if (Strings.isNullOrEmpty(queryText)) return queries;
    if (Strings.isNullOrEmpty(queries)) return queryText;
    return queries + " AND (" + queryText + ")";
  }

  private String getBasicRQLQuery() {
    String queries = queryPanel.getRQLQueryString();
    if ("*".equals(queries)) queries = "";
    String contains = containsInput.getText().trim();
    if (!Strings.isNullOrEmpty(contains)) contains = "contains(" + contains + ")";
    return Strings.isNullOrEmpty(queries) ? contains : queries + (Strings.isNullOrEmpty(contains) ? "" : "," + contains);
  }

  private String getAdvancedQuery() {
    return queryArea.getText();
  }

  private void showAdvancedQuery(boolean visible) {
    queryPanel.setVisible(!visible);
    containsInput.setVisible(!visible);
    queryInput.setVisible(!visible);
    queryArea.setVisible(visible);
    if (visible)
      searchButton.removeStyleName("small-indent");
    else
      searchButton.addStyleName("small-indent");
  }

  private void initQueryTypeahead() {
    oracle = new VariableFieldSuggestOracle();
    queryTypeahead = new Typeahead(oracle);
    queryTypeahead.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion = (VariableFieldSuggestOracle.VariableFieldSuggestion) selectedSuggestion;
        //if (!fieldSuggestion.getFieldTerms().isEmpty()) {
        //  getUiHandlers().onFacet(fieldSuggestion.getField(), fieldSuggestion.getFieldTerms().size(), new VariableFieldFacetHandler(fieldSuggestion));
        //} else
        addCriterion(fieldSuggestion, null, null, true);
        return "";
      }

    });
    queryTypeahead.setDisplayItemCount(15);
    queryTypeahead.setMinLength(2);
    queryTypeahead.setMatcherCallback(new Typeahead.MatcherCallback() {
      @Override
      public boolean compareQueryToItem(String query, String item) {
        return true;
      }
    });
  }

  private class VariableFieldFacetHandler implements SearchVariablesUiHandlers.FacetHandler {

    private final VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion;

    private VariableFieldFacetHandler(VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion) {
      this.fieldSuggestion = fieldSuggestion;
    }

    @Override
    public void onResult(FacetResultDto facet) {
      addCriterion(fieldSuggestion, null, facet, true);
    }
  }

  private void addCriterion(VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion, RQLQuery rqlQuery, FacetResultDto facet, boolean opened) {
    VariableFieldDropdown dd = new VariableFieldDropdown(fieldSuggestion, rqlQuery == null, facet) {
      @Override
      public void doFilter() {
        onSearch(0);
      }
    };
    dd.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        onSearch(0);
      }
    });
    dd.initialize(rqlQuery);
    queryPanel.addCriterion(dd, true, opened);
    queryInput.setText("");
    if (rqlQuery == null) onSearch(0);
  }

  private void initVariableItemTable() {
    if (variableItemProvider == null) {
      variableItemProvider = new VariableItemProvider();
      variableItemTable.initialize(placeManager, new VariableItemTable.ItemResultCheckDisplay() {
        @Override
        public IconAnchor getClearSelection() {
          return clearSelectionAnchor;
        }

        @Override
        public IconAnchor getSelectAll() {
          return selectAllAnchor;
        }

        @Override
        public HasText getSelectAllStatus() {
          return selectAllStatus;
        }

        @Override
        public String getNItemLabel(int nb) {
          return translationMessages.nVariablesLabel(nb);
        }

        @Override
        public Alert getSelectActionsAlert() {
          return selectAllItemsAlert;
        }

        @Override
        public Alert getSelectTipsAlert() {
          return selectItemTipsAlert;
        }

        @Override
        public void selectAllItems(final CheckboxColumn.ItemSelectionHandler<ItemResultDto> handler) {
          getUiHandlers().onSearchAll(getQuery(), new SearchVariablesPresenter.QueryResultHandler() {
            @Override
            public void onQueryResult(String rqlQuery, int offset, QueryResultDto results) {
              int last = offset + results.getHitsArray().length();
              boolean queryPending = last < results.getTotalHits();
              selectAllAnchor.setVisible(!queryPending);
              selectAllProgressBox.setVisible(queryPending);
              selectAllProgress.setPercent((last * 100) / results.getTotalHits());
              for (ItemResultDto item : JsArrays.toList(results.getHitsArray()))
                handler.onItemSelection(item);
              if (!queryPending) {
                selectAllProgress.setPercent(0);
                variableItemTable.redraw();
              }
            }
          });
        }
      });
      variableItemPager.setDisplay(variableItemTable);
      variableItemProvider.addDataDisplay(variableItemTable);
    }
  }

  private void setVariablesVisible(boolean visible) {
    refreshPending.setVisible(!visible);
    variableItemTablePanel.setVisible(visible);
    variableItemPager.setVisible(visible);
    sortDropdown.setVisible(visible);
    addToCartAll.setVisible(visible);
  }

  private class VariableItemProvider extends AsyncDataProvider<ItemResultDto> {

    @Override
    protected void onRangeChanged(HasData<ItemResultDto> display) {
      Range range = display.getVisibleRange();
      setVariablesVisible(false);
      getUiHandlers().onSearchRange(getQuery(), getRQLQuery(), range.getStart(), getSortWithOrder());
    }
  }

}
