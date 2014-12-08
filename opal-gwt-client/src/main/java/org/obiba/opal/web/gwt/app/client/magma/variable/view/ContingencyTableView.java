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
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.ContingencyTablePresenter;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavPills;
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

  private List<String> variableCategories;

  private List<String> crossWithCategories;

  private boolean showFrequencies = true;

  interface Binder extends UiBinder<Widget, ContingencyTableView> {}

  @UiField
  FlowPanel crossTable;

  @UiField
  NavPills pills;

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
  public void init(QueryResultDto resource, VariableDto variableDto, List<String> variableCategories,
      VariableDto crossWithVariableDto, List<String> crossWithCategories) {
    queryResult = resource;
    variable = variableDto;
    crossWithVariable = crossWithVariableDto;
    this.variableCategories = variableCategories;
    this.crossWithCategories = crossWithCategories;
  }

  @Override
  public void draw() {
    DefaultFlexTable parentTable = new DefaultFlexTable();

    addHeader(parentTable);

    pills.setVisible(!queryResult.getFacetsArray().get(0).hasStatistics());

    // data
    if(queryResult.getFacetsArray().get(0).hasStatistics()) {
      populateContinuousContingencyTable(parentTable);
    } else {
      populateCategoricalContingencyTable(parentTable);
    }
    crossTable.add(parentTable);
  }

  private void populateCategoricalContingencyTable(DefaultFlexTable parentTable) {
    // Process the resource to have a map by category X crossCategory
    Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets
        = new HashMap<String, Map<String, FacetResultDto.TermFrequencyResultDto>>();
    Map<String, Integer> variableFacetTotals = new HashMap<String, Integer>();
    Map<String, Integer> crossFacetTotals = new HashMap<String, Integer>();
    initStatsticsMaps(facets, variableFacetTotals, crossFacetTotals);

    int nbVariableCategories = variableCategories.size();

    if(facets.size() > 0) {
      addStatistics(parentTable, facets, variableFacetTotals, crossFacetTotals);
    } else {
      parentTable.setWidget(2, 0, new Label(translations.noResultsFound()));
      parentTable.getFlexCellFormatter().setColSpan(2, 0, nbVariableCategories + 4);

      pills.setVisible(false);
    }

  }

  private void addStatistics(DefaultFlexTable parentTable,
      Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets, Map<String, Integer> variableFacetTotals,
      Map<String, Integer> crossFacetTotals) {

    int variableCategoriesSize = variableCategories.size();
    int crossCategoriesSize = crossWithCategories.size();
    for(int i = 0; i < crossCategoriesSize; i++) {
      String crossName = crossWithCategories.get(i);
      parentTable.setWidget(i + 2, 0, new Label(crossName));

      for(int j = 0; j < variableCategoriesSize; j++) {
        String categoryName = variableCategories.get(j);
        writeFacetValue(parentTable, facets, crossFacetTotals, i, crossName, j, categoryName);
      }

      addValue(parentTable, i + 2, variableCategoriesSize + 1,
          crossFacetTotals.containsKey(crossName) ? crossFacetTotals.get(crossName) : 0,
          variableFacetTotals.get(ContingencyTablePresenter.TOTAL_FACET));
    }

    // N
    parentTable.setWidget(crossCategoriesSize + 3, 0, new Label(translations.totalLabel()));
    for(int i = 0; i < variableCategoriesSize; i++) {
      addValue(parentTable, crossCategoriesSize + 3, i + 1, variableFacetTotals.get(variableCategories.get(i)),
          variableFacetTotals.get(ContingencyTablePresenter.TOTAL_FACET));
    }

    addValue(parentTable, crossCategoriesSize + 3, variableCategoriesSize + 1,
        variableFacetTotals.get(ContingencyTablePresenter.TOTAL_FACET),
        variableFacetTotals.get(ContingencyTablePresenter.TOTAL_FACET));
  }

  private void writeFacetValue(DefaultFlexTable parentTable,
      Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets, Map<String, Integer> crossFacetTotals,
      int i, String crossName, int j, String categoryName) {

    if(facets.containsKey(categoryName)) {
      FacetResultDto.TermFrequencyResultDto termFrequencyResultDto = facets.get(categoryName).get(crossName);
      addValue(parentTable, i + 2, j + 1, termFrequencyResultDto == null ? 0 : termFrequencyResultDto.getCount(),
          crossFacetTotals.get(crossName));
    } else {
      addValue(parentTable, i + 2, j + 1, 0, crossFacetTotals.get(crossName));
    }
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

        if(facetResultDto.getFacet().equals(ContingencyTablePresenter.TOTAL_FACET)) {
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

  private void populateContinuousContingencyTable(DefaultFlexTable parentTable) {
    Map<String, FacetResultDto.StatisticalResultDto> continuousFacets
        = new HashMap<String, FacetResultDto.StatisticalResultDto>();
    for(FacetResultDto facetResultDto : JsArrays.toIterable(queryResult.getFacetsArray())) {
      continuousFacets.put(facetResultDto.getFacet(), facetResultDto.getStatistics());
    }

    parentTable.setWidget(2, 0, new Label(translations.minLabel()));
    parentTable.setWidget(3, 0, new Label(translations.maxLabel()));
    parentTable.setWidget(4, 0, new Label(translations.meanLabel()));
    parentTable.setWidget(5, 0, new Label(translations.standardDeviationLabel()));
    parentTable.setWidget(6, 0, new Label(translations.NLabel()));

    int variableCategoriesSize = variableCategories.size();
    for(int i = 0; i < variableCategoriesSize; i++) {
      writeFacetRow(parentTable, continuousFacets, i + 1, variableCategories.get(i));
    }

    parentTable.setWidget(2, variableCategoriesSize + 1,
        new Label(formatDecimal(continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getMin())));
    parentTable.setWidget(3, variableCategoriesSize + 1,
        new Label(formatDecimal(continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getMax())));
    parentTable.setWidget(4, variableCategoriesSize + 1,
        new Label(formatDecimal(continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getMean())));
    parentTable.setWidget(5, variableCategoriesSize + 1,
        new Label(formatDecimal(continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getStdDeviation())));
    parentTable.setWidget(6, variableCategoriesSize + 1,
        new Label(String.valueOf((int) continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getCount())));
  }

  private void writeFacetRow(DefaultFlexTable parentTable,
      Map<String, FacetResultDto.StatisticalResultDto> continuousFacets, int col, String name) {
    parentTable.setWidget(2, col, new Label(formatDecimal(continuousFacets.get(name).getMin())));
    parentTable.setWidget(3, col, new Label(formatDecimal(continuousFacets.get(name).getMax())));
    parentTable.setWidget(4, col, new Label(formatDecimal(continuousFacets.get(name).getMean())));
    parentTable.setWidget(5, col, new Label(formatDecimal(continuousFacets.get(name).getStdDeviation())));
    parentTable.setWidget(6, col, new Label(String.valueOf((int) continuousFacets.get(name).getCount())));
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
    parentTable.getFlexCellFormatter().setColSpan(0, 1, variableCategories.size());
    parentTable.getFlexCellFormatter().setWidth(0, 1, DEFAULT_WIDTH + "%");
    parentTable.getFlexCellFormatter().setWidth(0, 2, "10%");

    parentTable.getFlexCellFormatter().addStyleName(0, 0, "bold-table-header");
    parentTable.getFlexCellFormatter().addStyleName(0, 1, "bold-table-header");
    parentTable.getFlexCellFormatter().addStyleName(0, 2, "bold-table-header");

    int width = DEFAULT_WIDTH / variableCategories.size();
    for(int i = 0; i < variableCategories.size(); i++) {
      writeCategoryHeader(parentTable, variableCategories.get(i), width, i);
    }

  }

  private void writeCategoryHeader(DefaultFlexTable parentTable, String name, int width, int col) {
    parentTable.setWidget(1, col, new Label(name));
    parentTable.getFlexCellFormatter().setWidth(1, col, width + "%");
    parentTable.getFlexCellFormatter().addStyleName(1, col, "bold-table-header");
  }
}
