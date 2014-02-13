package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.ContingencyTablePresenter;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.gwt.user.client.ui.Label;

public class CategoricalContingencyTable extends ContingencyTable {

  private final List<String> crossWithCategories;

  private final boolean showFrequencies;

  public CategoricalContingencyTable(QueryResultDto queryResult, VariableDto variableDto,
      List<String> variableCategories, VariableDto crossWithVariableDto, List<String> crossWithCategories,
      Translations translations, boolean showFrequencies) {
    super(queryResult, variableDto, variableCategories, crossWithVariableDto, translations);
    this.crossWithCategories = crossWithCategories;
    this.showFrequencies = showFrequencies;
  }

  @Override
  protected void prepareTable() {
    super.prepareTable();
    parentTable.getFlexCellFormatter().setRowSpan(0, 3, crossWithCategories.size() + 5);
  }

  @Override
  void populate() {
    prepareTable();
// Process the resource to have a map by category X crossCategory
    Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets
        = new HashMap<String, Map<String, FacetResultDto.TermFrequencyResultDto>>();
    Map<String, Integer> variableFacetTotals = new HashMap<String, Integer>();
    Map<String, Integer> variableFacetMissings = new HashMap<String, Integer>();
    Map<String, Integer> crossFacetMissings = new HashMap<String, Integer>();
    Map<String, Integer> crossFacetTotals = new HashMap<String, Integer>();

    initStatsticsMaps(facets, variableFacetTotals, variableFacetMissings, crossFacetTotals, crossFacetMissings);

    int nbVariableCategories = variableCategories.size();

    if(facets.size() > 0) {
      addStatistics(facets, variableFacetTotals, variableFacetMissings, crossFacetTotals, crossFacetMissings);
    } else {
      parentTable.setWidget(2, 0, new Label(translations.noResultsFound()));
      parentTable.getFlexCellFormatter().setColSpan(2, 0, nbVariableCategories + 4);
    }
  }

  private void addStatistics(Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets,
      Map<String, Integer> variableFacetTotals, Map<String, Integer> variableFacetMissings,
      Map<String, Integer> crossFacetTotals, Map<String, Integer> crossFacetMissings) {

    int variableCategoriesSize = variableCategories.size();
    int crossCategoriesSize = crossWithCategories.size();

    Integer total = variableFacetTotals.get(ContingencyTablePresenter.TOTAL_FACET);
    int totalMissings = 0;
    for(int i = 0; i < crossCategoriesSize; i++) {
      String crossName = crossWithCategories.get(i);
      parentTable.setWidget(i + 2, 0, new Label(crossName));

      for(int j = 0; j < variableCategoriesSize; j++) {
        String categoryName = variableCategories.get(j);
        writeFacetValue(facets, crossFacetTotals, i, crossName, j, categoryName);
      }

      // Total COL
      parentTable.setWidget(i + 2, variableCategoriesSize + 1,
          getLabel(crossFacetTotals.containsKey(crossName) ? crossFacetTotals.get(crossName) : 0, total));

      // N/A COL
      int count = crossFacetMissings.containsKey(crossName) ? crossFacetMissings.get(crossName) : 0;
      parentTable.setWidget(i + 2, variableCategoriesSize + 2, getLabel(count, total));
      totalMissings += count;
    }

    populateTotalRow(variableFacetTotals, variableCategoriesSize, crossCategoriesSize, total);
    populateMissings(variableFacetMissings, variableCategoriesSize, crossCategoriesSize, total, totalMissings);

  }

