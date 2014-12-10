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

import com.google.common.collect.Iterators;

/**
 * Utility class used to convert an elastic search JSON query to a DTO query.
 */
public class EsResultConverter {

  private static final int MINIMUM_RESULT_COUNT = 0;

  private ItemResultDtoStrategy itemResultStrategy;

  public EsResultConverter() {
  }

  public void setStrategy(ItemResultDtoStrategy strategy) {
    itemResultStrategy = strategy;
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
    JSONArray hits = jsonHits.getJSONArray("hits");
    Search.QueryResultDto.Builder dtoResultsBuilder = Search.QueryResultDto.newBuilder()
        .setTotalHits(jsonHits.getInt("total"));

    if(hits.length() > 0) {
      HitsConverter hitsConverter = new HitsConverter();
      hitsConverter.setStrategy(itemResultStrategy);
      dtoResultsBuilder.addAllHits(hitsConverter.convert(jsonHits.getJSONArray("hits")));
    }

    if(json.has("aggregations")) {
      AggregationsConverter aggsConverter = new AggregationsConverter();
      dtoResultsBuilder.addAllFacets(aggsConverter.convert(json.getJSONObject("aggregations")));
    }

    return dtoResultsBuilder.build();
  }

  private static boolean countAboveThreshold(int count) {
    return count >= MINIMUM_RESULT_COUNT;
  }

  //
  // Inner classes
  //

  private static class AggregationsConverter {

    public Collection<Search.FacetResultDto> convert(JSONObject jsonAggregations) throws JSONException {
      Collection<Search.FacetResultDto> facetsDtoList = new ArrayList<>();

      for(Iterator<String> iterator = jsonAggregations.keys(); iterator.hasNext(); ) {
        String aggName = iterator.next();
        JSONObject jsonAggregation = jsonAggregations.getJSONObject(aggName);
        Search.FacetResultDto.Builder dtoResultBuilder = Search.FacetResultDto.newBuilder().setFacet(aggName);

        convertAggregation(jsonAggregation, dtoResultBuilder);

        if(jsonAggregation.has("doc_count")) {
          if(jsonAggregation.has("0")) {
            convertNestedAggregation(jsonAggregation.getJSONObject("0"), dtoResultBuilder);
          }
          convertFiltered(jsonAggregation, dtoResultBuilder);
        }

        facetsDtoList.add(dtoResultBuilder.build());
      }

      return facetsDtoList;
    }

    private void convertAggregation(JSONObject jsonAggregation, Search.FacetResultDto.Builder dtoResultBuilder)
        throws JSONException {
      if(jsonAggregation.has("buckets")) {
        convertBuckets(jsonAggregation.getJSONArray("buckets"), dtoResultBuilder);
      }

      if(jsonAggregation.has("avg")) {
        convertStats(jsonAggregation, dtoResultBuilder);
      }

      if(jsonAggregation.has("value")) {
        convertCount(jsonAggregation, dtoResultBuilder, "value");
      }

      if(jsonAggregation.has("values")) {
        convertValues(jsonAggregation.getJSONObject("values"), dtoResultBuilder);
      }
    }

    private void convertNestedAggregation(JSONObject jsonAggregation, Search.FacetResultDto.Builder dtoResultBuilder)
        throws JSONException {
      convertAggregation(jsonAggregation, dtoResultBuilder);

      if(jsonAggregation.has("doc_count")) {
        convertCount(jsonAggregation, dtoResultBuilder, "doc_count");
      }
    }

    private void convertFiltered(JSONObject jsonAggregation, Search.FacetResultDto.Builder dtoResultBuilder)
        throws JSONException {

      if(countAboveThreshold(jsonAggregation.getInt("doc_count"))) {
        Search.FacetResultDto.FilterResultDto dtoFilter = Search.FacetResultDto.FilterResultDto.newBuilder()
            .setCount(jsonAggregation.getInt("doc_count")).build();

        dtoResultBuilder.addFilters(dtoFilter);
      }
    }

