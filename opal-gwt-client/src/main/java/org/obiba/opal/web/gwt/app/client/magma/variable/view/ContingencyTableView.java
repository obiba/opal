/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.HashMap;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.ContingencyTablePresenter;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class ContingencyTableView extends ViewImpl implements ContingencyTablePresenter.Display {

  private static final int DEFAULT_WIDTH = 60;

  private QueryResultDto queryResult;

  private VariableDto variable;

  private VariableDto crossWithVariable;

  private boolean showFrequencies = true;

  interface Binder extends UiBinder<Widget, ContingencyTableView> {}

  @UiField
  FlowPanel crossTable;

  @UiField
  NavLink frequency;

  @UiField
  NavLink percentage;

  private final Translations translations;

  @Inject
  public ContingencyTableView(Binder uiBinder, Translations translations) {
    this.translations = translations;

    initWidget(uiBinder.createAndBindUi(this));

  }

  @UiHandler("percentage")
  public void onPercentage(ClickEvent event) {
    percentage.setActive(true);
    frequency.setActive(false);
    showFrequencies = false;
    crossTable.clear();
    draw();
  }

  @UiHandler("frequency")
  public void onFrequency(ClickEvent event) {
    frequency.setActive(true);
    percentage.setActive(false);
    showFrequencies = true;
    crossTable.clear();
    draw();
  }

  @Override
  public void init(QueryResultDto resource, VariableDto variable, VariableDto crossWithVariable) {
    queryResult = resource;
    this.variable = variable;
    this.crossWithVariable = crossWithVariable;
  }

  @Override
  public void draw() {
    DefaultFlexTable parentTable = new DefaultFlexTable();

    addHeader(parentTable);

    percentage.setVisible(!queryResult.getFacetsArray().get(0).hasStatistics());
    frequency.setVisible(!queryResult.getFacetsArray().get(0).hasStatistics());

    // data
    if(queryResult.getFacetsArray().get(0).hasStatistics()) {
      addContinuousStatistics(parentTable);
    } else {
      addCategoricalStatistics(parentTable);
    }
    crossTable.add(parentTable);

  }

  private void addCategoricalStatistics(DefaultFlexTable parentTable) {
    // Process the resource to have a map by categoryXcrossCategory
    Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets
        = new HashMap<String, Map<String, FacetResultDto.TermFrequencyResultDto>>();
    Map<String, Integer> variableFacetTotals = new HashMap<String, Integer>();
    Map<String, Integer> crossFacetTotals = new HashMap<String, Integer>();
    initStatsticsMaps(facets, variableFacetTotals, crossFacetTotals);

    int nbCrossCategories = crossWithVariable.getCategoriesArray().length();
    int nbVariableCategories = variable.getCategoriesArray().length();
    if(facets.size() > 0) {

      addStatistics(parentTable, facets, variableFacetTotals, crossFacetTotals, nbCrossCategories,
          nbVariableCategories);
    } else {
      parentTable.setWidget(2, 0, new Label(translations.noResultsFound()));
      parentTable.getFlexCellFormatter().setColSpan(2, 0, nbVariableCategories + 4);

      percentage.setVisible(false);
      frequency.setVisible(false);
    }

  }

  private void addStatistics(DefaultFlexTable parentTable,
      Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets, Map<String, Integer> variableFacetTotals,
      Map<String, Integer> crossFacetTotals, int nbCrossCategories, int nbVariableCategories) {
    for(int i = 0; i < nbCrossCategories; i++) {
      String crossName = crossWithVariable.getCategoriesArray().get(i).getName();
      parentTable.setWidget(i + 2, 0, new Label(crossName));

      for(int j = 0; j < nbVariableCategories; j++) {
        String categoryName = variable.getCategoriesArray().get(j).getName();

        if(facets.containsKey(categoryName)) {
          FacetResultDto.TermFrequencyResultDto termFrequencyResultDto = facets.get(categoryName).get(crossName);
          addValue(parentTable, i + 2, j + 1, termFrequencyResultDto == null ? 0 : termFrequencyResultDto.getCount(),
              crossFacetTotals.get(crossName));
        } else {
          addValue(parentTable, i + 2, j + 1, 0, crossFacetTotals.get(crossName));
        }
      }

      addValue(parentTable, i + 2, nbVariableCategories + 1,
          crossFacetTotals.containsKey(crossName) ? crossFacetTotals.get(crossName) : 0,
          variableFacetTotals.get("total"));
    }

    // N
    parentTable.setWidget(nbCrossCategories + 3, 0, new Label(translations.totalLabel()));
    for(int i = 0; i < nbVariableCategories; i++) {
      addValue(parentTable, nbCrossCategories + 3, i + 1,
          variableFacetTotals.get(variable.getCategoriesArray().get(i).getName()), variableFacetTotals.get("total"));
    }

    addValue(parentTable, nbCrossCategories + 3, nbVariableCategories + 1, variableFacetTotals.get("total"),
        variableFacetTotals.get("total"));
  }

  private void initStatsticsMaps(Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets,
      Map<String, Integer> variableFacetTotals, Map<String, Integer> crossFacetTotals) {

    for(FacetResultDto facetResultDto : JsArrays.toIterable(queryResult.getFacetsArray())) {
      Map<String, FacetResultDto.TermFrequencyResultDto> termByFacets
          = new HashMap<String, FacetResultDto.TermFrequencyResultDto>();

      int total = 0;
      for(FacetResultDto.TermFrequencyResultDto termFrequencyResultDto : JsArrays
          .toIterable(facetResultDto.getFrequenciesArray())) {

        termByFacets.put(termFrequencyResultDto.getTerm(), termFrequencyResultDto);
        facets.put(facetResultDto.getFacet(), termByFacets);
        total += termFrequencyResultDto.getCount();

        if(facetResultDto.getFacet().equals("total")) {
          crossFacetTotals.put(termFrequencyResultDto.getTerm(), termFrequencyResultDto.getCount());
        }
      }

      variableFacetTotals.put(facetResultDto.getFacet(), total);
    }
  }

  private void addValue(DefaultFlexTable parentTable, int row, int column, int count, Integer total) {
    if(showFrequencies) {
      parentTable.setWidget(row, column, new Label(String.valueOf(count)));
    } else {
      double d = count;
      parentTable.setWidget(row, column, new Label(total == null ? "0 %" : formatDecimal(d / total * 100) + " %"));
    }
  }

  private void addContinuousStatistics(DefaultFlexTable parentTable) {
    Map<String, FacetResultDto.StatisticalResultDto> continuousFacets
        = new HashMap<String, FacetResultDto.StatisticalResultDto>();
    for(FacetResultDto facetResultDto : JsArrays.toIterable(queryResult.getFacetsArray())) {
      continuousFacets.put(facetResultDto.getFacet(), facetResultDto.getStatistics());
    }

    parentTable.setWidget(2, 0, new Label(translations.meanLabel()));
    parentTable.setWidget(3, 0, new Label(translations.standardDeviationLabel()));
    parentTable.setWidget(4, 0, new Label(translations.NLabel()));
    for(int i = 0; i < variable.getCategoriesArray().length(); i++) {
      CategoryDto categoryDto = variable.getCategoriesArray().get(i);

      parentTable.setWidget(2, i + 1, new Label(formatDecimal(continuousFacets.get(categoryDto.getName()).getMean())));
      parentTable
          .setWidget(3, i + 1, new Label(formatDecimal(continuousFacets.get(categoryDto.getName()).getStdDeviation())));
      parentTable
          .setWidget(4, i + 1, new Label(String.valueOf((int) continuousFacets.get(categoryDto.getName()).getCount())));
    }

    parentTable.setWidget(2, variable.getCategoriesArray().length() + 1,
        new Label(formatDecimal(continuousFacets.get("total").getMean())));
    parentTable.setWidget(3, variable.getCategoriesArray().length() + 1,
        new Label(formatDecimal(continuousFacets.get("total").getStdDeviation())));
    parentTable.setWidget(4, variable.getCategoriesArray().length() + 1,
        new Label(String.valueOf((int) continuousFacets.get("total").getCount())));
  }

  private String formatDecimal(double number) {
    NumberFormat nf = NumberFormat.getFormat("#.##");
    return nf.format(number);
  }

  private void addHeader(DefaultFlexTable parentTable) {
    parentTable.setWidget(0, 0, new Label(crossWithVariable.getName()));
    parentTable.setWidget(0, 1, new Label(variable.getName()));
    parentTable.setWidget(0, 2, new Label(translations.totalLabel()));
    parentTable.getFlexCellFormatter().setRowSpan(0, 0, 2);
    parentTable.getFlexCellFormatter().setRowSpan(0, 2, 2);
    parentTable.getFlexCellFormatter().setColSpan(0, 1, variable.getCategoriesArray().length());
    parentTable.getFlexCellFormatter().setWidth(0, 1, DEFAULT_WIDTH + "%");
    parentTable.getFlexCellFormatter().setWidth(0, 2, "10%");

    parentTable.getFlexCellFormatter().addStyleName(0, 0, "cross-table-header");
    parentTable.getFlexCellFormatter().addStyleName(0, 1, "cross-table-header");
    parentTable.getFlexCellFormatter().addStyleName(0, 2, "cross-table-header");

    int width = DEFAULT_WIDTH / variable.getCategoriesArray().length();
    for(int i = 0; i < variable.getCategoriesArray().length(); i++) {
      parentTable.setWidget(1, i, new Label(variable.getCategoriesArray().get(i).getName()));
      parentTable.getFlexCellFormatter().setWidth(1, i, width + "%");
      parentTable.getFlexCellFormatter().addStyleName(1, i, "cross-table-header");
    }
  }
}
