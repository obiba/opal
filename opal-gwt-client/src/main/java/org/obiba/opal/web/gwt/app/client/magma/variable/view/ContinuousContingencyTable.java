package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.ContingencyTablePresenter;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.gwt.user.client.ui.Label;

public class ContinuousContingencyTable extends ContingencyTable {

  public ContinuousContingencyTable(QueryResultDto queryResult, VariableDto variableDto,
      List<String> variableCategories, VariableDto crossWithVariableDto, Translations translations) {
    super(queryResult, variableDto, variableCategories, crossWithVariableDto, translations);
  }

  @Override
  protected void prepareTable() {
    super.prepareTable();
    parentTable.getFlexCellFormatter().setRowSpan(0, 3, 7);
    parentTable.setWidget(2, 0, new Label(translations.meanLabel()));
    parentTable.setWidget(3, 0, new Label(translations.standardDeviationLabel()));
    parentTable.setWidget(4, 0, new Label(translations.NLabel()));
  }

  @Override
  void populate() {
    prepareTable();

    Map<String, FacetResultDto.StatisticalResultDto> continuousFacets
        = new HashMap<String, FacetResultDto.StatisticalResultDto>();
    Map<String, Integer> missingFacets = new HashMap<String, Integer>();
    Integer missingsByMissings = computeValues(continuousFacets, missingFacets);

    int totalMissings = populateValues(continuousFacets, missingFacets, missingsByMissings);
    totalMissings += missingsByMissings;

    populateMissings(missingFacets, totalMissings);

    populateTotal(continuousFacets);

  }

  private Integer computeValues(Map<String, FacetResultDto.StatisticalResultDto> continuousFacets,
      Map<String, Integer> missingFacets) {
    Integer missingsByMissings = 0;

    for(FacetResultDto facetResultDto : JsArrays.toIterable(queryResult.getFacetsArray())) {
      if(facetResultDto.hasStatistics()) {
        continuousFacets.put(facetResultDto.getFacet(), facetResultDto.getStatistics());
      } else {
        if(facetResultDto.hasMissing()) {
          missingFacets.put(facetResultDto.getFacet(), facetResultDto.getTotal());
          if(facetResultDto.getFacet().equals(ContingencyTablePresenter.MISSING_BY_CATEGORY_FACET)) {
            missingsByMissings = facetResultDto.getMissing();
          }
        }
        for(FacetResultDto.TermFrequencyResultDto termFrequencyResultDto : JsArrays
            .toIterable(facetResultDto.getFrequenciesArray())) {
          missingFacets.put(termFrequencyResultDto.getTerm(), termFrequencyResultDto.getCount());
        }
      }
    }
    return missingsByMissings;
  }

  private void populateMissings(Map<String, Integer> missingFacets, int totalMissings) {
    int totalNA = totalMissings;
    int variableCategoriesSize = variableCategories.size();

    // N/A COL
    parentTable.setWidget(2, variableCategoriesSize + 2, new Label("-"));
    parentTable.setWidget(3, variableCategoriesSize + 2, new Label("-"));
    parentTable.setWidget(4, variableCategoriesSize + 2,
        new Label(String.valueOf(missingFacets.get(ContingencyTablePresenter.MISSING_FACET))));
    totalNA += missingFacets.get(ContingencyTablePresenter.MISSING_FACET);

    // N/A Total ROW
    parentTable.setWidget(6, 0,
        new Label(translations.NALabel() + " (" + translations.totalLabel().toLowerCase() + ": " + totalNA + ")"));
  }

  private void populateTotal(Map<String, FacetResultDto.StatisticalResultDto> continuousFacets) {
    // Total COL
    int variableCategoriesSize = variableCategories.size();
    parentTable.setWidget(2, variableCategoriesSize + 1,
        new Label(formatDecimal(continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getMean())));
    parentTable.setWidget(3, variableCategoriesSize + 1,
        new Label(formatDecimal(continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getStdDeviation())));
    parentTable.setWidget(4, variableCategoriesSize + 1,
        new Label(String.valueOf((int) continuousFacets.get(ContingencyTablePresenter.TOTAL_FACET).getCount())));
    parentTable.setWidget(6, variableCategoriesSize + 1, new Label("-"));
  }

  private int populateValues(Map<String, FacetResultDto.StatisticalResultDto> continuousFacets,
      Map<String, Integer> missingFacets, Integer missingsByMissings) {

    int variableCategoriesSize = variableCategories.size();

    // Empty row
    parentTable.getFlexCellFormatter().setColSpan(5, 0, variableCategories.size() + 4);
    parentTable.getFlexCellFormatter().addStyleName(5, 0, "empty-row");

    int totalMissings = 0;
    for(int i = 0; i < variableCategoriesSize; i++) {
      String key = variableCategories.get(i);
      writeFacetRow(continuousFacets, i + 1, key);
      Integer nb = missingFacets.containsKey(key) ? missingFacets.get(key) : 0;
      parentTable.setWidget(6, i + 1, new Label(String.valueOf(nb)));
      totalMissings += nb;
    }
    parentTable.setWidget(6, variableCategoriesSize + 2, new Label(String.valueOf(missingsByMissings)));

    return totalMissings;
  }

  private void writeFacetRow(Map<String, FacetResultDto.StatisticalResultDto> continuousFacets, int col, String name) {
    parentTable.setWidget(2, col, new Label(formatDecimal(continuousFacets.get(name).getMean())));
    parentTable.setWidget(3, col, new Label(formatDecimal(continuousFacets.get(name).getStdDeviation())));
    parentTable.setWidget(4, col, new Label(String.valueOf((int) continuousFacets.get(name).getCount())));
  }

}
