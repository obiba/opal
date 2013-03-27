/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Search;
import org.springframework.util.Assert;

/**
 * Utility class used to convert an elastic search JSON query to a DTO query.
 */
public class EsResultConverter {

  private static final int MINIMUM_RESULT_COUNT = 0;

  private ItemResultDtoVisitor itemResultVisitor;

  public EsResultConverter() {
  }

  public void accept(ItemResultDtoVisitor visitor) {
    itemResultVisitor = visitor;
  }

  /**
   * Converts the JSON query to DTO query
   *
   * @param json - query response
   * @return Search.QueryResultDto
   * @throws JSONException
   */
  public Search.QueryResultDto convert(JSONObject json) throws JSONException {
    Assert.notNull(json, "Result JSON is null!");

    JSONObject jsonHits = json.getJSONObject("hits");
    Search.QueryResultDto.Builder dtoResultsBuilder = Search.QueryResultDto.newBuilder()
        .setTotalHits(jsonHits.getInt("total"));

    if (json.has("facets")) {
      FacetsConverter facetsConverter = new FacetsConverter();
      dtoResultsBuilder.addAllFacets(facetsConverter.convert(json.getJSONObject("facets")));
    }
    else if (jsonHits.has("hits")) {
      HitsConverter hitsConverter = new HitsConverter();
      hitsConverter.accept(itemResultVisitor);
      dtoResultsBuilder.addAllHits(hitsConverter.convert(jsonHits.getJSONArray("hits")));
    }

    return dtoResultsBuilder.build();
  }

  //
  // Inner classes
  //

  /**
   * Class used to convert facets JSON query result into DTO format
   */
  private static class FacetsConverter {

    public Collection<Search.FacetResultDto> convert(JSONObject jsonFacets) throws JSONException {
      Collection<Search.FacetResultDto> facetsDtoList = new ArrayList<Search.FacetResultDto>();

      for(Iterator<String> iterator = jsonFacets.keys(); iterator.hasNext(); ) {
        String facet = iterator.next();
        JSONObject jsonFacet = jsonFacets.getJSONObject(facet);
        Search.FacetResultDto.Builder dtoFacetResultBuilder = Search.FacetResultDto.newBuilder().setFacet(facet);

        if("terms".equals(jsonFacet.get("_type"))) {
          convertTerms(jsonFacet.getJSONArray("terms"), dtoFacetResultBuilder);
        } else if("statistical".equals(jsonFacet.get("_type"))) {
          convertStatistical(jsonFacet, dtoFacetResultBuilder);
        } else if("filter".equals(jsonFacet.get("_type"))) {
          convertFiltered(jsonFacet, dtoFacetResultBuilder);
        }

        facetsDtoList.add(dtoFacetResultBuilder.build());
      }

      return facetsDtoList;
    }


    private void convertFiltered(JSONObject jsonFacet, Search.FacetResultDto.Builder dtoResultBuilder)
        throws JSONException {

      if(countAboveThreshold(jsonFacet.getInt("count"))) {
        Search.FacetResultDto.FilterResultDto dtoFilter = Search.FacetResultDto.FilterResultDto.newBuilder()
            .setCount(jsonFacet.getInt("count")).build();

        dtoResultBuilder.addFilters(dtoFilter);
      }
    }

    private void convertTerms(JSONArray terms, Search.FacetResultDto.Builder dtoFacetResultBuilder) throws JSONException {

      for(int i = 0; i < terms.length(); i++) {
        JSONObject term = terms.getJSONObject(i);

        if(countAboveThreshold(term.getInt("count"))) {
          Search.FacetResultDto.TermFrequencyResultDto dtoTermFrequency = Search.FacetResultDto.TermFrequencyResultDto
              .newBuilder().setTerm(term.getString("term")).setCount(term.getInt("count")).build();

          dtoFacetResultBuilder.addFrequencies(dtoTermFrequency);
        }
      }
    }

    private void convertStatistical(JSONObject jsonStatistical, Search.FacetResultDto.Builder dtoFacetResultBuilder)
        throws JSONException {

      if(countAboveThreshold(jsonStatistical.getInt("count"))) {
        Search.FacetResultDto.StatisticalResultDto dtoStatistical = Search.FacetResultDto.StatisticalResultDto
            .newBuilder().setCount(jsonStatistical.getInt("count")).setTotal((float) jsonStatistical.getDouble("total"))
            .setMin((float) jsonStatistical.getDouble("min")).setMax((float) jsonStatistical.getDouble("max"))
            .setMean((float) jsonStatistical.getDouble("mean"))
            .setSumOfSquares((float) jsonStatistical.getDouble("sum_of_squares"))
            .setVariance((float) jsonStatistical.getDouble("variance"))
            .setStdDeviation((float) jsonStatistical.getDouble("std_deviation")).build();

        dtoFacetResultBuilder.setStatistics(dtoStatistical);
      }
    }

    private boolean countAboveThreshold(int count) {
      return count >= MINIMUM_RESULT_COUNT;
    }
  }


  /**
   * Class used to convert hits JSON query result into DTO format
   */
  private static class HitsConverter {

    private ItemResultDtoVisitor itemResultVisitor;

    public void accept(ItemResultDtoVisitor visitor) {
      itemResultVisitor = visitor;
    }

    public Collection<Search.ItemResultDto> convert(JSONArray jsonHits) throws JSONException {
      Collection<Search.ItemResultDto> itemsDtoList = new ArrayList<Search.ItemResultDto>();

      for(int i = 0; i < jsonHits.length(); i++) {
        Search.ItemResultDto.Builder dtoItemResultBuilder = Search.ItemResultDto.newBuilder();
        JSONObject jsonHit = jsonHits.getJSONObject(i);
        dtoItemResultBuilder.setIdentifier(jsonHit.getString("_id"));

        if (jsonHit.has("fields")) {
          convertFields(dtoItemResultBuilder, jsonHit.getJSONObject("fields"));
        }

        if (itemResultVisitor != null) itemResultVisitor.visit(dtoItemResultBuilder);
        itemsDtoList.add(dtoItemResultBuilder.build());
      }

      return itemsDtoList;
    }

    private void convertFields(Search.ItemResultDto.Builder dtoItemResultBuilder, JSONObject jsonFields)
        throws JSONException {

      Search.ItemFieldsDto.Builder  dtoItemFieldsBuilder = Search.ItemFieldsDto.newBuilder();

      for(Iterator<String> iterator = jsonFields.keys(); iterator.hasNext(); ) {

        Opal.EntryDto.Builder entryBuilder = Opal.EntryDto.newBuilder();
        String key = iterator.next();
        entryBuilder.setKey(key);
        entryBuilder.setValue(jsonFields.getString(key));
        dtoItemFieldsBuilder.addFields(entryBuilder.build());
      }

      dtoItemResultBuilder.setExtension(Search.ItemFieldsDto.item, dtoItemFieldsBuilder.build());
    }
  }


}