  private void populateMissings(Map<String, Integer> variableFacetMissings, int variableCategoriesSize,
      int crossCategoriesSize, Integer total, int totalMissings) {
    int totalNA = totalMissings;

    // Empty row
    parentTable.getFlexCellFormatter().setColSpan(crossCategoriesSize + 3, 0, variableCategoriesSize + 4);
    parentTable.getFlexCellFormatter().addStyleName(crossCategoriesSize + 3, 0, "empty-row");

    for(int i = 0; i < variableCategoriesSize; i++) {
      String key = variableCategories.get(i);
      int count = variableFacetMissings.containsKey(key) ? variableFacetMissings.get(key) : 0;
      parentTable.setWidget(crossCategoriesSize + 4, i + 1, getLabel(count, total));

      totalNA += count;
    }

    parentTable.setWidget(crossCategoriesSize + 4, variableCategoriesSize + 1, new Label("-"));
    Integer count = variableFacetMissings.get(ContingencyTablePresenter.MISSING_FACET);
    parentTable.setWidget(crossCategoriesSize + 4, variableCategoriesSize + 2, getLabel(count, total));

    totalNA += count;
    // N/A - ROW
    parentTable.setWidget(crossCategoriesSize + 4, 0,
        new Label(TranslationsUtils.replaceArguments(translations.NATotalLabel(), getLabelString(totalNA, total))));
  }

  private void populateTotalRow(Map<String, Integer> variableFacetTotals, int variableCategoriesSize,
      int crossCategoriesSize, Integer total) {// Totals ROW
    parentTable.setWidget(crossCategoriesSize + 2, 0, new Label(translations.totalLabel()));
    for(int i = 0; i < variableCategoriesSize; i++) {
      parentTable.setWidget(crossCategoriesSize + 2, i + 1,
          getLabel(variableFacetTotals.get(variableCategories.get(i)), total));
    }
    parentTable.setWidget(crossCategoriesSize + 2, variableCategoriesSize + 1, getLabel(total, total));
    parentTable.setWidget(crossCategoriesSize + 2, variableCategoriesSize + 2, new Label("-"));
  }

  private void writeFacetValue(Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets,
      Map<String, Integer> crossFacetTotals, int i, String crossName, int j, String categoryName) {

    if(facets.containsKey(categoryName)) {
      FacetResultDto.TermFrequencyResultDto termFrequencyResultDto = facets.get(categoryName).get(crossName);
      parentTable.setWidget(i + 2, j + 1,
          getLabel(termFrequencyResultDto == null ? 0 : termFrequencyResultDto.getCount(),
              crossFacetTotals.get(crossName)));
    } else {
      parentTable.setWidget(i + 2, j + 1, getLabel(0, crossFacetTotals.get(crossName)));
    }
  }

  private void initStatsticsMaps(Map<String, Map<String, FacetResultDto.TermFrequencyResultDto>> facets,
      Map<String, Integer> variableFacetTotals, Map<String, Integer> variableFacetMissings,
      Map<String, Integer> crossFacetTotals, Map<String, Integer> crossFacetMissings) {

    for(FacetResultDto facetResultDto : JsArrays.toIterable(queryResult.getFacetsArray())) {
      Map<String, FacetResultDto.TermFrequencyResultDto> termByFacets
          = new HashMap<String, FacetResultDto.TermFrequencyResultDto>();

      int total = 0;
      for(FacetResultDto.TermFrequencyResultDto termFrequencyResultDto : JsArrays
          .toIterable(facetResultDto.getFrequenciesArray())) {

        String term = termFrequencyResultDto.getTerm();

        termByFacets.put(term, termFrequencyResultDto);
        facets.put(facetResultDto.getFacet(), termByFacets);
        total += termFrequencyResultDto.getCount();

        if(facetResultDto.getFacet().equals(ContingencyTablePresenter.TOTAL_FACET)) {
          crossFacetTotals.put(term, termFrequencyResultDto.getCount());
        } else if(facetResultDto.getFacet().equals(ContingencyTablePresenter.MISSING_FACET)) {
          crossFacetMissings.put(term, termFrequencyResultDto.getCount());
        }
      }

      variableFacetTotals.put(facetResultDto.getFacet(), total);
      variableFacetMissings.put(facetResultDto.getFacet(), facetResultDto.getMissing());
    }
  }

  private Label getLabel(int count, Integer total) {
    return new Label(getLabelString(count, total));
  }

  private String getLabelString(int count, Integer total) {
    if(showFrequencies) {
      return String.valueOf(count);
    } else {
      return total == null ? "0 %" : formatDecimal((double) count / total * 100) + " %";
    }
  }
}