    private void convertCount(JSONObject jsonAggregation, Search.FacetResultDto.Builder dtoResultBuilder, String key)
        throws JSONException {

      if(countAboveThreshold(jsonAggregation.getInt(key))) {
        Search.FacetResultDto.ValueResultDto dtoValue = Search.FacetResultDto.ValueResultDto
            .newBuilder().setCount(jsonAggregation.getInt(key)).build();

        dtoResultBuilder.addValues(dtoValue);
      }
    }

    private void convertBuckets(JSONArray buckets, Search.FacetResultDto.Builder dtoResultBuilder)
        throws JSONException {

      for(int i = 0; i < buckets.length(); i++) {
        JSONObject term = buckets.getJSONObject(i);

        if(countAboveThreshold(term.getInt("doc_count"))) {
          Search.FacetResultDto.TermFrequencyResultDto dtoTermFrequency = Search.FacetResultDto.TermFrequencyResultDto
              .newBuilder().setTerm(term.getString("key")).setCount(term.getInt("doc_count")).build();

          dtoResultBuilder.addFrequencies(dtoTermFrequency);
        }
      }
    }

    private void convertValues(JSONObject values, Search.FacetResultDto.Builder dtoResultBuilder)
        throws JSONException {

      for (String key : Iterators.toArray(values.keys(), String.class)) {
        Search.FacetResultDto.ValueResultDto dtoValue = Search.FacetResultDto.ValueResultDto
            .newBuilder().setKey(key).setValue((float)values.getDouble(key)).build();

        dtoResultBuilder.addValues(dtoValue);
      }
    }


    private void convertStats(JSONObject jsonStatistical, Search.FacetResultDto.Builder dtoResultBuilder)
        throws JSONException {

      if(countAboveThreshold(jsonStatistical.getInt("count"))) {
        Search.FacetResultDto.StatisticalResultDto dtoStatistical = Search.FacetResultDto.StatisticalResultDto
            .newBuilder().setCount(jsonStatistical.getInt("count")).setTotal(
                (float) jsonStatistical.getDouble("sum")) //
            .setMin((float) jsonStatistical.getDouble("min")) //
            .setMax((float) jsonStatistical.getDouble("max")) //
            .setMean((float) jsonStatistical.getDouble("avg")) //
            .setSumOfSquares((float) jsonStatistical.getDouble("sum_of_squares")) //
            .setVariance((float) jsonStatistical.getDouble("variance")) //
            .setStdDeviation((float) jsonStatistical.getDouble("std_deviation")).build();

        dtoResultBuilder.setStatistics(dtoStatistical);
      }
    }

  }

  /**
   * Class used to convert hits JSON query result into DTO format
   */
  private static class HitsConverter {

    private ItemResultDtoStrategy itemResultStrategy;

    public void setStrategy(ItemResultDtoStrategy strategy) {
      itemResultStrategy = strategy;
    }

    public Collection<Search.ItemResultDto> convert(JSONArray jsonHits) throws JSONException {
      Collection<Search.ItemResultDto> itemsDtoList = new ArrayList<>();

      for(int i = 0; i < jsonHits.length(); i++) {
        Search.ItemResultDto.Builder dtoItemResultBuilder = Search.ItemResultDto.newBuilder();
        JSONObject jsonHit = jsonHits.getJSONObject(i);
        dtoItemResultBuilder.setIdentifier(jsonHit.getString("_id"));
        JSONObject fields = jsonHit.getJSONObject("fields").getJSONArray("partial").getJSONObject(0);

        if(fields.length() > 0) convertFields(dtoItemResultBuilder, fields);
        if(itemResultStrategy != null) itemResultStrategy.process(dtoItemResultBuilder);

        itemsDtoList.add(dtoItemResultBuilder.build());
      }

      return itemsDtoList;
    }

    @SuppressWarnings("unchecked")
    private void convertFields(Search.ItemResultDto.Builder dtoItemResultBuilder, JSONObject jsonFields)
        throws JSONException {

      Search.ItemFieldsDto.Builder dtoItemFieldsBuilder = Search.ItemFieldsDto.newBuilder();

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