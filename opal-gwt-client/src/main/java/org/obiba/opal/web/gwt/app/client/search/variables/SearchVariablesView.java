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

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.search.ItemResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

public class SearchVariablesView extends ViewWithUiHandlers<SearchVariablesUiHandlers> implements SearchVariablesPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchVariablesView> {}

  private final Translations translations;

  private final PlaceManager placeManager;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  TextBox queryInput;

  @UiField
  Image refreshPending;

  @UiField
  VariableItemTable variableItemTable;

  @UiField
  OpalSimplePager variableItemPager;

  private VariableItemProvider variableItemProvider;

  @Inject
  public SearchVariablesView(SearchVariablesView.Binder uiBinder, Translations translations, PlaceManager placeManager) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    this.placeManager = placeManager;
  }
  
  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("searchButton")
  public void onSearch(ClickEvent event) {
    setVariablesVisible(false);
    getUiHandlers().onSearch(queryInput.getText());
  }

  @UiHandler("queryInput")
  public void onQueryTyped(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) onSearch(null);
  }

  @Override
  public void setQuery(String query) {
    queryInput.setText(query);
    setVariablesVisible(false);
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
  public void reset() {
    setVariablesVisible(false);
    refreshPending.setVisible(false);
    queryInput.setText("");
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
      getUiHandlers().onSearchRange(queryInput.getText(), range.getStart(), range.getLength());
    }
  }
}
