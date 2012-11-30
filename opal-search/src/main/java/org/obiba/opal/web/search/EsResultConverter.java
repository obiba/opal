/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.util.Iterator;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.web.model.Search;
import org.springframework.util.Assert;

/**
 * Utility class used to convert an elastic search JSON query to a DTO query.
 */
public class EsResultConverter {

  private static final int MINIMUM_RESULT_COUNT = 0;

  /**
   * @param dtoQuery - keeps the original DTO query in order to retrieve the variable and facet names
   */
  public EsResultConverter() {
  }

  /**
   * Converts the JSON query to DTO query
   *
   * @param json
   * @return
   * @throws JSONException
   */
  public Search.QueryResultDto convert(JSONObject json) throws JSONException {
    Assert.notNull(json, "Result JSON is null!");

    return convertFacets(json.getJSONObject("facets"));
  }

  private Search.QueryResultDto convertFacets(JSONObject jsonFacets) throws JSONException {

    Search.QueryResultDto.Builder dtoResultsBuilder = Search.QueryResultDto.newBuilder();

    for (Iterator<String> iterator = jsonFacets.keys(); iterator.hasNext(); ) {
      String facet = iterator.next();
      JSONObject jsonFacet = jsonFacets.getJSONObject(facet);
      Search.FacetResultDto.Builder dtoFacetResultBuilder = Search.FacetResultDto.newBuilder().setFacet(facet);

      if("terms".equals(jsonFacet.get("_type"))) {
        convertTerms(jsonFacet.getJSONArray("terms"), dtoFacetResultBuilder);
      } else if("statistical".equals(jsonFacet.get("_type"))) {
        convertStatistical(jsonFacet, dtoFacetResultBuilder);
      }
      else if ("filter".equals(jsonFacet.get("_type"))) {
        convertFiltered(jsonFacet, dtoFacetResultBuilder);
      }

      if (dtoFacetResultBuilder.getFiltersCount() > 0
            || dtoFacetResultBuilder.getFrequenciesCount() > 0
            || dtoFacetResultBuilder.hasStatistics()) {

        dtoResultsBuilder.addFacets(dtoFacetResultBuilder.build());
      }
    }


    return dtoResultsBuilder.build();
  }

  private void convertFiltered(JSONObject jsonFacet, Search.FacetResultDto.Builder dtoResultBuilder) throws JSONException {

    if (countAboveThreshold(jsonFacet.getInt("count"))) {
      Search.FacetResultDto.FilterResultDto dtoFilter =
          Search.FacetResultDto.FilterResultDto.newBuilder()
          .setCount(jsonFacet.getInt("count")).build();

      dtoResultBuilder.addFilters(dtoFilter);
    }
  }

  private void convertTerms(JSONArray terms, Search.FacetResultDto.Builder dtoFacetResultBuilder) throws JSONException {

    for(int i = 0; i < terms.length(); i++) {
      JSONObject term = terms.getJSONObject(i);

      if (countAboveThreshold(term.getInt("count"))) {
        Search.FacetResultDto.TermFrequencyResultDto dtoTermFrequency = Search.FacetResultDto.TermFrequencyResultDto.newBuilder()
            .setTerm(term.getString("term")).setCount(term.getInt("count")).build();
  
        dtoFacetResultBuilder.addFrequencies(dtoTermFrequency);
      }
    }
  }

  private void convertStatistical(JSONObject jsonStatistical,
      Search.FacetResultDto.Builder dtoFacetResultBuilder) throws JSONException {

    if (countAboveThreshold(jsonStatistical.getInt("count"))) {
      Search.FacetResultDto.StatisticalResultDto dtoStatistical = Search.FacetResultDto.StatisticalResultDto.newBuilder()
          .setCount(jsonStatistical.getInt("count")).setTotal(jsonStatistical.getInt("total"))
          .setMin(jsonStatistical.getInt("min")).setMax(jsonStatistical.getInt("max"))
          .setMean(jsonStatistical.getInt("mean")).setSumOfSquares(jsonStatistical.getInt("sum_of_squares"))
          .setVariance(jsonStatistical.getInt("variance")).setStdDeviation(jsonStatistical.getInt("std_deviation"))
          .build();

      dtoFacetResultBuilder.setStatistics(dtoStatistical);
    }
  }

  private boolean countAboveThreshold(int count) {
    return count >= MINIMUM_RESULT_COUNT;
  }

}
