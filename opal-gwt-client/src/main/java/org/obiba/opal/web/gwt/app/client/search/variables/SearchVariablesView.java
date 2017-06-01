/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CriteriaPanel;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.ToggleAnchor;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;

public class SearchVariablesView extends ViewWithUiHandlers<SearchVariablesUiHandlers> implements SearchVariablesPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchVariablesView> {}

  private final Translations translations;

  private final PlaceManager placeManager;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  CriteriaPanel queryPanel;

  @UiField(provided = true)
  Typeahead queryTypeahead;

  @UiField
  TextBox queryInput;

  @UiField
  TextArea queryArea;

  @UiField
  Button searchButton;

  @UiField
  ToggleAnchor queryMode;

  @UiField
  Image refreshPending;

  @UiField
  VariableItemTable variableItemTable;

  @UiField
  OpalSimplePager variableItemPager;

  private VariableItemProvider variableItemProvider;

  @Inject
  public SearchVariablesView(SearchVariablesView.Binder uiBinder, Translations translations, PlaceManager placeManager) {
    this.translations = translations;
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
        queryPanel.setVisible(!visible);
        queryInput.setVisible(!visible);
        queryArea.setVisible(visible);
        if (visible)
          searchButton.removeStyleName("small-indent");
        else
          searchButton.addStyleName("small-indent");
        onSearch(null);
      }
    });
  }
  
  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("searchButton")
  public void onSearch(ClickEvent event) {
    setVariablesVisible(false);
    getUiHandlers().onSearch(getQuery());
  }

  @UiHandler("queryInput")
  public void onQueryTyped(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER || getQuery().isEmpty()) onSearch(null);
  }

  @UiHandler("queryArea")
  public void onAdvancedQueryTyped(KeyUpEvent event) {
    if ((event.getNativeKeyCode() == KeyCodes.KEY_ENTER && event.isControlKeyDown()) || getQuery().isEmpty()) onSearch(null);
  }

  @Override
  public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    ((VariableFieldSuggestOracle) queryTypeahead.getSuggestOracle()).setTaxonomies(taxonomies);
  }

  @Override
  public void setTables(List<TableDto> tables) {
    ((VariableFieldSuggestOracle) queryTypeahead.getSuggestOracle()).setTables(tables);
  }

  @Override
  public void setQuery(String query) {
    if (Strings.isNullOrEmpty(query)) {
      queryMode.setOn(true, false);
      queryPanel.setVisible(true);
      queryInput.setVisible(true);
      queryArea.setVisible(false);

    } else {
      queryMode.setOn(false, false);
      queryPanel.setVisible(false);
      queryInput.setVisible(false);
      queryArea.setVisible(true);
      queryArea.setText(query);
    }
  }

  @Override
  public void showResults(QueryResultDto results, int offset, int limit) {
    initVariableItemTable();
    variableItemProvider.updateRowData(offset, JsArrays.toList(results.getHitsArray()));
    variableItemProvider.updateRowCount(results.getTotalHits(), true);
    variableItemPager.setPagerVisible(results.getTotalHits() > Table.DEFAULT_PAGESIZE);
    setVariablesVisible(true);
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
    queryInput.setText("");
    queryArea.setText("");
    queryMode.setOn(true);
  }

  //
  // Private methods
  //

  private String getQuery() {
    if (queryArea.isVisible()) return queryArea.getText();
    String queryDropdowns = queryPanel.getQueryString();
    if ("*".equals(queryDropdowns)) queryDropdowns = "";
    return (queryDropdowns  + " " + queryInput.getText()).trim();
  }

  private void initQueryTypeahead() {
    queryTypeahead = new Typeahead(new VariableFieldSuggestOracle());
    queryTypeahead.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion = (VariableFieldSuggestOracle.VariableFieldSuggestion) selectedSuggestion;
        //if (!fieldSuggestion.getCategories().isEmpty()) {
        //  getUiHandlers().onFacet(fieldSuggestion.getField(), fieldSuggestion.getCategories().size(), new VariableFieldFacetHandler(fieldSuggestion));
        //} else
        addCriterion(fieldSuggestion, null);
        return "";
      }

    });
    queryTypeahead.setDisplayItemCount(15);
    queryTypeahead.setMinLength(2);
  }

  private class VariableFieldFacetHandler implements SearchVariablesUiHandlers.FacetHandler {

    private final VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion;

    private VariableFieldFacetHandler(VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion) {
      this.fieldSuggestion = fieldSuggestion;
    }

    @Override
    public void onResult(FacetResultDto facet) {
      addCriterion(fieldSuggestion, facet);
    }
  }

  private void addCriterion(VariableFieldSuggestOracle.VariableFieldSuggestion fieldSuggestion, FacetResultDto facet) {
    VariableFieldDropdown dd = new VariableFieldDropdown(fieldSuggestion, facet) {
      @Override
      public void doFilter() {
        onSearch(null);
      }
    };
    dd.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        onSearch(null);
      }
    });
    queryPanel.addCriterion(dd, true, false);
  }

  private void initVariableItemTable() {
    if (variableItemProvider == null) {
      variableItemProvider = new VariableItemProvider();
      variableItemTable.setPlaceManager(placeManager);
      variableItemPager.setDisplay(variableItemTable);
      variableItemProvider.addDataDisplay(variableItemTable);
    }
  }

  private void setVariablesVisible(boolean visible) {
    refreshPending.setVisible(!visible);
    variableItemTable.setVisible(visible);
    variableItemPager.setVisible(visible);
  }

  private class VariableItemProvider extends AsyncDataProvider<ItemResultDto> {

    @Override
    protected void onRangeChanged(HasData<ItemResultDto> display) {
      Range range = display.getVisibleRange();
      setVariablesVisible(false);
      getUiHandlers().onSearchRange(getQuery(), range.getStart(), range.getLength());
    }
  }

}
